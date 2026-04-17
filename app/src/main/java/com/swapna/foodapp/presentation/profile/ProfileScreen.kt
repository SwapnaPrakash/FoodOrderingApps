package com.swapna.foodapp.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.common.EditProfileSheet
import com.swapna.foodapp.presentation.common.ErrorScreen
import com.swapna.foodapp.presentation.common.ProfileHeader
import com.swapna.foodapp.presentation.common.ProfileMenuItem
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.presentation.ui.theme.AppGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel:     ProfileViewModel = hiltViewModel(),
) {
    val state       by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    // ── One-time events ───────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileViewModel.ProfileEvent.NavigateToLogin -> {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = true
                        }
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
        snackbarHost   = { SnackbarHost(snackbarHost) },
        containerColor = Color(0xFFF5F5F5),
        topBar         = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Profile",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::onBackPressed,
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    // Edit button in top bar
                    if (!state.isEditMode) {
                        IconButton(
                            onClick = viewModel::onEditClicked,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint               = ZomatoRed,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
    ) { paddingValues ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {

            // ── Profile header card ───────────────────────────
            item(key = "profile_header") {
                ProfileHeaderCard(
                    state        = state,
                    onNameChange  = viewModel::onNameChanged,
                    onEmailChange = viewModel::onEmailChanged,
                    onSave        = viewModel::onSaveProfile,
                    onCancel      = viewModel::onCancelEdit,
                )
            }

            // ── Phone number (from Firebase) ──────────────────
            item(key = "phone_section") {
                InfoCard(
                    icon    = Icons.Default.Phone,
                    label   = "Phone Number",
                    // ✅ Real phone from Firebase Auth
                    // This is the number used for OTP login
                    value   = state.phoneNumber
                        .ifEmpty { "Not set" },
                    tint    = ZomatoRed,
                )
            }

            // ── Saved Addresses ───────────────────────────────
            item(key = "addresses_header") {
                SectionHeader(title = "Saved Addresses")
            }

            if (state.addresses.isEmpty()) {
                item(key = "no_address") {
                    EmptyStateCard(
                        message = "No saved addresses yet"
                    )
                }
            } else {
                items(
                    items = state.addresses,
                    key   = { it.id },
                ) { address ->
                    AddressCard(
                        address  = address,
                        onDelete = {
                            viewModel.onDeleteAddress(address.id)
                        },
                    )
                }
            }

            // ── Recent Orders ─────────────────────────────────
            item(key = "orders_header") {
                SectionHeader(title = "Recent Orders")
            }

            if (state.orders.isEmpty()) {
                item(key = "no_orders") {
                    EmptyStateCard(
                        message = "No orders yet.\nStart ordering!"
                    )
                }
            } else {
                items(
                    items = state.orders,
                    key   = { it.id },
                ) { order ->
                    OrderCard(order = order)
                }
            }

            // ── Logout ────────────────────────────────────────
            item(key = "logout") {
                Spacer(Modifier.height(Dimens.SpaceM))
                OutlinedButton(
                    onClick  = viewModel::onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceL),
                    border   = androidx.compose.foundation
                        .BorderStroke(1.dp, ZomatoRed),
                    shape    = RoundedCornerShape(Dimens.RadiusM),
                ) {
                    Text(
                        text  = "Logout",
                        color = ZomatoRed,
                    )
                }
                Spacer(Modifier.height(Dimens.Space32))
            }
        }
    }
}

// ── Profile Header Card ───────────────────────────────────────
@Composable
private fun ProfileHeaderCard(
    state:        ProfileViewModel.ProfileUiState,
    onNameChange:  (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave:        () -> Unit,
    onCancel:      () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceM),
        colors   = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape    = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Column(
            modifier            = Modifier.padding(Dimens.SpaceL),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Avatar ────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ZomatoRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Default.Person,
                    contentDescription = "Profile avatar",
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(40.dp),
                )
            }

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── View mode ─────────────────────────────────────
            if (!state.isEditMode) {
                Text(
                    text       = state.displayName,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Dimens.SpaceXS))
                Text(
                    text  = state.displayEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppGray,
                )
            }

            // ── Edit mode ─────────────────────────────────────
            if (state.isEditMode) {
                Spacer(Modifier.height(Dimens.SpaceM))

                // Name field
                OutlinedTextField(
                    value         = state.editName,
                    onValueChange = onNameChange,
                    label         = { Text("Name") },
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZomatoRed,
                    ),
                    modifier      = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.SpaceM))

                // Email field
                OutlinedTextField(
                    value         = state.editEmail,
                    onValueChange = onEmailChange,
                    label         = { Text("Email") },
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZomatoRed,
                    ),
                    modifier      = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.SpaceL))

                // Save + Cancel buttons
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.SpaceM
                    ),
                ) {
                    OutlinedButton(
                        onClick  = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick  = onSave,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = ZomatoRed,
                        ),
                    ) {
                        Text(
                            text  = "Save",
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// ── Info card row ─────────────────────────────────────────────
@Composable
private fun InfoCard(
    icon:  ImageVector,
    label: String,
    value: String,
    tint:  Color = AppGray,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors   = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape    = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = tint,
                modifier           = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(Dimens.SpaceM))
            Column {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppGray,
                )
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Address card ──────────────────────────────────────────────
@Composable
private fun AddressCard(
    address:  Address,
    onDelete: () -> Unit,
) {
    val icon = when (address.label.lowercase()) {
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        else   -> Icons.Default.LocationOn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors   = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape    = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = address.label,
                tint               = ZomatoRed,
                modifier           = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(Dimens.SpaceM))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = address.label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text  = address.fullAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = "Delete address",
                    tint               = AppGray,
                )
            }
        }
    }
}

// ── Order card ────────────────────────────────────────────────
@Composable
private fun OrderCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors   = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape    = RoundedCornerShape(Dimens.RadiusM),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceM),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text       = order.restaurantName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                // Order status badge
                Text(
                    text     = order.status,
                    fontSize = 11.sp,
                    color    = when (order.status.lowercase()) {
                        "delivered" -> Color(0xFF3F7E00)
                        "cancelled" -> ZomatoRed
                        else        -> Color(0xFFFF8C00)
                    },
                    modifier = Modifier
                        .background(
                            color  = when (
                                order.status.lowercase()
                            ) {
                                "delivered" ->
                                    Color(0xFFE8F5E9)
                                "cancelled" ->
                                    Color(0xFFFFEBEE)
                                else        ->
                                    Color(0xFFFFF3E0)
                            },
                            shape  = RoundedCornerShape(4.dp),
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical   = 2.dp,
                        ),
                )
            }

            Spacer(Modifier.height(Dimens.SpaceXS))

            // Order items summary
            Text(
                text  = order.items
                    .take(2)
                    .joinToString(", ") { it.name }
                    .let {
                        if (order.items.size > 2)
                            "$it +${order.items.size - 2} more"
                        else it
                    },
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )

            Spacer(Modifier.height(Dimens.SpaceXS))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = order.timeFriendly,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
                Text(
                    text       = "₹${order.totalAmount.toInt()}",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            horizontal = Dimens.SpaceL,
            vertical   = Dimens.SpaceS,
        ),
    )
}

// ── Empty state card ──────────────────────────────────────────
@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceM),
        colors   = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
    ) {
        Text(
            text     = message,
            style    = MaterialTheme.typography.bodyMedium,
            color    = AppGray,
            modifier = Modifier.padding(Dimens.SpaceL),
        )
    }
}