package com.swapna.foodapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swapna.foodapp.di.IoDispatcher
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.AppConstants.ERR_COULD_NOT_LOAD_PROFILE
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_REMOVE_ADDRESS
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_UPDATE_PROFILE
import com.swapna.foodapp.utils.AppConstants.ERR_LOGOUT_FAILED
import com.swapna.foodapp.utils.AppConstants.ERR_NAME_EMPTY
import com.swapna.foodapp.utils.AppConstants.ERR_NO_USER_FOUND
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_NAVIGATION
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_UI
import com.swapna.foodapp.utils.AppConstants.MSG_ADDRESS_REMOVED
import com.swapna.foodapp.utils.AppConstants.MSG_PROFILE_UPDATED
import com.swapna.foodapp.utils.AppConstants.OBSERVER_FAILED
import com.swapna.foodapp.utils.AppConstants.PLACEHOLDER_ADD_EMAIL
import com.swapna.foodapp.utils.AppConstants.PLACEHOLDER_ADD_NAME
import com.swapna.foodapp.utils.AppConstants.PROFILE_MERGE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    data class ProfileUiState(
        val isLoading: Boolean = true,
        val user: User? = null,
        val orders: List<Order> = emptyList(),
        val isEditMode: Boolean = false,
        val editName: String = "",
        val editEmail: String = "",
        val error: String? = null,
    ) {
        val phoneNumber: String
            get() = user?.phone?.ifEmpty {
                FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
            } ?: ""

        val displayName: String
            get() = user?.name?.ifEmpty { PLACEHOLDER_ADD_NAME } ?: PLACEHOLDER_ADD_NAME
        val displayEmail: String
            get() = user?.email?.ifEmpty { PLACEHOLDER_ADD_EMAIL } ?: PLACEHOLDER_ADD_EMAIL
        val addresses: List<Address> get() = user?.addresses ?: emptyList()
        val hasAddresses: Boolean get() = addresses.isNotEmpty()
        val isLoggedIn: Boolean get() = user != null
    }

    sealed class ProfileEvent {
        object NavigateToLogin : ProfileEvent()
        object NavigateBack : ProfileEvent()
        data class ShowSnackbar(val message: String) : ProfileEvent()
        data class ShowError(val message: String) : ProfileEvent()
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<ProfileEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    private val _uiEvents = MutableSharedFlow<ProfileEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_UI,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _mergedEvents = MutableSharedFlow<ProfileEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val events: SharedFlow<ProfileEvent> = _mergedEvents.asSharedFlow()

    private val observerExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, OBSERVER_FAILED)
        }

    private val mergeObserverHandler =
        CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, PROFILE_MERGE_FAILED)
        }

    init {
        loadUserProfile()
        loadOrders()
        viewModelScope.launch(mergeObserverHandler) {
            _navigationEvents.collect { _mergedEvents.emit(it) }
        }
        viewModelScope.launch(mergeObserverHandler) {
            _uiEvents.collect { _mergedEvents.emit(it) }
        }
    }

    private fun loadUserProfile() =
        viewModelScope.launch(observerExceptionHandler) {
            userRepository.getCurrentUser().collect { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        editName = user?.name ?: "",
                        editEmail = user?.email ?: "",
                        error = if (user == null) ERR_COULD_NOT_LOAD_PROFILE else null,
                    )
                }
            }
        }

    private fun loadOrders() =
        viewModelScope.launch(observerExceptionHandler) {
            userRepository.getRecentOrders().collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }

    fun onEditClicked() {
        val current = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditMode = true,
                editName = current?.name ?: "",
                editEmail = current?.email ?: "",
            )
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(editEmail = email) }
    }

    fun onCancelEdit() {
        val current = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditMode = false,
                editName = current?.name ?: "",
                editEmail = current?.email ?: "",
            )
        }
    }

    fun onSaveProfile() = viewModelScope.launch {
        val state = _uiState.value
        val current = state.user

        if (current == null) {
            _uiEvents.emit(ProfileEvent.ShowError(ERR_NO_USER_FOUND))
            return@launch
        }
        if (state.editName.isBlank()) {
            _uiEvents.emit(ProfileEvent.ShowError(ERR_NAME_EMPTY))
            return@launch
        }

        try {
            val updatedUser = current.copy(
                name = state.editName.trim(),
                email = state.editEmail.trim(),
            )
            withContext(ioDispatcher) {
                userRepository.updateUser(updatedUser)
            }.fold(
                onSuccess = {
                    _uiState.update { it.copy(isEditMode = false) }
                    _uiEvents.emit(ProfileEvent.ShowSnackbar(MSG_PROFILE_UPDATED))
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    _uiEvents.emit(
                        ProfileEvent.ShowError(error.message ?: ERR_FAILED_UPDATE_PROFILE)
                    )
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiEvents.emit(
                ProfileEvent.ShowError(e.message ?: ERR_FAILED_UPDATE_PROFILE)
            )
        }
    }

    fun onDeleteAddress(addressId: String) = viewModelScope.launch {
        try {
            withContext(ioDispatcher) {
                userRepository.deleteAddress(addressId)
            }
            _uiEvents.emit(ProfileEvent.ShowSnackbar(MSG_ADDRESS_REMOVED))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiEvents.emit(
                ProfileEvent.ShowError(e.message ?: ERR_FAILED_REMOVE_ADDRESS)
            )
        }
    }

    fun onLogout() = viewModelScope.launch {
        try {
            withContext(ioDispatcher) {
                userRepository.logout()
            }
            _navigationEvents.emit(ProfileEvent.NavigateToLogin)
        } catch (e: CancellationException) {
            throw e   // rethrow — structured concurrency
        } catch (e: Exception) {
            _uiEvents.emit(
                ProfileEvent.ShowError(e.message ?: ERR_LOGOUT_FAILED)
            )
        }
    }

    fun onBackPressed() = viewModelScope.launch {
        _navigationEvents.emit(ProfileEvent.NavigateBack)
    }
}