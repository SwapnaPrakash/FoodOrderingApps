package com.swapna.foodapp.presentation.splash

import androidx.lifecycle.ViewModel
import com.swapna.foodapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    fun isLoggedIn(): Boolean = userRepository.isLoggedIn()

}