package com.swapna.foodapp.presentation.restaurant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants

// Why separate composable?
// Each menu item is a complex card.
// Keeping it separate = easy to test + reuse in cart.

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

@Composable
fun MenuItemRow(
    item:        MenuItem,
    quantity:    Int,             // ← how many in cart right now
    onIncrement: () -> Unit,      // ← + tapped
    onDecrement: () -> Unit,      // ← - tapped
    modifier:    Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.Top,
        ) {

            // ── Left: Item details ────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.SpaceM),
            ) {
                // Veg / Non-veg dot
                VegIndicator(isVeg = item.isVeg)

                Spacer(Modifier.height(4.dp))

                Text(
                    text       = item.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text       = "₹${item.price.toInt()}",
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (item.isBestseller) {
                    Spacer(Modifier.height(4.dp))
                    BestsellerBadge()
                }

                if (item.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = item.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = AppGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // ── Right: Image + ADD/Quantity button ────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {

                    // Item image
                    AsyncImage(
                        model              = item.imageUrl,
                        contentDescription = item.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusM)),
                    )

                    // ✅ KEY DAY 19 FEATURE:
                    // AnimatedContent switches between:
                    //   quantity == 0 → show ADD button
                    //   quantity > 0  → show QuantitySelector
                    AnimatedContent(
                        targetState   = quantity > 0,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "add_or_quantity",
                    ) { hasItems ->
                        if (hasItems) {
                            // Show +/- selector
                            QuantitySelector(
                                quantity    = quantity,
                                onIncrement = onIncrement,
                                onDecrement = onDecrement,
                            )
                        } else {
                            // Show plain ADD button
                            AddButton(
                                onClick = onIncrement,
                                enabled = item.isAvailable,
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color    = Color(0xFFF0F0F0),
            modifier = Modifier.padding(
                horizontal = Dimens.SpaceL
            ),
        )
    }
}

// ── Veg indicator ─────────────────────────────────────────────
@Composable
private fun VegIndicator(isVeg: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(
                width = 1.5.dp,
                color = if (isVeg) VegGreen else ZomatoRed,
                shape = RoundedCornerShape(2.dp),
            )
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isVeg) VegGreen else ZomatoRed,
                    shape = RoundedCornerShape(50.dp),
                ),
        )
    }
}

// ── Bestseller badge ──────────────────────────────────────────
@Composable
private fun BestsellerBadge() {
    Text(
        text       = "🏆 Bestseller",
        style      = MaterialTheme.typography.labelSmall,
        color      = Color(0xFF8B4513),
        fontWeight = FontWeight.Medium,
        modifier   = Modifier
            .background(
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}