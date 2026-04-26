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
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.common.BillDetails
import com.swapna.foodapp.presentation.common.CartItemRow
import com.swapna.foodapp.presentation.common.EmptyCartView
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.CartScaffoldBg
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartBottomSpace
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.BACK
import com.swapna.foodapp.utils.AppConstants.CART_BILL_KEY
import com.swapna.foodapp.utils.AppConstants.CART_BOTTOM_SPACE_KEY
import com.swapna.foodapp.utils.AppConstants.CART_RESTAURANT_NAME_KEY
import com.swapna.foodapp.utils.AppConstants.CART_SPACER_KEY
import com.swapna.foodapp.utils.AppConstants.ITEMS
import com.swapna.foodapp.utils.AppConstants.MY_CART
import com.swapna.foodapp.utils.AppConstants.ORDER_PLACED_SUCCESS
import com.swapna.foodapp.utils.AppConstants.PLACE_ORDER_PREFIX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CartViewModel.CartEvent.NavigateBack ->
                    navController.popBackStack()

                is CartViewModel.CartEvent.NavigateToHome ->
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }

                is CartViewModel.CartEvent.OrderPlaced -> {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                    snackbarHost.showSnackbar(ORDER_PLACED_SUCCESS)
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
        containerColor = CartScaffoldBg,

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = MY_CART,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState.items.isNotEmpty()) {
                            Text(
                                text = "${uiState.items.sumOf { it.quantity }}$ITEMS",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppGray,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = BACK,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppWhiteSurface,
                ),
            )
        },

        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppWhiteSurface)
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
                        shape = RoundedCornerShape(Dimens.RadiusM),
                    ) {
                        Text(
                            text = "$PLACE_ORDER_PREFIX${uiState.breakdown.total.toInt()}",
                            color = AppWhiteSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        },

        ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ZomatoRed)
                }
            }

            uiState.isEmpty -> {
                EmptyCartView(
                    onBrowseFood = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.HOME) { inclusive = false }
                        }
                    },
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {

                    item(key = CART_RESTAURANT_NAME_KEY) {
                        Text(
                            text = uiState.restaurantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppWhiteSurface)
                                .padding(Dimens.SpaceL),
                        )
                    }

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

                    item(key = CART_SPACER_KEY) {
                        Spacer(Modifier.height(Dimens.SpaceM))
                    }

                    item(key = CART_BILL_KEY) {
                        BillDetails(breakdown = uiState.breakdown)
                    }

                    item(key = CART_BOTTOM_SPACE_KEY) {
                        Spacer(Modifier.height(CartBottomSpace))
                    }
                }
            }
        }
    }
}