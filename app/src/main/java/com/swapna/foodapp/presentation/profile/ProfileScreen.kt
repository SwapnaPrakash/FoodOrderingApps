package com.swapna.foodapp.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.BestsellerBgColor
import com.swapna.foodapp.presentation.ui.theme.CancelledBgColor
import com.swapna.foodapp.presentation.ui.theme.DeliveredBgColor
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.AvatarIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.AvatarSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.MenuItemIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.OrderStatusBadgePaddingH
import com.swapna.foodapp.presentation.ui.theme.Dimens.OrderStatusBadgePaddingV
import com.swapna.foodapp.presentation.ui.theme.Dimens.OrderStatusBadgeRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.OrderStatusFontSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.ProfileLogoutBorder
import com.swapna.foodapp.presentation.ui.theme.PendingStatusColor
import com.swapna.foodapp.presentation.ui.theme.ProfileScreenBg
import com.swapna.foodapp.presentation.ui.theme.RatingGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.AppConstants.ALPHA_AVATAR_BG
import com.swapna.foodapp.utils.AppConstants.BACK
import com.swapna.foodapp.utils.AppConstants.CANCEL
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.DELETE_ADDRESS_DESC
import com.swapna.foodapp.utils.AppConstants.EDIT_PROFILE
import com.swapna.foodapp.utils.AppConstants.EMAIL
import com.swapna.foodapp.utils.AppConstants.KEY_ADDRESSES_HEADER
import com.swapna.foodapp.utils.AppConstants.KEY_LOGOUT
import com.swapna.foodapp.utils.AppConstants.KEY_NO_ADDRESS
import com.swapna.foodapp.utils.AppConstants.KEY_NO_ORDERS
import com.swapna.foodapp.utils.AppConstants.KEY_ORDERS_HEADER
import com.swapna.foodapp.utils.AppConstants.KEY_PHONE_SECTION
import com.swapna.foodapp.utils.AppConstants.KEY_PROFILE_HEADER
import com.swapna.foodapp.utils.AppConstants.LOGOUT
import com.swapna.foodapp.utils.AppConstants.NAME
import com.swapna.foodapp.utils.AppConstants.NOT_SET
import com.swapna.foodapp.utils.AppConstants.NO_ORDERS
import com.swapna.foodapp.utils.AppConstants.NO_SAVED_ADDRESSES
import com.swapna.foodapp.utils.AppConstants.ORDER_ITEMS_MORE_SUFFIX
import com.swapna.foodapp.utils.AppConstants.ORDER_STATUS_CANCELLED
import com.swapna.foodapp.utils.AppConstants.ORDER_STATUS_DELIVERED
import com.swapna.foodapp.utils.AppConstants.PHONE_NUMBER_LABEL
import com.swapna.foodapp.utils.AppConstants.PROFILE
import com.swapna.foodapp.utils.AppConstants.PROFILE_AVATAR_DESC
import com.swapna.foodapp.utils.AppConstants.RECENT_ORDERS_TITLE
import com.swapna.foodapp.utils.AppConstants.SAVE
import com.swapna.foodapp.utils.AppConstants.SAVED_ADDRESSES_TITLE
import com.swapna.foodapp.utils.AppConstants.WORK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileViewModel.ProfileEvent.NavigateToLogin -> {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                }

                is ProfileViewModel.ProfileEvent.NavigateBack ->
                    navController.popBackStack()

                is ProfileViewModel.ProfileEvent.ShowSnackbar ->
                    snackbarHost.showSnackbar(event.message)

                is ProfileViewModel.ProfileEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = ProfileScreenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = PROFILE, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = BACK,
                        )
                    }
                },
                actions = {
                    if (!state.isEditMode) {
                        IconButton(onClick = viewModel::onEditClicked) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = EDIT_PROFILE,
                                tint = ZomatoRed,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppWhiteSurface,
                ),
            )
        },
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {

            item(key = KEY_PROFILE_HEADER) {
                ProfileHeaderCard(
                    state = state,
                    onNameChange = viewModel::onNameChanged,
                    onEmailChange = viewModel::onEmailChanged,
                    onSave = viewModel::onSaveProfile,
                    onCancel = viewModel::onCancelEdit,
                )
            }

            item(key = KEY_PHONE_SECTION) {
                InfoCard(
                    icon = Icons.Default.Phone,
                    label = PHONE_NUMBER_LABEL,
                    value = state.phoneNumber.ifEmpty { NOT_SET },
                    tint = ZomatoRed,
                )
            }

            item(key = KEY_ADDRESSES_HEADER) {
                SectionHeader(title = SAVED_ADDRESSES_TITLE)
            }

            if (state.addresses.isEmpty()) {
                item(key = KEY_NO_ADDRESS) {
                    EmptyStateCard(message = NO_SAVED_ADDRESSES)
                }
            } else {
                items(items = state.addresses, key = { it.id }) { address ->
                    AddressCard(
                        address = address,
                        onDelete = { viewModel.onDeleteAddress(address.id) },
                    )
                }
            }

            item(key = KEY_ORDERS_HEADER) {
                SectionHeader(title = RECENT_ORDERS_TITLE)
            }

            if (state.orders.isEmpty()) {
                item(key = KEY_NO_ORDERS) {
                    EmptyStateCard(message = NO_ORDERS)
                }
            } else {
                items(items = state.orders, key = { it.id }) { order ->
                    OrderCard(order = order)
                }
            }

            item(key = KEY_LOGOUT) {
                Spacer(Modifier.height(Dimens.SpaceM))
                OutlinedButton(
                    onClick = viewModel::onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceL),
                    border = androidx.compose.foundation.BorderStroke(
                        ProfileLogoutBorder, ZomatoRed
                    ),
                    shape = RoundedCornerShape(Dimens.RadiusM),
                ) {
                    Text(text = LOGOUT, color = ZomatoRed)
                }
                Spacer(Modifier.height(Dimens.Space32))
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    state: ProfileViewModel.ProfileUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceM),
        colors = CardDefaults.cardColors(containerColor = AppWhiteSurface),
        shape = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceL),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(AvatarSize)
                    .clip(CircleShape)
                    .background(ZomatoRed.copy(alpha = ALPHA_AVATAR_BG)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = PROFILE_AVATAR_DESC,
                    tint = ZomatoRed,
                    modifier = Modifier.size(AvatarIconSize),
                )
            }

            Spacer(Modifier.height(Dimens.SpaceM))

            if (!state.isEditMode) {
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Dimens.SpaceXS))
                Text(
                    text = state.displayEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppGray,
                )
            }

            if (state.isEditMode) {
                Spacer(Modifier.height(Dimens.SpaceM))

                OutlinedTextField(
                    value = state.editName,
                    onValueChange = onNameChange,
                    label = { Text(NAME) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZomatoRed,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.SpaceM))

                OutlinedTextField(
                    value = state.editEmail,
                    onValueChange = onEmailChange,
                    label = { Text(EMAIL) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZomatoRed,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.SpaceL))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(CANCEL)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZomatoRed,
                        ),
                    ) {
                        Text(text = SAVE, color = AppWhiteSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color = AppGray,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors = CardDefaults.cardColors(containerColor = AppWhiteSurface),
        shape = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(MenuItemIconSize),
            )
            Spacer(Modifier.width(Dimens.SpaceM))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppGray,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onDelete: () -> Unit,
) {
    val icon = when (address.label.lowercase()) {
        ADDRESS_LABEL_HOME -> Icons.Default.Home
        WORK -> Icons.Default.Work
        else -> Icons.Default.LocationOn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors = CardDefaults.cardColors(containerColor = AppWhiteSurface),
        shape = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = address.label,
                tint = ZomatoRed,
                modifier = Modifier.size(MenuItemIconSize),
            )
            Spacer(Modifier.width(Dimens.SpaceM))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = address.fullAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = DELETE_ADDRESS_DESC,
                    tint = AppGray,
                )
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors = CardDefaults.cardColors(containerColor = AppWhiteSurface),
        shape = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceM)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.restaurantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                val statusLower = order.status.lowercase()
                Text(
                    text = order.status,
                    fontSize = OrderStatusFontSize,
                    color = when (statusLower) {
                        ORDER_STATUS_DELIVERED -> RatingGreen
                        ORDER_STATUS_CANCELLED -> ZomatoRed
                        else -> PendingStatusColor
                    },
                    modifier = Modifier
                        .background(
                            color = when (statusLower) {
                                ORDER_STATUS_DELIVERED -> DeliveredBgColor
                                ORDER_STATUS_CANCELLED -> CancelledBgColor
                                else -> BestsellerBgColor
                            },
                            shape = RoundedCornerShape(OrderStatusBadgeRadius),
                        )
                        .padding(
                            horizontal = OrderStatusBadgePaddingH,
                            vertical = OrderStatusBadgePaddingV,
                        ),
                )
            }

            Spacer(Modifier.height(Dimens.SpaceXS))

            val visibleItems = order.items.take(2)
            val extraCount = order.items.size - 2
            Text(
                text = visibleItems.joinToString(", ") { it.name }
                    .let {
                        if (extraCount > 0)
                            "$it +$extraCount$ORDER_ITEMS_MORE_SUFFIX"
                        else it
                    },
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )

            Spacer(Modifier.height(Dimens.SpaceXS))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = order.timeFriendly,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
                Text(
                    text = "$CURRENCY_SYMBOL${order.totalAmount.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            horizontal = Dimens.SpaceL,
            vertical = Dimens.SpaceS,
        ),
    )
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors = CardDefaults.cardColors(containerColor = AppWhiteSurface),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
            modifier = Modifier.padding(Dimens.SpaceL),
        )
    }
}