package com.swapna.foodapp.presentation.restaurant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

// Transparent top bar overlaid on hero image
// Shows: back, share, search, cart

@Composable
fun RestaurantTopBar(
    cartItemCount: Int,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.SpaceS,
                vertical   = Dimens.SpaceM,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Back button — circular white bg ───────────────────
        CircleIconButton(
            onClick = onBack,
            icon    = Icons.AutoMirrored.Filled.ArrowBack,
            desc    = "Back",
        )

        Spacer(Modifier.weight(1f))

        // ── Action buttons ────────────────────────────────────
        CircleIconButton(
            onClick = onSearch,
            icon    = Icons.Default.Search,
            desc    = "Search menu",
        )

        Spacer(Modifier.width(Dimens.SpaceS))

        CircleIconButton(
            onClick = onShare,
            icon    = Icons.Default.Share,
            desc    = "Share restaurant",
        )

        Spacer(Modifier.width(Dimens.SpaceS))

        // Cart with badge
        BadgedBox(
            badge = {
                if (cartItemCount > 0) {
                    Badge(containerColor = ZomatoRed) {
                        Text(
                            text  = cartItemCount.toString(),
                            color = AppWhite,
                        )
                    }
                }
            },
        ) {
            CircleIconButton(
                onClick = onCart,
                icon    = Icons.Default.ShoppingCart,
                desc    = "Cart",
            )
        }
    }
}

// ── Reusable: white circle background icon button ─────────────
@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
) {
    Box(
        modifier = Modifier
            .size(Dimens.MinTouchTarget)
            .background(
                color = Color.White.copy(alpha = 0.9f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector        = icon,
                contentDescription = desc,
                tint               = Color.Black,
                modifier           = Modifier.size(Dimens.IconM),
            )
        }
    }
}