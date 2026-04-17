package com.swapna.foodapp.presentation.home.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.content.ContextCompat
import com.swapna.foodapp.presentation.common.LocationHelper
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.unit.sp

private data class QuickLocation(
    val label:    String,
    val subtitle: String,
)

private val quickLocations = listOf(
    QuickLocation(AppConstants.LOC_KORAMANGALA,  AppConstants.LOC_KORAMANGALA_SUB),
    QuickLocation(AppConstants.LOC_INDIRANAGAR,  AppConstants.LOC_INDIRANAGAR_SUB),
    QuickLocation(AppConstants.LOC_HSR,          AppConstants.LOC_HSR_SUB),
    QuickLocation(AppConstants.LOC_WHITEFIELD,   AppConstants.LOC_WHITEFIELD_SUB),
    QuickLocation(AppConstants.LOC_ECITY,        AppConstants.LOC_ECITY_SUB),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    savedAddresses:     List<Address>,        // from HomeViewModel → user profile
    onLocationSelected: (String) -> Unit,     // saves to HomeViewModel + HomeTopBar
    onDismiss:          () -> Unit,
) {
    var searchQuery      by remember { mutableStateOf("") }
    // WHY showNotInAreaMsg state here?
    // Only shown inside this sheet — ephemeral UI
    // Dismisses when sheet closes — no ViewModel needed
    var showNotInAreaMsg by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) quickLocations
        else quickLocations.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
        tonalElevation   = 0.dp,
        scrimColor       = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding(),
        ) {

            // ── Title ────────────────────────────────────────────────
            Text(
                text       = AppConstants.SELECT_DELIVERY_LOCATION,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            )

            // ── Search field ─────────────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = {
                    Text(
                        text  = AppConstants.SEARCH_LOCATION_HINT,
                        color = AppGray,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = "Search location",
                        tint               = AppGray,
                        modifier           = Modifier.size(20.dp),
                    )
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = ZomatoRed,
                    unfocusedBorderColor    = Color.LightGray,
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── "We're not in your area yet" banner ──────────────────
            // Shown when user taps Use Current Location
            // WHY Card not Snackbar?
            // Stays visible inside sheet until user taps elsewhere
            // Snackbar requires SnackbarHostState at Scaffold level
            // Card is simpler and inline in sheet ✅
            if (showNotInAreaMsg) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceL),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD), // light amber
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "🚫", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(Dimens.SpaceS))
                        Column {
                            Text(
                                text       = "We're not in your area yet",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF856404),
                            )
                            Text(
                                text  = "We'll let you know when we expand 🚀",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF856404),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Dimens.SpaceS))
            }

            // ── Use Current Location ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable {
                        // WHY show message instead of selecting?
                        // App doesn't serve all areas via GPS yet
                        // Showing inline message = better UX than silent fail
                        // User can still pick from Popular Locations below
                        showNotInAreaMsg = true
                    }
                    .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.MyLocation,
                    contentDescription = "Use current location",
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(Dimens.SpaceM))
                Column {
                    Text(
                        text       = AppConstants.USE_CURRENT_LOCATION,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = ZomatoRed,
                    )
                    Text(
                        text  = AppConstants.ENABLE_LOCATION_PERMISSION,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {

                // ── SAVED ADDRESSES section ───────────────────────────
                // WHY show even when empty?
                // Shows section header + empty state so user knows
                // they can save addresses from Profile screen
                item(key = "saved_header") {
                    SectionHeader(title = AppConstants.SAVED_ADDRESSES)
                }

                if (savedAddresses.isEmpty()) {
                    // WHY show hint not just nothing?
                    // User doesn't know they can save addresses
                    // Subtle hint = discoverability improvement
                    item(key = "no_saved") {
                        Text(
                            text     = "No saved addresses yet.\nAdd Home & Work in Profile →",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = AppGray,
                            modifier = Modifier
                                .background(Color.White)
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimens.SpaceL,
                                    vertical   = Dimens.SpaceS,
                                ),
                        )
                    }
                } else {
                    items(
                        items = savedAddresses,
                        key   = { it.id },
                    ) { address ->
                        SavedAddressRow(
                            address = address,
                            onClick = {
                                // Build "Home, 5th Block, Bengaluru" format
                                val locationLabel = buildString {
                                    append(address.label)
                                    if (address.fullAddress.isNotEmpty()) {
                                        append(", ")
                                        append(address.fullAddress.take(40))
                                    }
                                }
                                onLocationSelected(locationLabel)
                                onDismiss()
                            },
                        )
                    }
                }

                item(key = "divider_after_saved") {
                    HorizontalDivider(
                        color    = Color(0xFFEEEEEE),
                        modifier = Modifier.padding(horizontal = Dimens.SpaceL),
                    )
                }

                // ── POPULAR LOCATIONS section ─────────────────────────
                item(key = "popular_header") {
                    SectionHeader(title = AppConstants.POPULAR_LOCATIONS)
                }

                items(
                    items = filtered,
                    key   = { it.label },
                ) { location ->
                    QuickLocationRow(
                        location = location,
                        onClick  = {
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

// ── Section header ────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        fontSize = 11.sp,
        color    = AppGray,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = Dimens.SpaceL)
            .padding(top = Dimens.SpaceL, bottom = Dimens.SpaceS),
    )
}

// ── Saved address row (Home / Work / Other) ───────────────────────────────
@Composable
private fun SavedAddressRow(
    address: Address,
    onClick: () -> Unit,
) {
    // WHY icon chosen by label?
    // Home → house icon, Work → briefcase icon
    // Visual shortcut: user recognizes at a glance
    val icon: ImageVector = when (address.label.lowercase().trim()) {
        AppConstants.HOME -> Icons.Default.Home
        AppConstants.WORK -> Icons.Default.Work
        else              -> Icons.Default.LocationOn
    }

    LocationRowLayout(
        icon    = icon,
        title   = address.label,
        // WHY show fullAddress + landmark?
        // Full address alone is enough for recognition
        // Landmark makes it even clearer ("Near ITPL")
        subtitle = buildString {
            append(address.fullAddress)
            if (address.landmark.isNotEmpty()) {
                append(" · ${address.landmark}")
            }
        },
        onClick = onClick,
    )
}

// ── Popular location row ──────────────────────────────────────────────────
@Composable
private fun QuickLocationRow(
    location: QuickLocation,
    onClick:  () -> Unit,
) {
    LocationRowLayout(
        icon     = Icons.Default.LocationOn,
        title    = location.label,
        subtitle = location.subtitle,
        onClick  = onClick,
    )
}

// ── Shared row layout ─────────────────────────────────────────────────────
// WHY extract to shared composable?
// SavedAddressRow + QuickLocationRow look identical
// One source of truth = consistent spacing + divider
@Composable
private fun LocationRowLayout(
    icon:     ImageVector,
    title:    String,
    subtitle: String,
    onClick:  () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = title,
                tint               = AppGray,
                modifier           = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(Dimens.SpaceM))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = subtitle,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = AppGray,
                        maxLines = 1,
                    )
                }
            }
        }
        HorizontalDivider(
            color     = Color(0xFFF5F5F5),
            thickness = 1.dp,
            modifier  = Modifier.padding(start = Dimens.SpaceL + 24.dp + Dimens.SpaceM),
        )
    }
}