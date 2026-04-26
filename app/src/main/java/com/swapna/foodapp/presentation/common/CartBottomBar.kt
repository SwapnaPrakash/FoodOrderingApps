package com.swapna.foodapp.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartArrowPaddingStart
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartBadgeBgRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartBadgePaddingH
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartBadgePaddingV
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartBarRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartBarShadowElevation
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_CART_BADGE_BG
import com.swapna.foodapp.utils.AppConstants.ALPHA_CART_SHADOW
import com.swapna.foodapp.utils.AppConstants.BULLET_SEPARATOR
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.GO_TO_CART_DESC
import com.swapna.foodapp.utils.AppConstants.ITEM_COUNT_SPACE
import com.swapna.foodapp.utils.AppConstants.ITEM_PLURAL
import com.swapna.foodapp.utils.AppConstants.ITEM_SINGULAR
import com.swapna.foodapp.utils.AppConstants.VIEW_CART

@Composable
fun CartBottomBar(
    itemCount: Int,
    breakdown: CartPriceBreakdown,
    onViewCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = itemCount > 0,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = CartBarShadowElevation,
                        shape = RoundedCornerShape(CartBarRadius),
                        ambientColor = ZomatoRed.copy(alpha = ALPHA_CART_SHADOW),
                    )
                    .background(
                        color = ZomatoRed,
                        shape = RoundedCornerShape(CartBarRadius),
                    )
                    .clickable { onViewCart() }
                    .padding(
                        horizontal = Dimens.SpaceL,
                        vertical = Dimens.SpaceM,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AppWhite.copy(alpha = ALPHA_CART_BADGE_BG),
                                shape = RoundedCornerShape(CartBadgeBgRadius),
                            )
                            .padding(
                                horizontal = CartBadgePaddingH,
                                vertical = CartBadgePaddingV,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$itemCount",
                            color = AppWhite,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text = "$ITEM_COUNT_SPACE${
                            if (itemCount == 1)
                                ITEM_SINGULAR
                            else ITEM_PLURAL
                        }",
                        color = AppWhite,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Text(
                        text = "$BULLET_SEPARATOR$CURRENCY_SYMBOL${
                            breakdown.total.toInt()
                        }",
                        color = AppWhite,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = VIEW_CART,
                        color = AppWhite,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = GO_TO_CART_DESC,
                        tint = AppWhite,
                        modifier = Modifier.padding(start = CartArrowPaddingStart),
                    )
                }
            }
        }
    }
}