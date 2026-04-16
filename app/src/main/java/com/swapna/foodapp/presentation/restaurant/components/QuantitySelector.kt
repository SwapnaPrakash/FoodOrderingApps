package com.swapna.foodapp.presentation.restaurant.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.presentation.ui.theme.VegGreen

// WHY QuantitySelector as separate component?
// Used in: RestaurantScreen MenuItem rows
// Used in: CartScreen item list
// Single component = consistent UX everywhere
// Change once → updates both screens

@Composable
fun QuantitySelector(
    quantity:    Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier:    Modifier = Modifier,
    color:       Color    = VegGreen,
) {
    Row(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(8.dp),
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        // ── Minus Button ──────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(32.dp)
                .clickable { onDecrement() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Remove,
                contentDescription = "Remove",
                tint               = color,
                modifier           = Modifier.size(16.dp),
            )
        }

        // ── Quantity Number ───────────────────────────────────
        // AnimatedContent = number slides up/down when changed
        // Shows count change is happening
        AnimatedContent(
            targetState   = quantity,
            transitionSpec = {
                if (targetState > initialState) {
                    // Incrementing → slide up
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                } else {
                    // Decrementing → slide down
                    slideInVertically { -it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut()
                }
            },
            label = "quantity",
        ) { qty ->
            Text(
                text       = qty.toString(),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = color,
                modifier   = Modifier
                    .width(28.dp)
                    .padding(horizontal = 4.dp),
            )
        }

        // ── Plus Button ───────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(32.dp)
                .clickable { onIncrement() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "Add",
                tint               = color,
                modifier           = Modifier.size(16.dp),
            )
        }
    }
}