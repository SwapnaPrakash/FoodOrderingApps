package com.swapna.foodapp.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashAppNameSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashDotSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashDotsRowHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashDotsSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashLetterSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashLogoEmojiSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashLogoSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashNameSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashTaglineSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.SplashTaglineSpacing
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_SPLASH_DOTS
import com.swapna.foodapp.utils.AppConstants.ALPHA_SPLASH_DOT_MIN
import com.swapna.foodapp.utils.AppConstants.ALPHA_SPLASH_LOGO_BG
import com.swapna.foodapp.utils.AppConstants.ALPHA_SPLASH_TAGLINE
import com.swapna.foodapp.utils.AppConstants.APP_NAME
import com.swapna.foodapp.utils.AppConstants.EMOJI_BURGER
import com.swapna.foodapp.utils.AppConstants.SPLASH_DOT_ANIM_MS
import com.swapna.foodapp.utils.AppConstants.SPLASH_FADE_ANIM_MS
import com.swapna.foodapp.utils.AppConstants.SPLASH_LOGO_ANIM_MS
import com.swapna.foodapp.utils.AppConstants.TAG_NAME

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.navigateTo.collectAsStateWithLifecycle()

    val logoScale = remember { Animatable(ALPHA_SPLASH_DOT_MIN) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(SPLASH_LOGO_ANIM_MS, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(SPLASH_FADE_ANIM_MS, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(destination) {
        destination ?: return@LaunchedEffect
        val route = when (destination) {
            SplashViewModel.SplashDestination.Home -> AppRoutes.HOME
            SplashViewModel.SplashDestination.Login -> AppRoutes.LOGIN
            null -> return@LaunchedEffect
        }
        navController.navigate(route) {
            popUpTo(AppRoutes.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZomatoRed),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(SplashLogoSize)
                    .scale(logoScale.value)
                    .background(
                        color = AppWhite.copy(alpha = ALPHA_SPLASH_LOGO_BG),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = EMOJI_BURGER,
                    fontSize = SplashLogoEmojiSize,
                )
            }

            Spacer(Modifier.height(SplashNameSpacing))

            Text(
                text = APP_NAME,
                color = AppWhite,
                fontSize = SplashAppNameSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = SplashLetterSpacing,
            )

            Spacer(Modifier.height(SplashTaglineSpacing))

            Text(
                text = TAG_NAME,
                color = AppWhite.copy(alpha = ALPHA_SPLASH_TAGLINE),
                fontSize = SplashTaglineSize,
                textAlign = TextAlign.Center,
            )
        }

        LoadingDots(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = AppWhite.copy(alpha = ALPHA_SPLASH_DOTS),
) {
    val dot1 = remember { Animatable(ALPHA_SPLASH_DOT_MIN) }
    val dot2 = remember { Animatable(ALPHA_SPLASH_DOT_MIN) }
    val dot3 = remember { Animatable(ALPHA_SPLASH_DOT_MIN) }

    LaunchedEffect(Unit) {
        while (true) {
            dot1.animateTo(1f, tween(SPLASH_DOT_ANIM_MS))
            dot1.animateTo(ALPHA_SPLASH_DOT_MIN, tween(SPLASH_DOT_ANIM_MS))
            dot2.animateTo(1f, tween(SPLASH_DOT_ANIM_MS))
            dot2.animateTo(ALPHA_SPLASH_DOT_MIN, tween(SPLASH_DOT_ANIM_MS))
            dot3.animateTo(1f, tween(SPLASH_DOT_ANIM_MS))
            dot3.animateTo(ALPHA_SPLASH_DOT_MIN, tween(SPLASH_DOT_ANIM_MS))
        }
    }

    Row(
        modifier = modifier.height(SplashDotsRowHeight),
        horizontalArrangement = Arrangement.spacedBy(SplashDotsSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(dot1, dot2, dot3).forEach { dot ->
            Box(
                modifier = Modifier
                    .size(SplashDotSize)
                    .alpha(dot.value)
                    .background(color, CircleShape),
            )
        }
    }
}