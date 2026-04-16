package com.swapna.foodapp.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.EVENT_BUFFER_DEFAULT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// WHY separate ProductDetailViewModel?
// Single Responsibility Principle:
//   RestaurantViewModel → menu list browsing
//   ProductDetailViewModel → single item detail
//                          + customisation selection
//                          + quantity management
//                          + price calculation
// Each VM does ONE job well

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle:                 SavedStateHandle,
    private val restaurantRepository: RestaurantRepository,
    private val addToCartUseCase:     AddToCartUseCase,
) : ViewModel() {

    // ── Get route arguments ───────────────────────────────────
    // SavedStateHandle reads from navigation arguments
    // checkNotNull = crash loudly if missing
    // Better than null check everywhere downstream
    private val restaurantId: String =
        checkNotNull(
            savedStateHandle[AppRoutes.ARG_RESTAURANT_ID]
        ) { "restaurantId required" }

    private val menuItemId: String =
        checkNotNull(
            savedStateHandle[AppRoutes.ARG_MENU_ITEM_ID]
        ) { "menuItemId required" }

    // ══════════════════════════════════════════════════════════
    // UI STATE
    // Single data class — everything UI needs
    // ══════════════════════════════════════════════════════════
    data class ProductDetailUiState(
        val item: MenuItem? = null,

        // Map<groupId, selectedOptionId>
        // WHY Map not List?
        //   Fast O(1) lookup: which option selected for group X?
        //   selectedOptions["size"] = "large"
        //   selectedOptions["spice"] = "medium"
        val selectedOptions: Map<String, String> = emptyMap(),

        val quantity: Int = 1,   // starts at 1, never below 1

        // Computed total price shown on Add to Cart button
        // = (base price + sum of selected option extras) × qty
        val totalPrice: Double = 0.0,

        val isLoading: Boolean = true,
        val error:     String? = null,
    )

    // ══════════════════════════════════════════════════════════
    // EVENTS — one-time actions
    // ══════════════════════════════════════════════════════════
    sealed class ProductDetailEvent {
        // After successfully adding to cart
        object NavigateBack : ProductDetailEvent()

        // Show snackbar feedback
        data class ShowSnackbar(
            val message: String,
        ) : ProductDetailEvent()

        // Show error
        data class ShowError(
            val message: String,
        ) : ProductDetailEvent()
    }

    private val _uiState =
        MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> =
        _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_DEFAULT,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ProductDetailEvent> =
        _events.asSharedFlow()

    init {
        loadMenuItem()
    }

    // ══════════════════════════════════════════════════════════
    // LOAD MENU ITEM
    // Finds the specific item from the restaurant menu
    // ══════════════════════════════════════════════════════════
    private fun loadMenuItem() = viewModelScope.launch {
        // Get full menu for this restaurant
        // WHY use getMenuItems not getMenuItem?
        //   Our mock API returns full menu map
        //   No single-item endpoint in our API
        //   In production: would call /menu/{itemId}
        restaurantRepository
            .getMenuItems(restaurantId)
            .collect { result ->
                result.fold(
                    onSuccess = { menuMap ->
                        // Flatten all categories to find our item
                        // menuMap = Map<String, List<MenuItem>>
                        // .values = all lists of items
                        // .flatten() = one flat list of all items
                        val item = menuMap.values
                            .flatten()
                            .find { it.id == menuItemId }

                        if (item == null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error     = "Item not found",
                                )
                            }
                            return@collect
                        }

                        // Set default selections
                        // WHY set defaults?
                        //   First option of each group
                        //   pre-selected (Zomato behavior)
                        //   User doesn't HAVE to change anything
                        val defaultSelections =
                            buildDefaultSelections(item)

                        _uiState.update {
                            it.copy(
                                item            = item,
                                selectedOptions = defaultSelections,
                                isLoading       = false,
                                totalPrice      = computeTotal(
                                    item,
                                    defaultSelections,
                                    1,
                                ),
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message
                                    ?: "Failed to load item",
                            )
                        }
                    }
                )
            }
    }

    // ── Build default selections ──────────────────────────────
    // For each customisation group → select first option
    // WHY first option?
    //   Standard UX — something must be pre-selected
    //   User can always change it
    private fun buildDefaultSelections(
        item: MenuItem,
    ): Map<String, String> =
        item.customisations.associate { group ->
            // Key   = group.id (e.g. "size_group")
            // Value = first option id (e.g. "regular")
            group.id to (group.options.firstOrNull()?.id ?: "")
        }

    // ── Compute total price ───────────────────────────────────
    // (basePrice + extras from selected options) × qty
    // WHY called on every selection change?
    //   Price shown on button updates live
    //   User sees cost of their choices immediately
    private fun computeTotal(
        item:            MenuItem,
        selectedOptions: Map<String, String>,
        quantity:        Int,
    ): Double {
        // Sum extra prices of all selected options
        val extras = item.customisations.sumOf { group ->
            val selectedOptionId =
                selectedOptions[group.id] ?: return@sumOf 0.0
            // Find selected option in this group
            val option = group.options.find {
                it.id == selectedOptionId
            }
            option?.extraPrice ?: 0.0
        }

        // (base + extras) × quantity
        return (item.price + extras) * quantity
    }

    // ══════════════════════════════════════════════════════════
    // USER ACTIONS
    // ══════════════════════════════════════════════════════════

    // User selects a customisation option
    // e.g. taps "Large" in Size group
    fun onOptionSelected(
        groupId:  String,  // which group changed
        optionId: String,  // which option selected
    ) {
        val item = _uiState.value.item ?: return

        // Build new selections map with this change
        // + operator on Map = add/replace key-value pair
        val newSelections = _uiState.value.selectedOptions +
                (groupId to optionId)

        _uiState.update {
            it.copy(
                selectedOptions = newSelections,
                // ✅ Recompute total immediately
                // Button price updates as user selects
                totalPrice = computeTotal(
                    item,
                    newSelections,
                    it.quantity,
                ),
            )
        }
    }

    // User taps + button
    fun onIncrementQuantity() {
        val current = _uiState.value.quantity
        // coerceAtMost = can't go above MAX
        val newQty  = (current + 1)
            .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY)

        val item = _uiState.value.item ?: return

        _uiState.update {
            it.copy(
                quantity   = newQty,
                totalPrice = computeTotal(
                    item,
                    it.selectedOptions,
                    newQty,
                ),
            )
        }
    }

    // User taps - button
    fun onDecrementQuantity() {
        val current = _uiState.value.quantity
        // coerceAtLeast(1) = minimum qty is 1
        // WHY not 0? User can't add 0 items to cart
        // Use remove button for that
        val newQty  = (current - 1).coerceAtLeast(1)

        val item = _uiState.value.item ?: return

        _uiState.update {
            it.copy(
                quantity   = newQty,
                totalPrice = computeTotal(
                    item,
                    it.selectedOptions,
                    newQty,
                ),
            )
        }
    }

    // User taps "Add to Cart" button
    fun onAddToCart() = viewModelScope.launch {
        val item = _uiState.value.item ?: run {
            _events.emit(
                ProductDetailEvent.ShowError("Item not found")
            )
            return@launch
        }

        val state = _uiState.value

        // Build list of SELECTED customisation options
        // to pass to AddToCartUseCase
        val selectedCustomisations: List<CustomisationOption> =
            item.customisations.mapNotNull { group ->
                val selectedId =
                    state.selectedOptions[group.id]
                group.options.find { it.id == selectedId }
            }

        try {
            addToCartUseCase(
                menuItem       = item,
                quantity       = state.quantity,
                customisations = selectedCustomisations,
            )

            // Show success feedback
            _events.emit(
                ProductDetailEvent.ShowSnackbar(
                    "${item.name} added to cart 🛒"
                )
            )

            // Go back to restaurant screen
            _events.emit(ProductDetailEvent.NavigateBack)

        } catch (e: Exception) {
            _events.emit(
                ProductDetailEvent.ShowError(
                    e.message ?: "Failed to add to cart"
                )
            )
        }
    }

    fun onBackPressed() = viewModelScope.launch {
        _events.emit(ProductDetailEvent.NavigateBack)
    }
}