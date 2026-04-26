package com.swapna.foodapp.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.swapna.foodapp.presentation.ui.theme.AppAnimations
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerBoxDefaultHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerBoxDefaultWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerCategoriesTitleWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerCategoryTextHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerCategoryTextWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerOffersTitleWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerRatingBadgeHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerRatingBadgeWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerRestaurantNameWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerRestaurantsTitleWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerSubtitle1Width
import com.swapna.foodapp.presentation.ui.theme.Dimens.ShimmerSubtitle2Width
import com.swapna.foodapp.presentation.ui.theme.ShimmerBase
import com.swapna.foodapp.presentation.ui.theme.ShimmerHighlight
import com.swapna.foodapp.utils.AppConstants.SHIMMER
import com.swapna.foodapp.utils.AppConstants.SHIMMER_GRADIENT_OFFSET
import com.swapna.foodapp.utils.AppConstants.SHIMMER_TRANSLATE_END
import com.swapna.foodapp.utils.AppConstants.SHIMMER_TRANSLATE_START
import com.swapna.foodapp.utils.AppConstants.SHIMMER_X

@Composable
fun HomeShimmer(paddingValues: PaddingValues) {

    val transition = rememberInfiniteTransition(label = SHIMMER)
    val translateX by transition.animateFloat(
        initialValue = SHIMMER_TRANSLATE_START,
        targetValue = SHIMMER_TRANSLATE_END,
        animationSpec = infiniteRepeatable(
            animation = tween(AppAnimations.SHIMMER_DURATION_MS),
            repeatMode = RepeatMode.Restart,
        ),
        label = SHIMMER_X,
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(translateX - SHIMMER_GRADIENT_OFFSET, 0f),
        end = Offset(translateX, 0f),
    )

    Column(modifier = Modifier.padding(paddingValues)) {

        ShimmerBox(
            brush = shimmerBrush,
            width = ShimmerOffersTitleWidth,
            height = Dimens.ShimmerTitleHeight,
            modifier = Modifier.padding(
                start = Dimens.SpaceL,
                top = Dimens.SpaceL,
                bottom = Dimens.SpaceS,
            ),
        )

        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            repeat(2) {
                ShimmerBox(
                    brush = shimmerBrush,
                    width = Dimens.OfferCardWidth,
                    height = Dimens.OfferCardHeight,
                    radius = Dimens.RadiusM,
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceL))

        ShimmerBox(
            brush = shimmerBrush,
            width = ShimmerCategoriesTitleWidth,
            height = Dimens.ShimmerTitleHeight,
            modifier = Modifier.padding(
                start = Dimens.SpaceL,
                bottom = Dimens.SpaceS,
            ),
        )

        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.CategoryChipImageSize)
                            .clip(CircleShape)
                            .background(shimmerBrush),
                    )
                    Spacer(Modifier.height(Dimens.SpaceXS))
                    ShimmerBox(
                        brush = shimmerBrush,
                        width = ShimmerCategoryTextWidth,
                        height = ShimmerCategoryTextHeight,
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpaceL))

        ShimmerBox(
            brush = shimmerBrush,
            width = ShimmerRestaurantsTitleWidth,
            height = Dimens.ShimmerTitleHeight,
            modifier = Modifier.padding(
                start = Dimens.SpaceL,
                bottom = Dimens.SpaceS,
            ),
        )

        repeat(2) {
            Column(
                modifier = Modifier.padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceS,
                ),
            ) {
                // Banner image
                ShimmerBox(
                    brush = shimmerBrush,
                    modifier = Modifier.fillMaxWidth(),
                    height = Dimens.RestaurantCardHeight,
                    radius = Dimens.RadiusM,
                )

                Spacer(Modifier.height(Dimens.SpaceS))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Restaurant name
                    ShimmerBox(
                        brush = shimmerBrush,
                        width = ShimmerRestaurantNameWidth,
                        height = Dimens.ShimmerTitleHeight,
                    )
                    ShimmerBox(
                        brush = shimmerBrush,
                        width = ShimmerRatingBadgeWidth,
                        height = ShimmerRatingBadgeHeight,
                        radius = Dimens.RadiusXS,
                    )
                }

                Spacer(Modifier.height(Dimens.SpaceXS))

                ShimmerBox(
                    brush = shimmerBrush,
                    width = ShimmerSubtitle1Width,
                    height = Dimens.ShimmerSubtitleHeight,
                )

                Spacer(Modifier.height(Dimens.SpaceXS))

                ShimmerBox(
                    brush = shimmerBrush,
                    width = ShimmerSubtitle2Width,
                    height = Dimens.ShimmerSubtitleHeight,
                )
            }

            Spacer(Modifier.height(Dimens.SpaceM))
        }
    }
}

@Composable
fun ShimmerBox(
    brush: Brush,
    modifier: Modifier = Modifier,
    width: Dp = ShimmerBoxDefaultWidth,
    height: Dp = ShimmerBoxDefaultHeight,
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