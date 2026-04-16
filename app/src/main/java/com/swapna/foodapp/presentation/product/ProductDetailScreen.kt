package com.swapna.foodapp.presentation.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.swapna.foodapp.utils.AppConstants.PRODUCT

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.swapna.foodapp.presentation.common.CustomisationGroup
import com.swapna.foodapp.presentation.common.ErrorScreen
import com.swapna.foodapp.presentation.restaurant.components.QuantitySelector
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    viewModel:     ProductDetailViewModel = hiltViewModel(),
) {
    // ── Collect state ─────────────────────────────────────────
    // collectAsStateWithLifecycle = auto-stops collecting
    // when screen is in background (saves resources)
    val uiState      by viewModel.uiState
        .collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    // ── Handle one-time events ────────────────────────────────
    // LaunchedEffect(Unit) = runs once when composable enters
    // WHY Unit? We don't want to restart on state changes
    // Just collect events for lifetime of this composable
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductDetailViewModel
                .ProductDetailEvent.NavigateBack ->
                    navController.popBackStack()

                is ProductDetailViewModel
                .ProductDetailEvent.ShowSnackbar ->
                    snackbarHost.showSnackbar(event.message)

                is ProductDetailViewModel
                .ProductDetailEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHost) },
        containerColor = Color.White,

        // ── TopBar ────────────────────────────────────────────
        topBar = {
            TopAppBar(
                title = {
                    // Show item name when loaded
                    Text(
                        text  = uiState.item?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::onBackPressed
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },

        // ── Add to Cart Bottom Bar ────────────────────────────
        // Always visible — user can add at any point
        // Shows live-updating price
        bottomBar = {
            if (uiState.item != null) {
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    color           = Color.White,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceM)
                            .navigationBarsPadding(),
                    ) {
                        // ── Quantity + Price row ──────────────
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Dimens.SpaceS),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Quantity selector — reused from Day 19
                            QuantitySelector(
                                quantity    = uiState.quantity,
                                onIncrement = viewModel::onIncrementQuantity,
                                onDecrement = viewModel::onDecrementQuantity,
                            )

                            Spacer(Modifier.weight(1f))

                            // Live total price
                            Text(
                                text       = "Total: ₹${
                                    uiState.totalPrice.toInt()
                                }",
                                style      = MaterialTheme.typography
                                    .titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        // ── Add to Cart Button ────────────────
                        Button(
                            onClick  = viewModel::onAddToCart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.ButtonHeight),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = ZomatoRed,
                            ),
                            shape    = RoundedCornerShape(
                                Dimens.RadiusM
                            ),
                        ) {
                            Text(
                                // Live price shown on button
                                // Updates as user selects options + qty
                                text       = "Add to Cart  •  ₹${
                                    uiState.totalPrice.toInt()
                                }",
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography
                                    .titleMedium,
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->

        when {
            // ── Loading ───────────────────────────────────────
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ZomatoRed)
                }
            }

            // ── Error ─────────────────────────────────────────
            uiState.error != null -> {
                ErrorScreen(
                    message = uiState.error!!,
                    onRetry = { /* retry */ },
                )
            }

            // ── Content ───────────────────────────────────────
            uiState.item != null -> {
                val item = uiState.item!!

                // WHY LazyColumn?
                // Customisation groups can be many
                // Long items need scrolling
                // LazyColumn = only renders visible items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {

                    // ── 1. Hero Image ─────────────────────────
                    item(key = "hero_image") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                        ) {
                            AsyncImage(
                                model              = item.imageUrl,
                                contentDescription = item.name,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .fillMaxSize(),
                            )
                        }
                    }

                    // ── 2. Item Info ──────────────────────────
                    item(key = "item_info") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpaceL),
                        ) {
                            // Veg indicator + name row
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                            ) {
                                VegDot(isVeg = item.isVeg)
                                Spacer(Modifier.width(Dimens.SpaceS))
                                Text(
                                    text       = item.name,
                                    style      = MaterialTheme.typography
                                        .headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Spacer(Modifier.height(Dimens.SpaceS))

                            // Base price
                            Text(
                                text       = "₹${item.price.toInt()}",
                                style      = MaterialTheme.typography
                                    .titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = ZomatoRed,
                            )

                            Spacer(Modifier.height(Dimens.SpaceXS))

                            // Rating + Bestseller badges
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                            ) {
                                // Rating badge
                                Surface(
                                    color = Color(0xFF3F7E00),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical   = 2.dp,
                                        ),
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.Default.Star,
                                            contentDescription = null,
                                            tint     = Color.White,
                                            modifier = Modifier
                                                .size(12.dp),
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text  = "4.5",
                                            color = Color.White,
                                            style = MaterialTheme
                                                .typography.labelSmall,
                                        )
                                    }
                                }

                                // Bestseller badge
                                if (item.isBestseller) {
                                    Spacer(Modifier.width(Dimens.SpaceS))
                                    Text(
                                        text     = "🏆 Bestseller",
                                        style    = MaterialTheme.typography
                                            .labelSmall,
                                        color    = Color(0xFF8B4513),
                                        modifier = Modifier
                                            .background(
                                                Color(0xFFFFF3E0),
                                                RoundedCornerShape(4.dp),
                                            )
                                            .padding(
                                                horizontal = 6.dp,
                                                vertical   = 2.dp,
                                            ),
                                    )
                                }
                            }

                            // Description
                            if (item.description.isNotEmpty()) {
                                Spacer(Modifier.height(Dimens.SpaceS))
                                Text(
                                    text  = item.description,
                                    style = MaterialTheme.typography
                                        .bodyMedium,
                                    color = AppGray,
                                )
                            }
                        }
                    }

                    // ── 3. Customise Header ───────────────────
                    if (item.customisations.isNotEmpty()) {
                        item(key = "customise_header") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text       = "Customise",
                                    style      = MaterialTheme.typography
                                        .titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.padding(
                                        horizontal = Dimens.SpaceL,
                                        vertical   = Dimens.SpaceM,
                                    ),
                                )
                            }
                        }

                        // ── 4. Customisation Groups ───────────
                        // Each customisation group = separate
                        // LazyColumn item with unique key
                        items(
                            items = item.customisations,
                            key   = { it.id },
                        ) { group ->
                            CustomisationGroup(
                                customisation    = group,
                                // Get selected option for THIS group
                                selectedOptionId = uiState
                                    .selectedOptions[group.id] ?: "",
                                onOptionSelected = { optionId ->
                                    // Notify ViewModel which group + option
                                    viewModel.onOptionSelected(
                                        groupId  = group.id,
                                        optionId = optionId,
                                    )
                                },
                            )
                        }
                    }

                    // Bottom padding so content not behind button
                    item(key = "bottom_space") {
                        Spacer(Modifier.height(160.dp))
                    }
                }
            }
        }
    }
}

// ── Veg indicator dot ─────────────────────────────────────────
// Reused pattern from MenuItemRow
// WHY not import from restaurant package?
//   Would create cross-package dependency
//   Better to have small private composable here
@Composable
private fun VegDot(isVeg: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(
                color = if (isVeg) VegGreen else ZomatoRed,
                shape = RoundedCornerShape(2.dp),
            ),
    )
}