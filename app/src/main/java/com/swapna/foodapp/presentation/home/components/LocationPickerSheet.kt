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
import com.swapna.foodapp.utils.AppConstants.CURRENT_LOCATION
import com.swapna.foodapp.utils.AppConstants.ENABLE_LOCATION_PERMISSION
import com.swapna.foodapp.utils.AppConstants.HOME
import com.swapna.foodapp.utils.AppConstants.LOC_ECITY
import com.swapna.foodapp.utils.AppConstants.LOC_ECITY_SUB
import com.swapna.foodapp.utils.AppConstants.LOC_HSR
import com.swapna.foodapp.utils.AppConstants.LOC_HSR_SUB
import com.swapna.foodapp.utils.AppConstants.LOC_INDIRANAGAR
import com.swapna.foodapp.utils.AppConstants.LOC_INDIRANAGAR_SUB
import com.swapna.foodapp.utils.AppConstants.LOC_KORAMANGALA
import com.swapna.foodapp.utils.AppConstants.LOC_KORAMANGALA_SUB
import com.swapna.foodapp.utils.AppConstants.LOC_WHITEFIELD
import com.swapna.foodapp.utils.AppConstants.LOC_WHITEFIELD_SUB
import com.swapna.foodapp.utils.AppConstants.POPULAR_LOCATIONS
import com.swapna.foodapp.utils.AppConstants.SAVED_ADDRESSES
import com.swapna.foodapp.utils.AppConstants.SEARCH_LOCATION_HINT
import com.swapna.foodapp.utils.AppConstants.SELECT_DELIVERY_LOCATION
import com.swapna.foodapp.utils.AppConstants.USE_CURRENT_LOCATION
import com.swapna.foodapp.utils.AppConstants.WORK

private data class QuickLocation(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val quickLocations = listOf(
    QuickLocation(
        label = LOC_KORAMANGALA,
        subtitle = LOC_KORAMANGALA_SUB,
        icon = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label = LOC_INDIRANAGAR,
        subtitle = LOC_INDIRANAGAR_SUB,
        icon = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label = LOC_HSR,
        subtitle = LOC_HSR_SUB,
        icon = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label = LOC_WHITEFIELD,
        subtitle = LOC_WHITEFIELD_SUB,
        icon = Icons.Default.LocationOn,
    ),
    QuickLocation(
        label = LOC_ECITY,
        subtitle = LOC_ECITY_SUB,
        icon = Icons.Default.LocationOn,
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
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Space32),
        ) {

            // Header
            Text(
                text = SELECT_DELIVERY_LOCATION,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceM,
                ),
            )

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(SEARCH_LOCATION_HINT, color = AppGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = AppGray)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppDivider,
                    focusedBorderColor = ZomatoRed,
                    unfocusedContainerColor = AppLightGray,
                    focusedContainerColor = AppLightGray,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
            )

            Spacer(Modifier.height(Dimens.SpaceL))

            // Use Current Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLocationSelected(CURRENT_LOCATION)
                        onDismiss()
                    }
                    .padding(
                        horizontal = Dimens.SpaceL,
                        vertical = Dimens.SpaceM,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = CURRENT_LOCATION,
                    tint = ZomatoRed,
                    modifier = Modifier.size(Dimens.IconM),
                )
                Spacer(Modifier.width(Dimens.SpaceM))
                Column {
                    Text(
                        text = USE_CURRENT_LOCATION,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ZomatoRed,
                    )
                    Text(
                        text = ENABLE_LOCATION_PERMISSION,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpaceL))

            // Saved Addresses (from Profile)
            if (savedAddresses.isNotEmpty()) {
                Text(
                    text = SAVED_ADDRESSES,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppGray,
                    modifier = Modifier
                        .padding(horizontal = Dimens.SpaceL)
                        .padding(top = Dimens.SpaceL, bottom = Dimens.SpaceS)
                )

                savedAddresses.forEach { address ->
                    SavedAddressRow(
                        address = address,
                        onClick = {
                            onLocationSelected(address.label + ", " + address.fullAddress.take(30))
                            onDismiss()
                        },
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Dimens.SpaceL)
                )
            }

            // Popular Locations
            Text(
                text = POPULAR_LOCATIONS,
                style = MaterialTheme.typography.labelSmall,
                color = AppGray,
                modifier = Modifier
                    .padding(horizontal = Dimens.SpaceL)
                    .padding(top = Dimens.SpaceL, bottom = Dimens.SpaceS)
            )

            LazyColumn {
                items(filteredLocations) { location ->
                    QuickLocationRow(
                        location = location,
                        onClick = {
                            onLocationSelected(location.label)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedAddressRow(
    address: Address,
    onClick: () -> Unit,
) {
    val icon = when (address.label.lowercase()) {
        HOME -> Icons.Default.Home
        WORK -> Icons.Default.Work
        else -> Icons.Default.LocationOn
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = address.label,
            tint = AppGray,
            modifier = Modifier.size(Dimens.IconM),
        )
        Spacer(Modifier.width(Dimens.SpaceM))
        Column {
            Text(
                text = address.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = address.fullAddress,
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun QuickLocationRow(
    location: QuickLocation,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = location.icon,
            contentDescription = location.label,
            tint = AppGray,
            modifier = Modifier.size(Dimens.IconM),
        )
        Spacer(Modifier.width(Dimens.SpaceM))
        Column {
            Text(
                text = location.label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = location.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
        }
    }
}