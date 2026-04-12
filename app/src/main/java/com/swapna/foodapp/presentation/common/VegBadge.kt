package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.NonVegRed
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.utils.AppConstants.NON_VEG
import com.swapna.foodapp.utils.AppConstants.VEG

@Composable
fun VegBadge(
    isVeg: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (isVeg) VegGreen else NonVegRed

    Box(
        modifier = modifier
            .size(Dimens.VegBadgeSize)
            .border(
                width = Dimens.BorderNormal,
                color = color,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = if (isVeg) VEG else NON_VEG,
            tint = color,
            modifier = Modifier.size(Dimens.VegDotSize),
        )
    }
}