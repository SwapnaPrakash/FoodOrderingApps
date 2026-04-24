package com.swapna.foodapp.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val FoodAppShapes = Shapes(

    // 4dp — very small: badges, tiny chips
    extraSmall = RoundedCornerShape(Dimens.RadiusXS),

    // 8dp — small: input fields, small cards
    small = RoundedCornerShape(Dimens.RadiusS),

    // 12dp — medium: most cards, bottom sheets
    medium = RoundedCornerShape(Dimens.RadiusM),

    // 16dp — large: offer cards, restaurant cards
    large = RoundedCornerShape(Dimens.RadiusL),

    // 24dp — extra large: modals, full panels
    extraLarge = RoundedCornerShape(Dimens.RadiusXL),
)