package com.swapna.foodapp.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val AppFontFamily = FontFamily.Default

val FoodAppTypography = Typography(

// ── Display — large screen titles ─────────────────────────
// Figma: "32 / Bold / -0.5 letter spacing"
displayLarge = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Bold,
fontSize      = 32.sp,
lineHeight    = 40.sp,
letterSpacing = (-0.5).sp,
),

// Figma: "28 / Bold"
displayMedium = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Bold,
fontSize      = 28.sp,
lineHeight    = 36.sp,
letterSpacing = 0.sp,
),

// Figma: "24 / Bold" — screen title e.g. "Login"
displaySmall = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Bold,
fontSize      = 24.sp,
lineHeight    = 32.sp,
letterSpacing = 0.sp,
),

// ── Headline — section headers ─────────────────────────────
// Figma: "22 / SemiBold" — e.g. filter sheet title
headlineLarge = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.SemiBold,
fontSize      = 22.sp,
lineHeight    = 28.sp,
letterSpacing = 0.sp,
),

// Figma: "20 / SemiBold" — e.g. "Select Delivery Location"
headlineMedium = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.SemiBold,
fontSize      = 20.sp,
lineHeight    = 28.sp,
letterSpacing = 0.sp,
),

// Figma: "18 / SemiBold" — e.g. section headers
headlineSmall = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.SemiBold,
fontSize      = 18.sp,
lineHeight    = 24.sp,
letterSpacing = 0.sp,
),

// ── Title — card titles, list titles ──────────────────────
// Figma: "17 / Bold" — restaurant card name
titleLarge = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Bold,
fontSize      = 17.sp,
lineHeight    = 24.sp,
letterSpacing = 0.sp,
),

// Figma: "15 / SemiBold" — e.g. menu item name
titleMedium = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.SemiBold,
fontSize      = 15.sp,
lineHeight    = 22.sp,
letterSpacing = 0.1.sp,
),

// Figma: "13 / Medium" — e.g. chip labels
titleSmall = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Medium,
fontSize      = 13.sp,
lineHeight    = 20.sp,
letterSpacing = 0.1.sp,
),

// ── Body — main content ────────────────────────────────────
// Figma: "16 / Regular" — descriptions, addresses
bodyLarge = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Normal,
fontSize      = 16.sp,
lineHeight    = 24.sp,
letterSpacing = 0.5.sp,
),

// Figma: "14 / Regular" — secondary info
bodyMedium = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Normal,
fontSize      = 14.sp,
lineHeight    = 20.sp,
letterSpacing = 0.25.sp,
),

// Figma: "12 / Regular" — tertiary info, hints
bodySmall = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Normal,
fontSize      = 12.sp,
lineHeight    = 16.sp,
letterSpacing = 0.4.sp,
),

// ── Label — chips, badges, tags ───────────────────────────
// Figma: "14 / Medium" — button text, chip labels
labelLarge = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Medium,
fontSize      = 14.sp,
lineHeight    = 20.sp,
letterSpacing = 0.1.sp,
),

// Figma: "12 / Medium" — filter chips, small labels
labelMedium = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Medium,
fontSize      = 12.sp,
lineHeight    = 16.sp,
letterSpacing = 0.5.sp,
),

// Figma: "10 / Medium" — badge text, tiny labels
labelSmall = TextStyle(
fontFamily    = AppFontFamily,
fontWeight    = FontWeight.Medium,
fontSize      = 10.sp,
lineHeight    = 14.sp,
letterSpacing = 0.5.sp,
),
)
