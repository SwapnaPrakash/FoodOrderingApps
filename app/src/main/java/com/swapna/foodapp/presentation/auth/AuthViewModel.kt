package com.swapna.foodapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.DIGIT_OTP
import com.swapna.foodapp.utils.AppConstants.ERROR_INCORRECT_OTP
import com.swapna.foodapp.utils.AppConstants.ERROR_INVALID_OTP
import com.swapna.foodapp.utils.AppConstants.ERROR_INVALID_PHONE
import com.swapna.foodapp.utils.AppConstants.ERROR_SEND_OTP_FAILED
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

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun sendOtp(phone: String) {
        if (!isValidPhone(phone)) {
            _state.value = AuthState.Error(ERROR_INVALID_PHONE)
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            userRepository.sendOtp(phone)
                .onSuccess { _state.value = AuthState.OtpSent(phone) }
                .onFailure { throwable ->
                    _state.value = AuthState.Error(
                        throwable.message ?: ERROR_SEND_OTP_FAILED
                    )
                }
        }
    }

    fun verifyOtp(otp: String) {
        if (otp.length != AppConstants.OTP_LENGTH) {
            _state.value = AuthState.Error(
                "$ERROR_INVALID_OTP ${AppConstants.OTP_LENGTH} $DIGIT_OTP"
            )
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            userRepository.verifyOtp(otp)
                .onSuccess { user -> _state.value = AuthState.Success(user) }
                .onFailure { throwable ->
                    _state.value = AuthState.Error(
                        throwable.message ?: ERROR_INCORRECT_OTP
                    )
                }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }

    private fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        if (phone.length != AppConstants.PHONE_LENGTH) return false
        if (!phone.all { it.isDigit() }) return false
        return true
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class OtpSent(val phone: String) : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}