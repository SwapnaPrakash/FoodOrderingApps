package com.swapna.foodapp.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.common.BillDetails
import com.swapna.foodapp.presentation.common.CartItemRow
import com.swapna.foodapp.presentation.common.EmptyCartView
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    // ── Handle one-time events ────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CartViewModel.CartEvent.NavigateBack ->
                    navController.popBackStack()

                is CartViewModel.CartEvent.NavigateToHome ->
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = false
                        }
                    }

                is CartViewModel.CartEvent.OrderPlaced -> {
                    // Navigate to order confirmation
                    // Day 22+ — for now go home
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = false
                        }
                    }
                    snackbarHost.showSnackbar(
                        "Order placed successfully! 🎉"
                    )
                }

                is CartViewModel.CartEvent.ShowSnackbar ->
                    snackbarHost.showSnackbar(event.message)

                is CartViewModel.CartEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Color(0xFFF8F8F8),

        // ── Top App Bar ───────────────────────────────────────
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Cart",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState.items.isNotEmpty()) {
                            Text(
                                text = "${
                                    uiState.items
                                        .sumOf { it.quantity }
                                } items",
                                style = MaterialTheme.typography
                                    .bodySmall,
                                color = AppGray,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::onBackPressed
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },

        // ── Place Order Bottom Bar ────────────────────────────
        // Only shown when cart has items
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(Dimens.SpaceM)
                        .navigationBarsPadding(),
                ) {
                    Button(
                        onClick = viewModel::onPlaceOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZomatoRed,
                        ),
                        shape = RoundedCornerShape(
                            Dimens.RadiusM
                        ),
                    ) {
                        Text(
                            text = "Place Order  •  " +
                                    "₹${uiState.breakdown.total.toInt()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography
                                .titleMedium,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->

        when {
            // ── Loading ───────────────────────────────────────
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ZomatoRed)
                }
            }

            // ── Empty cart ────────────────────────────────────
            uiState.isEmpty -> {
                EmptyCartView(
                    onBrowseFood = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.HOME) {
                                inclusive = false
                            }
                        }
                    },
                )
            }

            // ── Cart with items ───────────────────────────────
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {

                    // ── Restaurant name header ─────────────────
                    item(key = "restaurant_name") {
                        Text(
                            text = uiState.restaurantName,
                            style = MaterialTheme.typography
                                .titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(Dimens.SpaceL),
                        )
                    }

                    // ── Cart items ─────────────────────────────
                    // key = item.id → stable keys for Compose
                    items(
                        items = uiState.items,
                        key = { it.id },
                    ) { cartItem ->
                        CartItemRow(
                            cartItem = cartItem,
                            onIncrement = viewModel::onIncrementItem,
                            onDecrement = viewModel::onDecrementItem,
                        )
                    }

                    // ── Spacer between items + bill ────────────
                    item(key = "spacer") {
                        Spacer(Modifier.height(Dimens.SpaceM))
                    }

                    // ── Bill Details ───────────────────────────
                    // Shows subtotal + delivery + GST + total
                    item(key = "bill_details") {
                        BillDetails(
                            breakdown = uiState.breakdown,
                        )
                    }

                    // ── Bottom space for button ────────────────
                    item(key = "bottom_space") {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}