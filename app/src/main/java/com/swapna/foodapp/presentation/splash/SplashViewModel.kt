package com.swapna.foodapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _navigateTo = MutableStateFlow<SplashDestination?>(null)
    val navigateTo: StateFlow<SplashDestination?> = _navigateTo.asStateFlow()

    init {
        checkLoginAndNavigate()
    }

    private fun checkLoginAndNavigate() = viewModelScope.launch {
        delay(AppConstants.SPLASH_DELAY_MS)
        _navigateTo.value = if (userRepository.isLoggedIn()) {
            SplashDestination.Home
        } else {
            SplashDestination.Login
        }
    }

    sealed class SplashDestination {
        object Home : SplashDestination()
        object Login : SplashDestination()
    }
}