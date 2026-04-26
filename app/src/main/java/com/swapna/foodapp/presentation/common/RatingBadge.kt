package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.RatingExcellent
import com.swapna.foodapp.presentation.ui.theme.RatingGood
import com.swapna.foodapp.presentation.ui.theme.RatingVeryGood

@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        rating >= AppBusinessRules.RATING_EXCELLENT -> RatingExcellent
        rating >= AppBusinessRules.RATING_VERY_GOOD -> RatingVeryGood
        rating >= AppBusinessRules.RATING_GOOD -> RatingGood
        else -> RatingGood
    }

    Row(
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(Dimens.RadiusXS),
            )
            .padding(
                horizontal = Dimens.RatingBadgePaddingH,
                vertical = Dimens.RatingBadgePaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(Dimens.RatingIconSize),
        )
        Spacer(Modifier.width(Dimens.RatingBadge))
        Text(
            text = String.format("%.1f", rating),
            color = Color.White,
            fontSize = Dimens.RatingFontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        )
    }
}