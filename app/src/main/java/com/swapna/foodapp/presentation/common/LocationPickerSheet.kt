package com.swapna.foodapp.presentation.common

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
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.presentation.home.HomeViewModel
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.EmptyAddressEmojiSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationAddressCircle
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationAddressIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationAddressRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationAddressSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationBottomSpace
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationCloseCircle
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationCloseIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationDividerHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationDragHandleBotPad
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationDragHandleHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationDragHandleTopPad
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationDragHandleWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationHeaderTopPad
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationIconCircleSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationIconInCircleSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationPopularCircle
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationPopularIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationPopularSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationPopularTrailSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationProgressSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationProgressStroke
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationRowPadding
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationRowRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationRowVertPad
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSearchIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSearchRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSectionIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSectionLetterSp
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSectionSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSectionTextSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationSheetRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.LocationTitleSize
import com.swapna.foodapp.presentation.ui.theme.ErrorBannerBg
import com.swapna.foodapp.presentation.ui.theme.ErrorBannerText
import com.swapna.foodapp.presentation.ui.theme.LocationBorderGray
import com.swapna.foodapp.presentation.ui.theme.LocationDragHandle
import com.swapna.foodapp.presentation.ui.theme.LocationSurfaceGray
import com.swapna.foodapp.presentation.ui.theme.LocationTextPrimary
import com.swapna.foodapp.presentation.ui.theme.LocationTextSecondary
import com.swapna.foodapp.presentation.ui.theme.WarningBannerBg
import com.swapna.foodapp.presentation.ui.theme.WarningBannerText
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.AppConstants.ADD_HOME_WORK
import com.swapna.foodapp.utils.AppConstants.ALPHA_LOCATION_ICON
import com.swapna.foodapp.utils.AppConstants.ALPHA_LOCATION_SUBTITLE
import com.swapna.foodapp.utils.AppConstants.ALPHA_SCRIM
import com.swapna.foodapp.utils.AppConstants.CLOSE
import com.swapna.foodapp.utils.AppConstants.CURRENT_LOCATION
import com.swapna.foodapp.utils.AppConstants.DELIVERY_LOCATION
import com.swapna.foodapp.utils.AppConstants.DETECTING_LOCATION
import com.swapna.foodapp.utils.AppConstants.EMOJI_CROSS
import com.swapna.foodapp.utils.AppConstants.EMOJI_FIRE
import com.swapna.foodapp.utils.AppConstants.EMOJI_HOUSE
import com.swapna.foodapp.utils.AppConstants.EMOJI_NO_ENTRY
import com.swapna.foodapp.utils.AppConstants.EMOJI_PIN
import com.swapna.foodapp.utils.AppConstants.GETTING_GPS
import com.swapna.foodapp.utils.AppConstants.KEY_BOTTOM
import com.swapna.foodapp.utils.AppConstants.KEY_GAP
import com.swapna.foodapp.utils.AppConstants.KEY_NO_SAVED
import com.swapna.foodapp.utils.AppConstants.KEY_POPULAR_HEADER
import com.swapna.foodapp.utils.AppConstants.KEY_SAVED_HEADER
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
import com.swapna.foodapp.utils.AppConstants.NOT_IN_AREA_MESSAGE
import com.swapna.foodapp.utils.AppConstants.NO_SAVED_ADDRESSES_HINT
import com.swapna.foodapp.utils.AppConstants.SEARCH_FIELD_PLACEHOLDER
import com.swapna.foodapp.utils.AppConstants.SEARCH_LOCATION_DESC
import com.swapna.foodapp.utils.AppConstants.SECTION_POPULAR_LOCATIONS
import com.swapna.foodapp.utils.AppConstants.SECTION_SAVED_ADDRESSES
import com.swapna.foodapp.utils.AppConstants.USES_DEVICE_GPS
import com.swapna.foodapp.utils.AppConstants.USE_CURRENT_LOCATION_DESC
import com.swapna.foodapp.utils.AppConstants.WHERE_DELIVER
import com.swapna.foodapp.utils.AppConstants.WORK

private val RedSurface = ZomatoRed.copy(alpha = 0.08f)

private data class QuickLocation(val label: String, val subtitle: String)

private val quickLocations = listOf(
    QuickLocation(LOC_KORAMANGALA, LOC_KORAMANGALA_SUB),
    QuickLocation(LOC_INDIRANAGAR, LOC_INDIRANAGAR_SUB),
    QuickLocation(LOC_HSR, LOC_HSR_SUB),
    QuickLocation(LOC_WHITEFIELD, LOC_WHITEFIELD_SUB),
    QuickLocation(LOC_ECITY, LOC_ECITY_SUB),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    savedAddresses: List<Address>,
    locationFetchState: HomeViewModel.LocationFetchState,
    locationErrorMsg: String,
    onUseCurrentLocation: () -> Unit,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) quickLocations
        else quickLocations.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LocationSurfaceGray,
        tonalElevation = Dimens.Zero,
        scrimColor = Color.Black.copy(alpha = ALPHA_SCRIM),
        shape = RoundedCornerShape(
            topStart = LocationSheetRadius,
            topEnd = LocationSheetRadius,
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = LocationDragHandleTopPad,
                        bottom = LocationDragHandleBotPad,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(LocationDragHandleWidth)
                        .height(LocationDragHandleHeight)
                        .clip(CircleShape)
                        .background(LocationDragHandle),
                )
            }
        },
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.SpaceL,
                        end = Dimens.SpaceS,
                        top = LocationHeaderTopPad,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = DELIVERY_LOCATION,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LocationTextPrimary,
                        fontSize = LocationTitleSize,
                    )
                    Text(
                        text = WHERE_DELIVER,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocationTextSecondary,
                    )
                }

                IconButton(onClick = onDismiss) {
                    Box(
                        modifier = Modifier
                            .size(LocationCloseCircle)
                            .clip(CircleShape)
                            .background(LocationSurfaceGray),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = CLOSE,
                            tint = LocationTextSecondary,
                            modifier = Modifier.size(LocationCloseIconSize),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpaceM))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = SEARCH_FIELD_PLACEHOLDER,
                        color = LocationTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = SEARCH_LOCATION_DESC,
                        tint = if (searchQuery.isNotEmpty()) ZomatoRed
                        else LocationTextSecondary,
                        modifier = Modifier.size(LocationSearchIconSize),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZomatoRed,
                    unfocusedBorderColor = LocationBorderGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = LocationSurfaceGray,
                ),
                shape = RoundedCornerShape(LocationSearchRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            AnimatedVisibility(
                visible = locationFetchState == HomeViewModel.LocationFetchState.Error,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                StatusBanner(
                    emoji = EMOJI_CROSS,
                    message = locationErrorMsg,
                    bgColor = ErrorBannerBg,
                    txtColor = ErrorBannerText,
                )
            }

            AnimatedVisibility(
                visible = locationFetchState == HomeViewModel.LocationFetchState.NotInArea,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                StatusBanner(
                    emoji = EMOJI_NO_ENTRY,
                    message = NOT_IN_AREA_MESSAGE,
                    bgColor = WarningBannerBg,
                    txtColor = WarningBannerText,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL, vertical = LocationRowPadding)
                    .clip(RoundedCornerShape(LocationRowRadius))
                    .background(RedSurface)
                    .clickable(
                        enabled = locationFetchState !=
                                HomeViewModel.LocationFetchState.Fetching,
                    ) { onUseCurrentLocation() },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = Dimens.SpaceM,
                            vertical = LocationRowVertPad,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(LocationIconCircleSize)
                            .clip(CircleShape)
                            .background(ZomatoRed),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (locationFetchState ==
                            HomeViewModel.LocationFetchState.Fetching
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(LocationProgressSize),
                                strokeWidth = LocationProgressStroke,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = USE_CURRENT_LOCATION_DESC,
                                tint = Color.White,
                                modifier = Modifier.size(LocationIconInCircleSize),
                            )
                        }
                    }

                    Spacer(Modifier.width(Dimens.SpaceM))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (locationFetchState ==
                                HomeViewModel.LocationFetchState.Fetching
                            )
                                DETECTING_LOCATION
                            else USE_CURRENT_LOCATION_DESC,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = ZomatoRed,
                        )
                        Text(
                            text = if (locationFetchState ==
                                HomeViewModel.LocationFetchState.Fetching
                            )
                                GETTING_GPS
                            else USES_DEVICE_GPS,
                            style = MaterialTheme.typography.bodySmall,
                            color = ZomatoRed.copy(alpha = ALPHA_LOCATION_SUBTITLE),
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ZomatoRed.copy(alpha = ALPHA_LOCATION_ICON),
                        modifier = Modifier.size(LocationSearchIconSize),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpaceS))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                item(key = KEY_SAVED_HEADER) {
                    SectionLabel(title = SECTION_SAVED_ADDRESSES, icon = EMOJI_PIN)
                }

                if (savedAddresses.isEmpty()) {
                    item(key = KEY_NO_SAVED) {
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

                item(key = KEY_GAP) { Spacer(Modifier.height(Dimens.SpaceS)) }
                item(key = KEY_POPULAR_HEADER) {
                    SectionLabel(title = SECTION_POPULAR_LOCATIONS, icon = EMOJI_FIRE)
                }

                items(items = filtered, key = { it.label }) { location ->
                    PopularLocationRow(
                        label = location.label,
                        subtitle = location.subtitle,
                        onClick = { onLocationSelected(location.label) },
                    )
                }

                item(key = KEY_BOTTOM) {
                    Spacer(Modifier.height(LocationBottomSpace))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = LocationSectionIconSize,
        )
        Spacer(Modifier.width(LocationSectionSpacing))
        Text(
            text = title,
            fontSize = LocationSectionTextSize,
            fontWeight = FontWeight.Bold,
            color = LocationTextSecondary,
            letterSpacing = LocationSectionLetterSp,
        )
    }
}

@Composable
private fun SavedAddressRow(
    address: Address,
    onClick: () -> Unit,
) {
    val icon: ImageVector = when (address.label.lowercase()) {
        ADDRESS_LABEL_HOME -> Icons.Default.Home
        WORK -> Icons.Default.Work
        else -> Icons.Default.LocationOn
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = LocationRowPadding)
            .clip(RoundedCornerShape(LocationAddressRadius))
            .background(LocationSurfaceGray)
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
                    .size(LocationAddressCircle)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = address.label,
                    tint = ZomatoRed,
                    modifier = Modifier.size(LocationAddressIconSize),
                )
            }

            Spacer(Modifier.width(Dimens.SpaceM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LocationTextPrimary,
                )
                Spacer(Modifier.height(LocationAddressSpacing))
                Text(
                    text = buildString {
                        append(address.fullAddress)
                        if (address.landmark.isNotEmpty())
                            append(" · ${address.landmark}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = LocationTextSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PopularLocationRow(
    label: String,
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
                .size(LocationPopularCircle)
                .clip(CircleShape)
                .background(LocationSurfaceGray),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = label,
                tint = LocationTextSecondary,
                modifier = Modifier.size(LocationPopularIconSize),
            )
        }

        Spacer(Modifier.width(Dimens.SpaceM))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = LocationTextPrimary,
            )
            Spacer(Modifier.height(LocationPopularSpacing))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LocationTextSecondary,
                maxLines = 1,
            )
        }

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = LocationBorderGray,
            modifier = Modifier.size(LocationPopularTrailSize),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.SpaceL + LocationPopularCircle + Dimens.SpaceM)
            .height(LocationDividerHeight)
            .background(LocationBorderGray),
    )
}

@Composable
private fun EmptyAddressHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = LocationRowPadding)
            .clip(RoundedCornerShape(LocationAddressRadius))
            .background(LocationSurfaceGray),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = EMOJI_HOUSE,
                fontSize = EmptyAddressEmojiSize,
            )
            Spacer(Modifier.width(Dimens.SpaceM))
            Column {
                Text(
                    text = NO_SAVED_ADDRESSES_HINT,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LocationTextPrimary,
                )
                Text(
                    text = ADD_HOME_WORK,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocationTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(
    emoji: String,
    message: String,
    bgColor: Color,
    txtColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = LocationRowPadding)
            .clip(RoundedCornerShape(LocationAddressRadius))
            .background(bgColor),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = txtColor,
            )
        }
    }
}