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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swapna.foodapp.presentation.ui.theme.AppWhite

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    // ✅ Observe navigateTo StateFlow
    val destination by viewModel.navigateTo
        .collectAsStateWithLifecycle()

    // ── Animations ────────────────────────────────────────────
    val logoScale    = remember { Animatable(0.3f) }
    val contentAlpha = remember { Animatable(0f) }

    // Logo bounce
    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue   = 1.0f,
            animationSpec = tween(500, easing = FastOutSlowInEasing),
        )
    }

    // Fade in
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing),
        )
    }

    // ── Navigation ────────────────────────────────────────────
    // ✅ LaunchedEffect(destination) fires ONLY when
    //    destination changes from null → non-null
    LaunchedEffect(destination) {
        destination ?: return@LaunchedEffect // null = still loading

        val route = when (destination) {
            SplashViewModel.SplashDestination.Home  -> AppRoutes.HOME
            SplashViewModel.SplashDestination.Login -> AppRoutes.LOGIN
            null -> return@LaunchedEffect
        }

        navController.navigate(route) {
            // Remove splash from back stack
            // Back press on Home/Login exits app
            popUpTo(AppRoutes.SPLASH) { inclusive = true }
        }
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(ZomatoRed),
        contentAlignment = Alignment.Center,
    ) {
        // Content — logo + text
        Column(
            modifier            = Modifier.alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🍔", fontSize = 56.sp)
            }

            Spacer(Modifier.height(24.dp))

            // App name
            Text(
                text          = "FoodApp",
                color         = AppWhite,
                fontSize      = 32.sp,
                fontWeight    = FontWeight.Bold,
                textAlign     = TextAlign.Center,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                text      = "Order food you love",
                color     = AppWhite.copy(alpha = 0.8f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
            )
        }

        // Loading dots — bottom center
        LoadingDots(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Animated loading dots ─────────────────────────────────────
@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color       = Color.White.copy(alpha = 0.6f),
) {
    val dot1 = remember { Animatable(0.3f) }
    val dot2 = remember { Animatable(0.3f) }
    val dot3 = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        while (true) {
            dot1.animateTo(1f, tween(300))
            dot1.animateTo(0.3f, tween(300))
            dot2.animateTo(1f, tween(300))
            dot2.animateTo(0.3f, tween(300))
            dot3.animateTo(1f, tween(300))
            dot3.animateTo(0.3f, tween(300))
        }
    }

    Row(
        modifier              = modifier.height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        listOf(dot1, dot2, dot3).forEach { dot ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dot.value)
                    .background(color, CircleShape),
            )
        }
    }
}