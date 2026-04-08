package com.swapna.foodapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // ── State Definition ──────────────────────────────────────
    // Sealed class = only these exact states are possible
    // No magic booleans like isLoading + isError + isSuccess

    sealed class AuthState {
        // Initial state — nothing has happened yet
        object Idle : AuthState()

        // Spinner showing while API call is in flight
        object Loading : AuthState()

        // OTP was sent — phone is stored so UI can display it
        data class OtpSent(val phone: String) : AuthState()

        // Login complete — user object received
        data class Success(val user: User) : AuthState()

        // Something went wrong — message to show in UI
        data class Error(val message: String) : AuthState()
    }

    // ── StateFlow ─────────────────────────────────────────────
    // Private mutable — only ViewModel can change it
    // Public read-only — UI observes this
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // ── Send OTP ──────────────────────────────────────────────
    fun sendOtp(phone: String) {
        // Validate BEFORE making any API call
        if (!isValidPhone(phone)) {
            _state.value = AuthState.Error(
                "Enter a valid 10-digit phone number"
            )
            return   // Stop here — don't call repository
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading

            userRepository.sendOtp(phone)
                .onSuccess {
                    _state.value = AuthState.OtpSent(phone)
                }
                .onFailure { throwable ->
                    _state.value = AuthState.Error(
                        throwable.message ?: "Failed to send OTP"
                    )
                }
        }
    }

    // ── Verify OTP ────────────────────────────────────────────
    fun verifyOtp(otp: String) {
        // Validate length before API call
        if (otp.length != AppConstants.OTP_LENGTH) {
            _state.value = AuthState.Error(
                "Enter the ${AppConstants.OTP_LENGTH}-digit OTP"
            )
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading

            userRepository.verifyOtp(otp)
                .onSuccess { user ->
                    _state.value = AuthState.Success(user)
                }
                .onFailure { throwable ->
                    _state.value = AuthState.Error(
                        throwable.message ?: "Incorrect OTP. Try again."
                    )
                }
        }
    }

    // ── Reset ─────────────────────────────────────────────────
    // Called when user dismisses error or wants to restart flow
    fun resetState() {
        _state.value = AuthState.Idle
    }

    // ── Validation ────────────────────────────────────────────
    // Private — only ViewModel uses this
    private fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        if (phone.length != AppConstants.PHONE_LENGTH) return false
        if (!phone.all { it.isDigit() }) return false
        return true
    }
}