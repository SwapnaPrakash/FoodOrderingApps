package com.swapna.foodapp.presentation.restaurant.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.presentation.common.VegBadge
import com.swapna.foodapp.presentation.ui.theme.AddButtonBorder
import com.swapna.foodapp.presentation.ui.theme.AddButtonText
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.RecommendedCardBg

// ── Why a separate Composable? ────────────────────────────────
// RecommendedSection is a complex UI block with:
//   - LazyRow for horizontal scroll
//   - Each card has image, veg badge, name, price, add button
// Keeping it separate = RestaurantScreen stays clean and readable

@Composable
fun RecommendedSection(
    items: List<MenuItem>,
    onItemTap: (String) -> Unit,     // navigate to Product Detail
    onAddTap: (MenuItem) -> Unit,    // quick add to cart
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        // ── Section header ─────────────────────────────────────
        // "Recommended ⭐" title with item count
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text       = "Recommended",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(Dimens.SpaceXS))
            Text(
                text  = "⭐",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.weight(1f))
            // Item count in gray
            Text(
                text  = "${items.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
        }

        // ── Horizontal scroll row ──────────────────────────────
        // key = { it.id } prevents recomposition when list order changes
        LazyRow(
            contentPadding        = PaddingValues(horizontal = Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            items(
                items = items,
                key   = { it.id },  // ✅ stable key for Compose
            ) { menuItem ->
                RecommendedItemCard(
                    item     = menuItem,
                    onTap    = { onItemTap(menuItem.id) },
                    onAddTap = { onAddTap(menuItem) },
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceM))
    }
}

// ── Individual Recommended Card ────────────────────────────────
// Figma: 160dp wide, image on top, info below, + button bottom right
@Composable
private fun RecommendedItemCard(
    item: MenuItem,
    onTap: () -> Unit,
    onAddTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier      = modifier
            .width(Dimens.RecommendedCardWidth)      // 160dp
            .clickable { onTap() },
        color         = RecommendedCardBg,
        shape         = RoundedCornerShape(Dimens.RadiusM),
        shadowElevation = Dimens.ElevationS,
        border        = androidx.compose.foundation.BorderStroke(
            width = Dimens.BorderThin,
            color = Color(0xFFEEEEEE),
        ),
    ) {
        Column {

            // ── Food Image ─────────────────────────────────────
            // Box allows veg badge to overlay on top-left corner
            Box {
                AsyncImage(
                    model              = item.imageUrl.ifEmpty {
                        // Fallback if no image in API
                        "https://picsum.photos/seed/${item.id}/160/120"
                    },
                    contentDescription = item.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = Dimens.RadiusM,
                                topEnd   = Dimens.RadiusM,
                                bottomStart = 0.dp,
                                bottomEnd   = 0.dp,
                            )
                        ),
                )

                // Veg/Non-veg badge — top-left corner
                VegBadge(
                    isVeg    = item.isVeg,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Dimens.SpaceXS),
                )
            }

            // ── Item Info ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.SpaceS,
                        vertical   = Dimens.SpaceXS,
                    ),
            ) {
                // Item name — max 2 lines
                Text(
                    text       = item.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(Dimens.SpaceXXS))

                // Price + Add button row
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Price: ₹249
                    Text(
                        text       = "₹${item.price.toInt()}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )

                    // ADD button — outlined red + sign
                    // Why OutlinedButton? Figma shows border-only style for + button
                    AddButton(onTap = onAddTap)
                }
            }
        }
    }
}

// ── ADD Button ─────────────────────────────────────────────────
// Small outlined button with + icon
// Figma: white bg, red border, red "+" text
@Composable
fun AddButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick      = onTap,
        modifier     = modifier.height(Dimens.SmallButtonHeight),
        shape        = RoundedCornerShape(Dimens.RadiusS),
        contentPadding = PaddingValues(
            horizontal = Dimens.SpaceM,
            vertical   = Dimens.SpaceXS,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor   = AddButtonText,   // ZomatoRed
        ),
        border = BorderStroke(
            width = Dimens.BorderNormal,
            color = AddButtonBorder,          // ZomatoRed
        ),
    ) {
        Icon(
            imageVector        = Icons.Default.Add,
            contentDescription = "Add to cart",
            tint               = AddButtonText,
            modifier           = Modifier.size(Dimens.IconS),
        )
        Spacer(Modifier.width(Dimens.SpaceXXS))
        Text(
            text       = "ADD",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = AddButtonText,
        )
    }
}