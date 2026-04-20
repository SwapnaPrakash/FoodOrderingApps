package com.swapna.foodapp.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.presentation.home.HomeViewModel
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants

// ── Design tokens ─────────────────────────────────────────────
private val SurfaceGray   = Color(0xFFF8F8F8)
private val BorderGray    = Color(0xFFEEEEEE)
private val TextPrimary   = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)
private val RedSurface    = ZomatoRed.copy(alpha = 0.08f)

private data class QuickLocation(val label: String, val subtitle: String)

private val quickLocations = listOf(
    QuickLocation(AppConstants.LOC_KORAMANGALA, AppConstants.LOC_KORAMANGALA_SUB),
    QuickLocation(AppConstants.LOC_INDIRANAGAR, AppConstants.LOC_INDIRANAGAR_SUB),
    QuickLocation(AppConstants.LOC_HSR,         AppConstants.LOC_HSR_SUB),
    QuickLocation(AppConstants.LOC_WHITEFIELD,  AppConstants.LOC_WHITEFIELD_SUB),
    QuickLocation(AppConstants.LOC_ECITY,       AppConstants.LOC_ECITY_SUB),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    savedAddresses:       List<Address>,
    locationFetchState:   HomeViewModel.LocationFetchState,
    locationErrorMsg:     String,
    onUseCurrentLocation: () -> Unit,
    onLocationSelected:   (String) -> Unit,
    onDismiss:            () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

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
        scrimColor       = Color.Black.copy(alpha = 0.6f),
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        // ── Custom slim drag handle ────────────────────────────
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDDDDD)),
                )
            }
        },
        // ── Remove sheet insets — Column handles them ──────────
        windowInsets = WindowInsets(0),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {

            // ── Header: Title + Close button ──────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.SpaceL, end = Dimens.SpaceS, top = 4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text       = "Delivery Location",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        fontSize   = 22.sp,
                    )
                    Text(
                        text  = "Where should we deliver?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceGray),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Close,
                            contentDescription = "Close",
                            tint               = TextSecondary,
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── Search field ──────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = {
                    Text(
                        text  = "Search area, street or landmark...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = "Search",
                        tint               = if (searchQuery.isNotEmpty()) ZomatoRed
                        else TextSecondary,
                        modifier           = Modifier.size(20.dp),
                    )
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = ZomatoRed,
                    unfocusedBorderColor    = BorderGray,
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = SurfaceGray,
                ),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── Status banners ────────────────────────────────
            AnimatedVisibility(
                visible = locationFetchState == HomeViewModel.LocationFetchState.Error,
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                StatusBanner(
                    emoji    = "❌",
                    message  = locationErrorMsg,
                    bgColor  = Color(0xFFFFEDED),
                    txtColor = Color(0xFFB00020),
                )
            }

            AnimatedVisibility(
                visible = locationFetchState == HomeViewModel.LocationFetchState.NotInArea,
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                StatusBanner(
                    emoji    = "🚫",
                    message  = "We're not in your area yet. We'll notify you when we expand! 🚀",
                    bgColor  = Color(0xFFFFF3CD),
                    txtColor = Color(0xFF856404),
                )
            }

            // ── Use Current Location ──────────────────────────
            // WHY Box+clickable not Card(onClick)?
            // Card(onClick) conflicts with ModalBottomSheet drag gestures
            // Sheet consumes the touch before Card click fires
            // Box+clickable registers directly on layout — no conflict
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RedSurface)
                    .clickable(
                        enabled = locationFetchState !=
                                HomeViewModel.LocationFetchState.Fetching,
                    ) { onUseCurrentLocation() },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceM, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Icon circle container
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ZomatoRed),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (locationFetchState ==
                            HomeViewModel.LocationFetchState.Fetching
                        ) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Default.MyLocation,
                                contentDescription = "Use current location",
                                tint               = Color.White,
                                modifier           = Modifier.size(22.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(Dimens.SpaceM))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (locationFetchState ==
                                HomeViewModel.LocationFetchState.Fetching)
                                "Detecting location..."
                            else "Use Current Location",
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color      = ZomatoRed,
                        )
                        Text(
                            text = if (locationFetchState ==
                                HomeViewModel.LocationFetchState.Fetching)
                                "Getting GPS coordinates..."
                            else "Uses your device GPS",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZomatoRed.copy(alpha = 0.7f),
                        )
                    }

                    Icon(
                        imageVector        = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint               = ZomatoRed.copy(alpha = 0.4f),
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpaceS))

            // ── Scrollable list ───────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {

                // ── Saved Addresses section ───────────────────
                item(key = "saved_header") {
                    SectionLabel(title = "SAVED ADDRESSES", icon = "📍")
                }

                if (savedAddresses.isEmpty()) {
                    item(key = "no_saved") {
                        EmptyAddressHint()
                    }
                } else {
                    items(items = savedAddresses, key = { it.id }) { address ->
                        SavedAddressRow(
                            address = address,
                            onClick = {
                                onLocationSelected(
                                    "${address.label}, ${address.fullAddress.take(40)}"
                                )
                            },
                        )
                    }
                }

                item(key = "gap") { Spacer(Modifier.height(Dimens.SpaceS)) }

                // ── Popular Locations section ─────────────────
                item(key = "popular_header") {
                    SectionLabel(title = "POPULAR LOCATIONS", icon = "🔥")
                }

                items(items = filtered, key = { it.label }) { location ->
                    PopularLocationRow(
                        label    = location.label,
                        subtitle = location.subtitle,
                        onClick  = { onLocationSelected(location.label) },
                    )
                }

                item(key = "bottom") { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Private composables
// ══════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(title: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text          = title,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextSecondary,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
private fun SavedAddressRow(
    address: Address,
    onClick: () -> Unit,
) {
    val icon: ImageVector = when (address.label.lowercase()) {
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        else   -> Icons.Default.LocationOn
    }

    // WHY Box+clickable not Card(onClick)?
    // Same reason as Use Current Location row above
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGray)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = address.label,
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(Dimens.SpaceM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = address.label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(address.fullAddress)
                        if (address.landmark.isNotEmpty())
                            append(" · ${address.landmark}")
                    },
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PopularLocationRow(
    label:   String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SurfaceGray),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.LocationOn,
                contentDescription = label,
                tint               = TextSecondary,
                modifier           = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(Dimens.SpaceM))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = subtitle,
                style    = MaterialTheme.typography.bodySmall,
                color    = TextSecondary,
                maxLines = 1,
            )
        }

        Icon(
            imageVector        = Icons.Default.LocationOn,
            contentDescription = null,
            tint               = BorderGray,
            modifier           = Modifier.size(16.dp),
        )
    }

    // Subtle divider indented to align with text
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.SpaceL + 38.dp + Dimens.SpaceM)
            .height(1.dp)
            .background(BorderGray),
    )
}

@Composable
private fun EmptyAddressHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGray),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "🏠", fontSize = 24.sp)
            Spacer(Modifier.width(Dimens.SpaceM))
            Column {
                Text(
                    text       = "No saved addresses",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                Text(
                    text  = "Add Home & Work in your Profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(
    emoji:    String,
    message:  String,
    bgColor:  Color,
    txtColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text  = message,
                style = MaterialTheme.typography.bodySmall,
                color = txtColor,
            )
        }
    }
}