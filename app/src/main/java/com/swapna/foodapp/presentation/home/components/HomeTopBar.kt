package com.swapna.foodapp.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun HomeTopBar(
    location: String,
    cartItemCount: Int,
    onLocationClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppWhite)
            .padding(
                horizontal = Dimens.SpaceL,
                vertical   = Dimens.SpaceM,
            ),
    ) {
        // ── Row 1: Location + Cart Icon ───────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Location section — tappable to change address
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocationClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(Dimens.IconM),
                )
                Spacer(Modifier.width(Dimens.SpaceXS))
                Column {
                    Text(
                        text       = "Delivering to",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color      = AppGray,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = location,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines   = 1,
                        )
                        Icon(
                            imageVector        = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Change location",
                            modifier           = Modifier.size(Dimens.IconM),
                        )
                    }
                }
            }

            // Cart icon with badge
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge(containerColor = ZomatoRed) {
                            Text(
                                text     = cartItemCount.toString(),
                                fontSize = 10.sp,
                            )
                        }
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = "Cart"
                },
            ) {
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector        = Icons.Default.ShoppingCart,
                        contentDescription = "Go to cart",
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpaceM))

        // ── Row 2: Search Bar ─────────────────────────────────
        // readOnly = true — tap navigates to Search screen
        // Not a real text field — just looks like one (Figma pattern)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSearchClick() } // ✅ Works perfectly
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = {
                    Text(
                        text = "Search for restaurants and food",
                        color = AppGray,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AppGray,
                    )
                },
                readOnly = true,
                singleLine = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = AppDivider,
                    disabledContainerColor = Color(0xFFF8F8F8),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        HorizontalDivider(color = AppDivider)
    }
}