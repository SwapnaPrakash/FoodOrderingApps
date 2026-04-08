package com.swapna.foodapp.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppLightGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

// Predefined location options for quick selection
private data class QuickLocation(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val quickLocations = listOf(
    QuickLocation(
        label    = "Koramangala, Bengaluru",
        subtitle = "5th Block, Bengaluru - 560095",
        icon     = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label    = "Indiranagar, Bengaluru",
        subtitle = "100 Feet Road, Bengaluru - 560038",
        icon     = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label    = "HSR Layout, Bengaluru",
        subtitle = "Sector 7, Bengaluru - 560102",
        icon     = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label    = "Whitefield, Bengaluru",
        subtitle = "ITPL Main Road, Bengaluru - 560066",
        icon     = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label    = "Electronic City, Bengaluru",
        subtitle = "Phase 1, Bengaluru - 560100",
        icon     = Icons.Default.LocationOn,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    sheetState: SheetState,
    savedAddresses: List<Address>,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter locations based on search query
    val filteredLocations = remember(searchQuery) {
        if (searchQuery.isBlank()) quickLocations
        else quickLocations.filter {
            it.label.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Space32),
        ) {

            // ── Header ────────────────────────────────────────
            Text(
                text       = "Select Delivery Location",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            )

            // ── Search Field ──────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = {
                    Text("Search for area, street name...", color = AppGray)
                },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null, tint = AppGray)
                },
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppDivider,
                    focusedBorderColor   = ZomatoRed,
                    unfocusedContainerColor = AppLightGray,
                    focusedContainerColor   = AppLightGray,
                ),
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
            )

            Spacer(Modifier.height(Dimens.SpaceL))

            // ── Use Current Location ───────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLocationSelected("Current Location")
                        onDismiss()
                    }
                    .padding(
                        horizontal = Dimens.SpaceL,
                        vertical   = Dimens.SpaceM,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.MyLocation,
                    contentDescription = "Current location",
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(Dimens.IconM),
                )
                Spacer(Modifier.width(Dimens.SpaceM))
                Column {
                    Text(
                        text       = "Use Current Location",
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = ZomatoRed,
                    )
                    Text(
                        text  = "Enable location permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpaceL))

            // ── Saved Addresses (from Profile) ─────────────────
            if (savedAddresses.isNotEmpty()) {
                Text(
                    text     = "SAVED ADDRESSES",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = AppGray,
                    modifier = Modifier.padding(horizontal = Dimens.SpaceL)
                        .padding(top = Dimens.SpaceL, bottom = Dimens.SpaceS)
                )

                savedAddresses.forEach { address ->
                    SavedAddressRow(
                        address  = address,
                        onClick  = {
                            onLocationSelected(address.label + ", " + address.fullAddress.take(30))
                            onDismiss()
                        },
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Dimens.SpaceL)
                )
            }

            // ── Popular Locations ─────────────────────────────
            Text(
                text     = "POPULAR LOCATIONS",
                style    = MaterialTheme.typography.labelSmall,
                color    = AppGray,
                modifier = Modifier.padding(horizontal = Dimens.SpaceL)
                    .padding(top = Dimens.SpaceL, bottom = Dimens.SpaceS)
            )

            LazyColumn {
                items(filteredLocations) { location ->
                    QuickLocationRow(
                        location = location,
                        onClick  = {
                            onLocationSelected(location.label)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

// ── Saved Address Row ──────────────────────────────────────────
@Composable
private fun SavedAddressRow(
    address: Address,
    onClick: () -> Unit,
) {
    val icon = when (address.label.lowercase()) {
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        else   -> Icons.Default.LocationOn
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = address.label,
            tint               = AppGray,
            modifier           = Modifier.size(Dimens.IconM),
        )
        Spacer(Modifier.width(Dimens.SpaceM))
        Column {
            Text(
                text       = address.label,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = address.fullAddress,
                style    = MaterialTheme.typography.bodySmall,
                color    = AppGray,
                maxLines = 1,
            )
        }
    }
}

// ── Quick Location Row ────────────────────────────────────────
@Composable
private fun QuickLocationRow(
    location: QuickLocation,
    onClick: () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = location.icon,
            contentDescription = location.label,
            tint               = AppGray,
            modifier           = Modifier.size(Dimens.IconM),
        )
        Spacer(Modifier.width(Dimens.SpaceM))
        Column {
            Text(
                text  = location.label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text  = location.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
        }
    }
}