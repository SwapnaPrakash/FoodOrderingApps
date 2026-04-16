package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.presentation.restaurant.components.QuantitySelector
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

// WHY QuantitySelector reused here?
// Same component as RestaurantScreen menu items
// Consistent UX — user already knows how it works
// Change once → updates both screens

@Composable
fun CartItemRow(
    cartItem:    CartItem,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    modifier:    Modifier = Modifier,
) {
    val item = cartItem.menuItem

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // ── Left: Veg indicator + name + price ────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.SpaceM),
            ) {
                // Veg / Non-veg dot
                VegDot(isVeg = item.isVeg)

                Spacer(Modifier.height(Dimens.SpaceXS))

                Text(
                    text       = item.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )

                // Show selected customisations if any
                if (cartItem.selectedCustomisations.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = cartItem.selectedCustomisations
                            .joinToString(", ") { it.label },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(Dimens.SpaceXS))

                // Item total price (qty × price + extras)
                Text(
                    text       = "₹${cartItem.totalPrice.toInt()}",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            // ── Right: Image + QuantitySelector ──────────────
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
                            .size(80.dp)
                            .clip(RoundedCornerShape(
                                Dimens.RadiusM
                            )),
                    )

                    // Quantity selector — reused from Day 19
                    QuantitySelector(
                        quantity    = cartItem.quantity,
                        onIncrement = { onIncrement(cartItem) },
                        onDecrement = { onDecrement(cartItem) },
                    )
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

// ── Veg indicator dot ─────────────────────────────────────────
@Composable
private fun VegDot(isVeg: Boolean) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .border(
                width = 1.5.dp,
                color = if (isVeg) VegGreen else ZomatoRed,
                shape = RoundedCornerShape(2.dp),
            )
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (isVeg) VegGreen else ZomatoRed,
                    shape = RoundedCornerShape(50.dp),
                ),
        )
    }
}