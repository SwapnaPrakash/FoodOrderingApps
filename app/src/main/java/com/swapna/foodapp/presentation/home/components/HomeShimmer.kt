package com.swapna.foodapp.presentation.home.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.presentation.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import com.swapna.foodapp.presentation.ui.theme.AppAnimations
import com.swapna.foodapp.presentation.ui.theme.ShimmerBase
import com.swapna.foodapp.presentation.ui.theme.ShimmerHighlight

@Composable
fun HomeShimmer(paddingValues: PaddingValues) {

    // Animated shimmer brush
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1000f,
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

    Column(modifier = Modifier.padding(paddingValues)) {

        // ── Section title shimmer ─────────────────────────────
        ShimmerBox(
            brush  = shimmerBrush,
            width  = 160.dp,
            height = Dimens.ShimmerTitleHeight,
            modifier = Modifier.padding(
                start  = Dimens.SpaceL,
                top    = Dimens.SpaceL,
                bottom = Dimens.SpaceS,
            ),
        )

        // ── Offer cards shimmer ───────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            repeat(2) {
                ShimmerBox(
                    brush  = shimmerBrush,
                    width  = Dimens.OfferCardWidth,
                    height = Dimens.OfferCardHeight,
                    radius = Dimens.RadiusM,
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceL))

        // ── Section title shimmer ─────────────────────────────
        ShimmerBox(
            brush  = shimmerBrush,
            width  = 200.dp,
            height = Dimens.ShimmerTitleHeight,
            modifier = Modifier.padding(
                start  = Dimens.SpaceL,
                bottom = Dimens.SpaceS,
            ),
        )

        // ── Category chips shimmer ────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            repeat(4) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    // Circular image placeholder
                    Box(
                        modifier = Modifier
                            .size(Dimens.CategoryChipImageSize) // 56dp
                            .clip(CircleShape)
                            .background(shimmerBrush),
                    )
                    Spacer(Modifier.height(Dimens.SpaceXS))
                    // Label placeholder
                    ShimmerBox(
                        brush  = shimmerBrush,
                        width  = 50.dp,
                        height = 12.dp,
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpaceL))

        // ── Section title shimmer ─────────────────────────────
        ShimmerBox(
            brush  = shimmerBrush,
            width  = 180.dp,
            height = Dimens.ShimmerTitleHeight,
            modifier = Modifier.padding(
                start  = Dimens.SpaceL,
                bottom = Dimens.SpaceS,
            ),
        )

        // ── Restaurant cards shimmer ──────────────────────────
        repeat(2) {
            Column(
                modifier = Modifier.padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceS,
                ),
            ) {
                // Banner image placeholder
                ShimmerBox(
                    brush    = shimmerBrush,
                    modifier = Modifier.fillMaxWidth(),
                    height   = Dimens.RestaurantCardHeight, // 180dp
                    radius   = Dimens.RadiusM,
                )

                Spacer(Modifier.height(Dimens.SpaceS))

                // Name + rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ShimmerBox(
                        brush  = shimmerBrush,
                        width  = 180.dp,
                        height = Dimens.ShimmerTitleHeight,
                    )
                    ShimmerBox(
                        brush  = shimmerBrush,
                        width  = 36.dp,
                        height = 20.dp,
                        radius = Dimens.RadiusXS,
                    )
                }

                Spacer(Modifier.height(Dimens.SpaceXS))

                // Cuisines placeholder
                ShimmerBox(
                    brush  = shimmerBrush,
                    width  = 140.dp,
                    height = Dimens.ShimmerSubtitleHeight,
                )

                Spacer(Modifier.height(Dimens.SpaceXS))

                // Delivery info placeholder
                ShimmerBox(
                    brush  = shimmerBrush,
                    width  = 120.dp,
                    height = Dimens.ShimmerSubtitleHeight,
                )
            }

            Spacer(Modifier.height(Dimens.SpaceM))
        }
    }
}

// ── Reusable shimmer box ───────────────────────────────────────
@Composable
fun ShimmerBox(
    brush: Brush,
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 20.dp,
    radius: Dp = Dimens.RadiusS,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(brush),
    )
}