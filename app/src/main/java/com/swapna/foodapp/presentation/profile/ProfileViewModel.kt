package com.swapna.foodapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

// WHY separate ProfileViewModel?
// Single Responsibility:
//   ProfileViewModel → user data display + edit + logout
//   Not mixed with auth or navigation logic
// Clean separation = easy to test + maintain

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // ══════════════════════════════════════════════════════════
    // UI STATE
    // Everything the screen needs in one place
    // ══════════════════════════════════════════════════════════
    data class ProfileUiState(
        val user:          User?    = null,

        // Edit mode fields
        // WHY separate edit fields from user?
        // User taps edit → fills fields
        // Taps cancel → original user unchanged
        // Taps save → user updated from fields
        val editName:      String   = "",
        val editEmail:     String   = "",

        // Validation errors shown below fields
        val nameError:     String?  = null,
        val emailError:    String?  = null,

        // Controls ModalBottomSheet visibility
        val showEditSheet: Boolean  = false,

        val isLoading:     Boolean  = true,
        val isSaving:      Boolean  = false,
        val error:         String?  = null,
    )

    // ══════════════════════════════════════════════════════════
    // EVENTS — one-time navigation/feedback
    // ══════════════════════════════════════════════════════════
    sealed class ProfileEvent {
        object NavigateToLogin      : ProfileEvent()
        object NavigateToOrders     : ProfileEvent()
        object NavigateToAddresses  : ProfileEvent()
        object NavigateToPayments   : ProfileEvent()
        object NavigateToSettings   : ProfileEvent()
        data class ShowSnackbar(
            val message: String,
        )                           : ProfileEvent()
        data class ShowError(
            val message: String,
        )                           : ProfileEvent()
    }

    private val _uiState =
        MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> =
        _uiState.asStateFlow()

    private val _events =
        MutableSharedFlow<ProfileEvent>(
            replay              = 0,
            extraBufferCapacity = EVENT_BUFFER_DEFAULT,
            onBufferOverflow    = BufferOverflow.DROP_OLDEST,
        )
    val events: SharedFlow<ProfileEvent> =
        _events.asSharedFlow()

    init {
        loadUser()
    }

    // ══════════════════════════════════════════════════════════
    // LOAD USER
    // Fetches user from DataStore/Repository on screen open
    // ══════════════════════════════════════════════════════════
    private fun loadUser() = viewModelScope.launch {
        // getUser() returns Result<User>
        // fold = handle success + failure in one call
        userRepository.getUser().fold(
            onSuccess = { user ->
                _uiState.update {
                    it.copy(
                        user      = user,
                        isLoading = false,
                        // Pre-fill edit fields with current values
                        // WHY? When user opens edit sheet
                        // they see current values already filled
                        editName  = user.name,
                        editEmail = user.email,
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error     = error.message
                            ?: "Failed to load profile",
                    )
                }
            }
        )
    }

    // ══════════════════════════════════════════════════════════
    // EDIT PROFILE SHEET
    // ══════════════════════════════════════════════════════════

    // User taps "Edit Profile" button
    // Opens ModalBottomSheet
    fun onEditProfileTapped() {
        val user = _uiState.value.user ?: return
        _uiState.update {
            it.copy(
                // Pre-fill with current user data
                editName      = user.name,
                editEmail     = user.email,
                // Clear old errors
                nameError     = null,
                emailError    = null,
                // Show sheet
                showEditSheet = true,
            )
        }
    }

    // Called on every keystroke in Name field
    // WHY update state on every keystroke?
    // Enables real-time validation + button enable/disable
    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                editName  = name,
                // Clear error as user types
                // Better UX than showing error while typing
                nameError = null,
            )
        }
    }

    // Called on every keystroke in Email field
    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                editEmail  = email,
                emailError = null,
            )
        }
    }

    // User taps Save in edit sheet
    fun onSaveProfile() = viewModelScope.launch {
        val state = _uiState.value

        // ── Validate BEFORE calling repository ────────────────
        // WHY validate in ViewModel not repository?
        // Immediate feedback — no network call needed
        // Repository should not know about UI validation
        val nameError  = validateName(state.editName)
        val emailError = validateEmail(state.editEmail)

        // If either invalid → show errors, don't save
        if (nameError != null || emailError != null) {
            _uiState.update {
                it.copy(
                    nameError  = nameError,
                    emailError = emailError,
                )
            }
            return@launch
        }

        // ── Save to repository ────────────────────────────────
        _uiState.update { it.copy(isSaving = true) }

        val updatedUser = state.user!!.copy(
            name  = state.editName.trim(),
            email = state.editEmail.trim(),
        )

        userRepository.updateUser(updatedUser).fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        user          = updatedUser,
                        isSaving      = false,
                        showEditSheet = false, // close sheet
                    )
                }
                _events.emit(
                    ProfileEvent.ShowSnackbar(
                        "Profile updated successfully"
                    )
                )
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(isSaving = false)
                }
                _events.emit(
                    ProfileEvent.ShowError(
                        error.message ?: "Failed to update profile"
                    )
                )
            }
        )
    }

    // User taps X or swipes down edit sheet
    fun onDismissEditSheet() {
        _uiState.update {
            it.copy(
                showEditSheet = false,
                nameError     = null,
                emailError    = null,
            )
        }
    }

    // ── Validation helpers ────────────────────────────────────
    // WHY private? Only ViewModel uses these
    // Returns error String or null if valid

    private fun validateName(name: String): String? {
        if (name.isBlank()) return "Name cannot be empty"
        if (name.trim().length < 2)
            return "Name must be at least 2 characters"
        return null // valid
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty"
        // Simple email check — contains @ and .
        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            return "Enter a valid email address"
        }
        return null // valid
    }

    // ══════════════════════════════════════════════════════════
    // LOGOUT
    // ══════════════════════════════════════════════════════════
    fun onLogout() = viewModelScope.launch {
        // Clear user session from DataStore
        userRepository.logout()
        // Navigate to login — remove all back stack
        _events.emit(ProfileEvent.NavigateToLogin)
    }

    // ══════════════════════════════════════════════════════════
    // MENU NAVIGATION
    // ══════════════════════════════════════════════════════════
    fun onOrdersTapped() = viewModelScope.launch {
        _events.emit(ProfileEvent.NavigateToOrders)
    }

    fun onAddressesTapped() = viewModelScope.launch {
        _events.emit(ProfileEvent.NavigateToAddresses)
    }

    fun onPaymentsTapped() = viewModelScope.launch {
        _events.emit(ProfileEvent.NavigateToPayments)
    }

    fun onSettingsTapped() = viewModelScope.launch {
        _events.emit(ProfileEvent.NavigateToSettings)
    }
}