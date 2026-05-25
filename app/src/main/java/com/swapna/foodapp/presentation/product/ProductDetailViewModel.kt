package com.swapna.foodapp.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.di.IoDispatcher
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppConstants.ARG_MENU_ITEM_ID_REQUIRED
import com.swapna.foodapp.utils.AppConstants.ARG_RESTAURANT_ID_REQUIRED
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_ADD_CART
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_LOAD_ITEM
import com.swapna.foodapp.utils.AppConstants.ERR_ITEM_NOT_FOUND
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_NAVIGATION
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_UI
import com.swapna.foodapp.utils.AppConstants.MSG_ADDED_TO_CART
import com.swapna.foodapp.utils.AppConstants.PROD_MERGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val restaurantRepository: RestaurantRepository,
    private val addToCartUseCase: AddToCartUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val restaurantId: String =
        checkNotNull(savedStateHandle[AppRoutes.ARG_RESTAURANT_ID]) {
            ARG_RESTAURANT_ID_REQUIRED
        }

    private val menuItemId: String =
        checkNotNull(savedStateHandle[AppRoutes.ARG_MENU_ITEM_ID]) {
            ARG_MENU_ITEM_ID_REQUIRED
        }

    data class ProductDetailUiState(
        val item: MenuItem? = null,
        val selectedOptions: Map<String, String> = emptyMap(),
        val quantity: Int = 1,
        val totalPrice: Double = 0.0,
        val isLoading: Boolean = true,
        val error: String? = null,
    )

    sealed class ProductDetailEvent {
        object NavigateBack : ProductDetailEvent()
        data class ShowSnackbar(val message: String) : ProductDetailEvent()
        data class ShowError(val message: String) : ProductDetailEvent()
    }

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<ProductDetailEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    private val _uiEvents = MutableSharedFlow<ProductDetailEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_UI,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _mergedEvents = MutableSharedFlow<ProductDetailEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val events: SharedFlow<ProductDetailEvent> = _mergedEvents.asSharedFlow()

    private val mergeObserverHandler =
        CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, PROD_MERGE)
        }

    init {
        loadMenuItem()
        viewModelScope.launch(mergeObserverHandler) {
            _navigationEvents.collect { _mergedEvents.emit(it) }
        }
        viewModelScope.launch(mergeObserverHandler) {
            _uiEvents.collect { _mergedEvents.emit(it) }
        }
    }

    private fun loadMenuItem() = viewModelScope.launch {
        restaurantRepository.getMenuItems(restaurantId).collect { result ->
            result.fold(
                onSuccess = { menuMap ->
                    val item = menuMap.values.flatten().find { it.id == menuItemId }
                    if (item == null) {
                        _uiState.update {
                            it.copy(isLoading = false, error = ERR_ITEM_NOT_FOUND)
                        }
                        return@collect
                    }
                    val defaultSelections = buildDefaultSelections(item)
                    _uiState.update {
                        it.copy(
                            item = item,
                            selectedOptions = defaultSelections,
                            isLoading = false,
                            totalPrice = computeTotal(item, defaultSelections, 1),
                        )
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: ERR_FAILED_LOAD_ITEM,
                        )
                    }
                }
            )
        }
    }

    private fun buildDefaultSelections(item: MenuItem): Map<String, String> =
        item.customisations.associate { group ->
            group.id to (group.options.firstOrNull()?.id ?: "")
        }

    private fun computeTotal(
        item: MenuItem,
        selectedOptions: Map<String, String>,
        quantity: Int,
    ): Double {
        val extras = item.customisations.sumOf { group ->
            val selectedOptionId = selectedOptions[group.id] ?: return@sumOf 0.0
            group.options.find { it.id == selectedOptionId }?.extraPrice ?: 0.0
        }
        return (item.price + extras) * quantity
    }

    fun onOptionSelected(groupId: String, optionId: String) {
        val item = _uiState.value.item ?: return
        val newSelections = _uiState.value.selectedOptions + (groupId to optionId)
        _uiState.update {
            it.copy(
                selectedOptions = newSelections,
                totalPrice = computeTotal(item, newSelections, it.quantity),
            )
        }
    }

    fun onIncrementQuantity() {
        val item = _uiState.value.item ?: return
        val newQty = (_uiState.value.quantity + 1)
            .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY)
            .coerceAtLeast(1)
        _uiState.update {
            it.copy(
                quantity = newQty,
                totalPrice = computeTotal(item, it.selectedOptions, newQty)
            )
        }
    }

    fun onDecrementQuantity() {
        val item = _uiState.value.item ?: return
        val newQty = (_uiState.value.quantity - 1)
            .coerceAtLeast(1)
            .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY)
        _uiState.update {
            it.copy(quantity = newQty, totalPrice = computeTotal(item, it.selectedOptions, newQty))
        }
    }

    fun onAddToCart() = viewModelScope.launch {
        val item = _uiState.value.item ?: run {
            _uiEvents.emit(ProductDetailEvent.ShowError(ERR_ITEM_NOT_FOUND))
            return@launch
        }
        val state = _uiState.value
        val selectedCustomisations: List<CustomisationOption> =
            item.customisations.mapNotNull { group ->
                val selectedId = state.selectedOptions[group.id]
                group.options.find { it.id == selectedId }
            }
        try {
            withContext(ioDispatcher) {
                addToCartUseCase(
                    menuItem = item,
                    quantity = state.quantity,
                    customisations = selectedCustomisations,
                )
            }
            _uiEvents.emit(ProductDetailEvent.ShowSnackbar("${item.name}$MSG_ADDED_TO_CART"))
            _navigationEvents.emit(ProductDetailEvent.NavigateBack)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiEvents.emit(ProductDetailEvent.ShowError(e.message ?: ERR_FAILED_ADD_CART))
        }
    }

    fun onBackPressed() = viewModelScope.launch {
        _navigationEvents.emit(ProductDetailEvent.NavigateBack)
    }
}