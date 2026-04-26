package com.swapna.foodapp.presentation.product

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swapna.foodapp.presentation.common.CustomisationGroup
import com.swapna.foodapp.presentation.common.ErrorScreen
import com.swapna.foodapp.presentation.common.QuantitySelector
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.BestsellerBgColor
import com.swapna.foodapp.presentation.ui.theme.BestsellerTextColor
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.ProductBottomSpace
import com.swapna.foodapp.presentation.ui.theme.Dimens.ProductHeroHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.ProductShadowElevation
import com.swapna.foodapp.presentation.ui.theme.Dimens.ProductVegDotRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.ProductVegDotSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingBadgePaddingH
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingBadgePaddingV
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingBadgeRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingStarSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingStarSpacing
import com.swapna.foodapp.presentation.ui.theme.RatingGreen
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ADD_TO_CART_PREFIX
import com.swapna.foodapp.utils.AppConstants.BACK
import com.swapna.foodapp.utils.AppConstants.BESTSELLER_LABEL
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.CUSTOMISE
import com.swapna.foodapp.utils.AppConstants.HARDCODED_RATING
import com.swapna.foodapp.utils.AppConstants.KEY_CUSTOMISE_HEADER
import com.swapna.foodapp.utils.AppConstants.KEY_HERO_IMAGE
import com.swapna.foodapp.utils.AppConstants.KEY_ITEM_INFO
import com.swapna.foodapp.utils.AppConstants.KEY_PRODUCT_BOTTOM
import com.swapna.foodapp.utils.AppConstants.TOTAL_PREFIX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductDetailViewModel.ProductDetailEvent.NavigateBack ->
                    navController.popBackStack()

                is ProductDetailViewModel.ProductDetailEvent.ShowSnackbar ->
                    snackbarHost.showSnackbar(event.message)

                is ProductDetailViewModel.ProductDetailEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = AppWhiteSurface,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.item?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
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
            if (uiState.item != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppWhiteSurface,
                    shadowElevation = ProductShadowElevation,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceM)
                            .navigationBarsPadding(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Dimens.SpaceS),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QuantitySelector(
                                quantity = uiState.quantity,
                                onIncrement = viewModel::onIncrementQuantity,
                                onDecrement = viewModel::onDecrementQuantity,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "$TOTAL_PREFIX${uiState.totalPrice.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Button(
                            onClick = viewModel::onAddToCart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.ButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomatoRed,
                            ),
                            shape = RoundedCornerShape(Dimens.RadiusM),
                        ) {
                            Text(
                                text = "$ADD_TO_CART_PREFIX${uiState.totalPrice.toInt()}",
                                color = AppWhiteSurface,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
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

            uiState.error != null -> {
                ErrorScreen(
                    message = uiState.error!!,
                    onRetry = { },
                )
            }

            uiState.item != null -> {
                val item = uiState.item!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {

                    item(key = KEY_HERO_IMAGE) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ProductHeroHeight),
                        ) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    item(key = KEY_ITEM_INFO) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpaceL),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VegDot(isVeg = item.isVeg)
                                Spacer(Modifier.width(Dimens.SpaceS))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Spacer(Modifier.height(Dimens.SpaceS))

                            Text(
                                text = "$CURRENCY_SYMBOL${item.price.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ZomatoRed,
                            )

                            Spacer(Modifier.height(Dimens.SpaceXS))

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Surface(
                                    color = RatingGreen,
                                    shape = RoundedCornerShape(RatingBadgeRadius),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = RatingBadgePaddingH,
                                            vertical = RatingBadgePaddingV,
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AppWhiteSurface,
                                            modifier = Modifier.size(RatingStarSize),
                                        )
                                        Spacer(Modifier.width(RatingStarSpacing))
                                        Text(
                                            text = HARDCODED_RATING,
                                            color = AppWhiteSurface,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }

                                if (item.isBestseller) {
                                    Spacer(Modifier.width(Dimens.SpaceS))
                                    Text(
                                        text = BESTSELLER_LABEL,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BestsellerTextColor,
                                        modifier = Modifier
                                            .background(
                                                BestsellerBgColor,
                                                RoundedCornerShape(RatingBadgeRadius),
                                            )
                                            .padding(
                                                horizontal = RatingBadgePaddingH,
                                                vertical = RatingBadgePaddingV,
                                            ),
                                    )
                                }
                            }

                            if (item.description.isNotEmpty()) {
                                Spacer(Modifier.height(Dimens.SpaceS))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppGray,
                                )
                            }
                        }
                    }

                    if (item.customisations.isNotEmpty()) {
                        item(key = KEY_CUSTOMISE_HEADER) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = CUSTOMISE,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = Dimens.SpaceL,
                                        vertical = Dimens.SpaceM,
                                    ),
                                )
                            }
                        }

                        items(
                            items = item.customisations,
                            key = { it.id },
                        ) { group ->
                            CustomisationGroup(
                                customisation = group,
                                selectedOptionId = uiState.selectedOptions[group.id] ?: "",
                                onOptionSelected = { optionId ->
                                    viewModel.onOptionSelected(
                                        groupId = group.id,
                                        optionId = optionId,
                                    )
                                },
                            )
                        }
                    }

                    item(key = KEY_PRODUCT_BOTTOM) {
                        Spacer(Modifier.height(ProductBottomSpace))
                    }
                }
            }
        }
    }
}

@Composable
private fun VegDot(isVeg: Boolean) {
    Box(
        modifier = Modifier
            .size(ProductVegDotSize)
            .background(
                color = if (isVeg) VegGreen else ZomatoRed,
                shape = RoundedCornerShape(ProductVegDotRadius),
            ),
    )
}