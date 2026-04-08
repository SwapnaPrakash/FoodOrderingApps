package com.swapna.foodapp.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZomatoRed),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = "🍔",
            fontSize   = 72.sp,
        )
    }

    LaunchedEffect(Unit) {
        delay(1500)
        val destination = if (viewModel.isLoggedIn()) {
            AppRoutes.HOME
        } else {
            AppRoutes.LOGIN
        }
        navController.navigate(destination) {
            popUpTo(AppRoutes.SPLASH) { inclusive = true }
        }
    }
}