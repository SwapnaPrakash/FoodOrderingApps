package com.swapna.foodapp.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val AppFontFamily = FontFamily.Default

val FoodAppTypography = Typography(

    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = Dimens.FontSize32,
        lineHeight = Dimens.LineHeight40,
        letterSpacing = Dimens.LetterSpacingNeg,
    ),

    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = Dimens.FontSize28,
        lineHeight = Dimens.LineHeight36,
        letterSpacing = Dimens.LetterSpacingNone,
    ),

    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = Dimens.FontSize24,
        lineHeight = Dimens.LineHeight32,
        letterSpacing = Dimens.LetterSpacingNone,
    ),

    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = Dimens.FontSize22,
        lineHeight = Dimens.LineHeight28,
        letterSpacing = Dimens.LetterSpacingNone,
    ),

    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = Dimens.FontSize20,
        lineHeight = Dimens.LineHeight28,
        letterSpacing = Dimens.LetterSpacingNone,
    ),

    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = Dimens.FontSize18,
        lineHeight = Dimens.LineHeight24,
        letterSpacing = Dimens.LetterSpacingNone,
    ),

    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = Dimens.FontSize17,
        lineHeight = Dimens.LineHeight24,
        letterSpacing = Dimens.LetterSpacingNone,
    ),

    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = Dimens.FontSize15,
        lineHeight = Dimens.LineHeight22,
        letterSpacing = Dimens.LetterSpacingXS,
    ),

    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = Dimens.FontSize13,
        lineHeight = Dimens.LineHeight20,
        letterSpacing = Dimens.LetterSpacingXS,
    ),

    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = Dimens.FontSize16,
        lineHeight = Dimens.LineHeight24,
        letterSpacing = Dimens.LetterSpacingL,
    ),

    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = Dimens.FontSize14,
        lineHeight = Dimens.LineHeight20,
        letterSpacing = Dimens.LetterSpacingS,
    ),

    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = Dimens.FontSize12,
        lineHeight = Dimens.LineHeight16,
        letterSpacing = Dimens.LetterSpacingM,
    ),

    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = Dimens.FontSize14,
        lineHeight = Dimens.LineHeight20,
        letterSpacing = Dimens.LetterSpacingXS,
    ),

    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = Dimens.FontSize12,
        lineHeight = Dimens.LineHeight16,
        letterSpacing = Dimens.LetterSpacingL,
    ),

    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = Dimens.FontSize10,
        lineHeight = Dimens.LineHeight14,
        letterSpacing = Dimens.LetterSpacingL,
    ),
)