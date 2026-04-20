package com.swapna.foodapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.EVENT_BUFFER_DEFAULT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // ── UI State ──────────────────────────────────────────────
    data class ProfileUiState(
        val isLoading:  Boolean      = true,
        val user:       User?        = null,
        val orders:     List<Order>  = emptyList(),
        val isEditMode: Boolean      = false,
        val editName:   String       = "",
        val editEmail:  String       = "",
        val error:      String?      = null,
    ) {
        // ── Computed properties ───────────────────────────────
        // WHY computed not stored?
        // Always derived from user object
        // Single source of truth — no sync needed

        // Phone from Firebase Auth — always real OTP number
        // WHY Firebase not Room?
        // User cannot change phone number in app
        // Firebase Auth = authoritative source for phone
        val phoneNumber: String
            get() = user?.phone
                ?.ifEmpty {
                    // Fallback to Firebase if Room phone empty
                    FirebaseAuth.getInstance()
                        .currentUser
                        ?.phoneNumber
                        ?: ""
                }
                ?: ""

        val displayName: String
            get() = user?.name
                ?.ifEmpty { "Add your name" }
                ?: "Add your name"

        val displayEmail: String
            get() = user?.email
                ?.ifEmpty { "Add email address" }
                ?: "Add email address"

        val addresses: List<Address>
            get() = user?.addresses ?: emptyList()

        val hasAddresses: Boolean
            get() = addresses.isNotEmpty()

        val isLoggedIn: Boolean
            get() = user != null
    }

    // ── Events ────────────────────────────────────────────────
    sealed class ProfileEvent {
        object NavigateToLogin                        : ProfileEvent()
        object NavigateBack                           : ProfileEvent()
        data class ShowSnackbar(val message: String)  : ProfileEvent()
        data class ShowError(val message: String)     : ProfileEvent()
    }

    // ── State + Events flows ──────────────────────────────────
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>(
        extraBufferCapacity = 5,
        onBufferOverflow    = BufferOverflow.SUSPEND,
    )
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadUserProfile()
        loadOrders()
    }

    // ── Load user profile ─────────────────────────────────────
    // WHY observe Flow not one-shot?
    // User edits name → Flow emits → UI updates automatically
    // No manual refresh needed after save
    // UserRepository.getCurrentUser() handles Firebase fallback
    // already — no need to duplicate logic here
    private fun loadUserProfile() = viewModelScope.launch {
        userRepository.getCurrentUser().collect { user ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user      = user,
                    // Pre-fill edit fields with current values
                    // So when user taps Edit → fields already filled
                    editName  = user?.name  ?: "",
                    editEmail = user?.email ?: "",
                    error     = if (user == null) {
                        "Could not load profile"
                    } else null,
                )
            }
        }
    }

    // ── Load recent orders ────────────────────────────────────
    // ✅ FIX: getRecentOrders() not getOrders()
    // UserRepository interface defines getRecentOrders()
    // Orders fetched from API — not Room for MVP
    private fun loadOrders() = viewModelScope.launch {
        userRepository.getRecentOrders().collect { orders ->
            _uiState.update { it.copy(orders = orders) }
        }
    }

    // ── Edit mode ─────────────────────────────────────────────
    fun onEditClicked() {
        val current = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditMode = true,
                // Pre-fill with current values
                // empty string if user has not set them yet
                editName   = current?.name  ?: "",
                editEmail  = current?.email ?: "",
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
        // Reset edit fields back to current user values
        val current = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditMode = false,
                editName   = current?.name  ?: "",
                editEmail  = current?.email ?: "",
            )
        }
    }

    // ── Save profile ──────────────────────────────────────────
    // ✅ FIX: updateUser(user) not updateProfile(name, email)
    // UserRepository.updateUser() takes full User object
    // We copy current user and replace name + email
    fun onSaveProfile() = viewModelScope.launch {
        val state   = _uiState.value
        val current = state.user

        if (current == null) {
            _events.emit(
                ProfileEvent.ShowError("No user found. Please login again.")
            )
            return@launch
        }

        // Validation
        if (state.editName.isBlank()) {
            _events.emit(
                ProfileEvent.ShowError("Name cannot be empty")
            )
            return@launch
        }

        try {
            // ✅ FIX: Build updated User object
            // updateUser() takes User not (name, email) params
            val updatedUser = current.copy(
                name  = state.editName.trim(),
                email = state.editEmail.trim(),
            )

            // ✅ FIX: updateUser() returns Result<Unit>
            val result = userRepository.updateUser(updatedUser)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isEditMode = false) }
                    _events.emit(
                        ProfileEvent.ShowSnackbar(
                            "Profile updated ✅"
                        )
                    )
                },
                onFailure = { error ->
                    _events.emit(
                        ProfileEvent.ShowError(
                            error.message ?: "Failed to update profile"
                        )
                    )
                },
            )
        } catch (e: Exception) {
            _events.emit(
                ProfileEvent.ShowError(
                    e.message ?: "Failed to update profile"
                )
            )
        }
    }

    // ── Delete address ────────────────────────────────────────
    // Removes address from user's saved addresses list in Room
    fun onDeleteAddress(addressId: String) = viewModelScope.launch {
        try {
            userRepository.deleteAddress(addressId)
            _events.emit(
                ProfileEvent.ShowSnackbar("Address removed")
            )
        } catch (e: Exception) {
            _events.emit(
                ProfileEvent.ShowError(
                    e.message ?: "Failed to remove address"
                )
            )
        }
    }

    // ── Logout ────────────────────────────────────────────────
    // Signs out from Firebase + clears Room user data
    // Navigate to LoginScreen after logout
    fun onLogout() = viewModelScope.launch {
        try {
            userRepository.logout()
            // Navigate to login — popUpTo clears backstack
            // so user cannot press back to get to ProfileScreen
            _events.emit(ProfileEvent.NavigateToLogin)
        } catch (e: Exception) {
            _events.emit(
                ProfileEvent.ShowError(
                    e.message ?: "Logout failed. Please try again."
                )
            )
        }
    }

    // ── Navigation ────────────────────────────────────────────
    fun onBackPressed() = viewModelScope.launch {
        _events.emit(ProfileEvent.NavigateBack)
    }
}
