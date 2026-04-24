package com.swapna.foodapp.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FoodAppColorScheme = lightColorScheme(
    primary = ZomatoRed,
    onPrimary = AppWhite,
    primaryContainer = ZomatoRedLight,
    onPrimaryContainer = ZomatoRedDark,

    secondary = AppDarkGray,
    onSecondary = AppWhite,
    secondaryContainer = AppLightGray,
    onSecondaryContainer = AppBlack,

    background = AppBackground,
    onBackground = AppBlack,
    surface = AppWhite,
    onSurface = AppBlack,
    surfaceVariant = AppLightGray,
    onSurfaceVariant = AppGray,

    error = ErrorRed,
    onError = AppWhite,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed,

    outline = AppDivider,
    outlineVariant = AppLightGray,
)

@Composable
fun FoodAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FoodAppColorScheme,
        typography = FoodAppTypography,
        shapes = FoodAppShapes,
        content = content,
    )
}

