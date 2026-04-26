package com.swapna.foodapp.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.swapna.foodapp.presentation.ui.theme.AppAnimations
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ShimmerBase
import com.swapna.foodapp.presentation.ui.theme.ShimmerHighlight

@Composable
fun RestaurantShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(AppAnimations.SHIMMER_DURATION_MS),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start  = Offset(translateX - 300f, 0f),
        end    = Offset(translateX, 0f),
    )

    Column {
        // Hero image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.RestaurantHeroHeight)
                .background(shimmerBrush),
        )

        // Info card placeholder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
        ) {
            // Name
            Box(
                modifier = Modifier
                    .width(Dimens.Space80 * 2)
                    .height(Dimens.ShimmerTitleHeight)
                    .clip(RoundedCornerShape(Dimens.RadiusS))
                    .background(shimmerBrush),
            )
            Spacer(Modifier.height(Dimens.SpaceS))
            // Cuisine
            Box(
                modifier = Modifier
                    .width(Dimens.Space80)
                    .height(Dimens.ShimmerSubtitleHeight)
                    .clip(RoundedCornerShape(Dimens.RadiusS))
                    .background(shimmerBrush),
            )
            Spacer(Modifier.height(Dimens.SpaceL))
            // Metrics row
            Row {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.Space48)
                            .padding(horizontal = Dimens.SpaceXS)
                            .clip(RoundedCornerShape(Dimens.RadiusS))
                            .background(shimmerBrush),
                    )
                }
            }
        }

        // Menu items placeholder
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceL),
            ) {
                Column(Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(Dimens.Space80 * 2)
                            .height(Dimens.ShimmerTitleHeight)
                            .clip(RoundedCornerShape(Dimens.RadiusS))
                            .background(shimmerBrush),
                    )
                    Spacer(Modifier.height(Dimens.SpaceS))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ShimmerSubtitleHeight)
                            .clip(RoundedCornerShape(Dimens.RadiusS))
                            .background(shimmerBrush),
                    )
                    Spacer(Modifier.height(Dimens.SpaceS))
                    Box(
                        modifier = Modifier
                            .width(Dimens.Space48)
                            .height(Dimens.ShimmerSubtitleHeight)
                            .clip(RoundedCornerShape(Dimens.RadiusS))
                            .background(shimmerBrush),
                    )
                }
                Spacer(Modifier.width(Dimens.SpaceM))
                Box(
                    modifier = Modifier
                        .size(Dimens.MenuItemImageSquare)
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(shimmerBrush),
                )
            }
        }
    }
}