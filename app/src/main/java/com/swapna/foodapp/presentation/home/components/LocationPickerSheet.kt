package com.swapna.foodapp.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppLightGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants
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
import androidx.compose.ui.text.input.ImeAction

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
        else {
            quickLocations.filter { location ->
                location.label.contains(
                    searchQuery, ignoreCase = true
                ) ||
                        location.subtitle.contains(
                            searchQuery, ignoreCase = true
                        )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor   = Color.White,
        tonalElevation   = 0.dp,
        scrimColor       = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(
            topStart = Dimens.RadiusL,
            topEnd   = Dimens.RadiusL,
        ),dragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding(),
        ) {

            // Header
            Text(
                text = SELECT_DELIVERY_LOCATION,
                style = MaterialTheme.typography.titleLarge,
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
                    Text(
                        SEARCH_LOCATION_HINT,
                        color = AppGray,
                        style = MaterialTheme.typography.bodyMedium,)
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = AppConstants.CD_SEARCH_LOCATION,
                        tint               = AppGray,
                        modifier           = Modifier.size(Dimens.IconM),
                        )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppDivider,
                    focusedBorderColor = ZomatoRed,
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                shape    = RoundedCornerShape(Dimens.RadiusS),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
            )

            Spacer(Modifier.height(Dimens.SpaceL))

            // Use Current Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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

            HorizontalDivider(
                color    = AppDivider,
                modifier = Modifier.padding(horizontal = Dimens.SpaceL))

            // Saved Addresses (from Profile)
            if (savedAddresses.isNotEmpty()) {
                Text(
                    text = SAVED_ADDRESSES,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppGray,
                    modifier = Modifier
                        .background(Color.White)
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
                    color    = AppDivider,
                    modifier = Modifier.padding(horizontal = Dimens.SpaceL)
                )
            }

            // Popular Locations
            Text(
                text = POPULAR_LOCATIONS,
                style = MaterialTheme.typography.labelSmall,
                color = AppGray,
                modifier = Modifier
                    .background(White)
                    .padding(horizontal = Dimens.SpaceL)
                    .padding(top = Dimens.SpaceL, bottom = Dimens.SpaceS)
            )

            LazyColumn( modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
                ) {
                items(items = filteredLocations,
                    key   = { it.label },
                    ) { location ->
                    QuickLocationRow(
                        location = location,
                        onClick = {
                            onLocationSelected(location.label)
                            onDismiss()
                        },
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(Dimens.Space32))
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
            .background(Color.White)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "${AppConstants.CD_LOCATION_PIN}: ${address.label}",
            tint = AppGray,
            modifier = Modifier.size(Dimens.IconM),
        )
        Spacer(Modifier.width(Dimens.SpaceM))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = address.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = address.fullAddress,
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
                maxLines = 1,
            )
        }
        // Thin divider indented to align with text
        HorizontalDivider(
            color     = Color(0xFFF5F5F5),
            thickness = 1.dp,
            modifier  = Modifier.padding(
                start = Dimens.SpaceL + Dimens.IconM + Dimens.SpaceM,
            ),
        )
    }
}

@Composable
private fun QuickLocationRow(
    location: QuickLocation,
    onClick:  () -> Unit,
) {
    // ✅ FIX 8: White background per quick location row
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Location pin icon
            Icon(
                imageVector        = location.icon,
                contentDescription = "${AppConstants.CD_LOCATION_PIN}: ${location.label}",
                tint               = AppGray,
                modifier           = Modifier.size(Dimens.IconM),
            )

            Spacer(Modifier.width(Dimens.SpaceM))

            Column(modifier = Modifier.weight(1f)) {
                // Area name — SemiBold to stand out
                Text(
                    text       = location.label,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(2.dp))

                // Full address — lighter gray
                Text(
                    text  = location.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
            }
        }

        // Thin divider between rows
        // Indented from left to align with text column
        HorizontalDivider(
            color     = Color(0xFFF5F5F5),
            thickness = 1.dp,
            modifier  = Modifier.padding(
                // start = padding + icon size + icon-to-text gap
                // so divider aligns with text not icon
                start = Dimens.SpaceL + Dimens.IconM + Dimens.SpaceM,
            ),
        )
    }
}

/*
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
}*/
