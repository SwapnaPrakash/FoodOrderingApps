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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    // ── Handle one-time events ────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileViewModel.ProfileEvent.NavigateToLogin ->
                    // Clear entire back stack → fresh login
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }

                is ProfileViewModel.ProfileEvent.NavigateToOrders ->
                    // Day 29+ — orders screen
                    snackbarHost.showSnackbar(
                        "Orders screen coming soon"
                    )

                is ProfileViewModel.ProfileEvent
                .NavigateToAddresses ->
                    snackbarHost.showSnackbar(
                        "Addresses screen coming soon"
                    )

                is ProfileViewModel.ProfileEvent
                .NavigateToPayments ->
                    snackbarHost.showSnackbar(
                        "Payments screen coming soon"
                    )

                is ProfileViewModel.ProfileEvent
                .NavigateToSettings ->
                    snackbarHost.showSnackbar(
                        "Settings screen coming soon"
                    )

                is ProfileViewModel.ProfileEvent.ShowSnackbar ->
                    snackbarHost.showSnackbar(event.message)

                is ProfileViewModel.ProfileEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Color(0xFFF8F8F8),

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme
                            .typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
    ) { paddingValues ->

        when {

            // ── Loading ───────────────────────────────────────
            uiState.isLoading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment =
                        androidx.compose.ui.Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ZomatoRed)
                }
            }

            // ── Error ─────────────────────────────────────────
            uiState.error != null && uiState.user == null -> {
                ErrorScreen(
                    message = uiState.error!!,
                    onRetry = { /* reload */ },
                )
            }

            // ── Content ───────────────────────────────────────
            uiState.user != null -> {
                val user = uiState.user!!

                // verticalScroll = entire screen scrollable
                // WHY? Long profiles with many menu items
                // should scroll smoothly
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                ) {

                    // ── 1. Profile Header ─────────────────────
                    // Shows avatar + name + email + phone
                    ProfileHeader(user = user)

                    Spacer(Modifier.height(Dimens.SpaceM))

                    // ── 2. Menu Items ─────────────────────────
                    // White card with clickable rows
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 2.dp,
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.ShoppingBag,
                                label = "My Orders",
                                onClick = viewModel::onOrdersTapped,
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.LocationOn,
                                label = "Saved Addresses",
                                onClick = viewModel::onAddressesTapped,
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.CreditCard,
                                label = "Payment Methods",
                                onClick = viewModel::onPaymentsTapped,
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.Notifications,
                                label = "Notifications",
                                onClick = { },
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.Settings,
                                label = "Settings",
                                onClick = viewModel::onSettingsTapped,
                            )
                        }
                    }

                    Spacer(Modifier.height(Dimens.SpaceL))

                    // ── 3. Edit Profile Button ────────────────
                    Button(
                        onClick = viewModel::onEditProfileTapped,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpaceL)
                            .height(Dimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZomatoRed,
                        ),
                        shape = RoundedCornerShape(
                            Dimens.RadiusM
                        ),
                    ) {
                        Text(
                            text = "Edit Profile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.height(Dimens.SpaceM))

                    // ── 4. Logout Button ──────────────────────
                    // Outlined = less prominent than Edit
                    // Logout is destructive — should be subtle
                    OutlinedButton(
                        onClick = viewModel::onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpaceL)
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(
                            Dimens.RadiusM
                        ),
                        border = androidx.compose.foundation
                            .BorderStroke(
                                1.dp, Color.LightGray
                            ),
                    ) {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment =
                                androidx.compose.ui.Alignment
                                    .CenterVertically,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector =
                                    Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.Gray,
                            )
                            androidx.compose.foundation.layout
                                .Spacer(
                                    Modifier.width(Dimens.SpaceS)
                                )
                            Text(
                                text = "Logout",
                                color = Color.Gray,
                            )
                        }
                    }

                    Spacer(Modifier.height(Dimens.SpaceXL))
                }

                // ── 5. Edit Profile Sheet ─────────────────────
                // Placed OUTSIDE Column — overlays entire screen
                // WHY outside? ModalBottomSheet must not be
                // inside a scrollable container
                if (uiState.showEditSheet) {
                    EditProfileSheet(
                        name = uiState.editName,
                        email = uiState.editEmail,
                        nameError = uiState.nameError,
                        emailError = uiState.emailError,
                        isSaving = uiState.isSaving,
                        onNameChange = viewModel::onNameChanged,
                        onEmailChange = viewModel::onEmailChanged,
                        onSave = viewModel::onSaveProfile,
                        onDismiss = viewModel::onDismissEditSheet,
                    )
                }
            }
        }
    }
}