package com.swapna.foodapp.presentation.restaurant.components

import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.CartPriceBreakdown

// WHY CartBottomBar as separate component?
// Shown at bottom of RestaurantScreen
// Slides up when cart has items
// Slides down when cart is empty
// AnimatedVisibility handles the animation

@Composable
fun CartBottomBar(
    itemCount:    Int,
    breakdown:    CartPriceBreakdown,
    onViewCart:   () -> Unit,
    modifier:     Modifier = Modifier,
) {
    // AnimatedVisibility — slides up when items added
    AnimatedVisibility(
        visible = itemCount > 0,
        enter   = fadeIn() + expandVertically(),
        exit    = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM)
                .navigationBarsPadding(), // respect device nav bar
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation    = 8.dp,
                        shape        = RoundedCornerShape(12.dp),
                        ambientColor = ZomatoRed.copy(alpha = 0.3f),
                    )
                    .background(
                        color = ZomatoRed,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { onViewCart() }
                    .padding(
                        horizontal = Dimens.SpaceL,
                        vertical   = Dimens.SpaceM,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {

                // ── Left: item count + total ───────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Item count badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical   = 2.dp,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = "$itemCount",
                            color      = AppWhite,
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text       = "  ${
                            if (itemCount == 1) "item" else "items"
                        }",
                        color      = AppWhite,
                        style      = MaterialTheme.typography.bodyMedium,
                    )

                    Text(
                        text  = "  •  ₹${breakdown.total.toInt()}",
                        color = AppWhite,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // ── Right: View Cart ───────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text       = "View Cart",
                        color      = AppWhite,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go to cart",
                        tint               = AppWhite,
                        modifier           = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}