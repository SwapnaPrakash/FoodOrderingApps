package com.swapna.foodapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.search.SearchRestaurantsUseCase
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.EMPTY
import com.swapna.foodapp.utils.AppConstants.FAILED_TO_LOAD
import com.swapna.foodapp.utils.AppConstants.SEARCH_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchRestaurantsUseCase,
    private val restaurantRepository: RestaurantRepository,
) : ViewModel() {

    data class SearchUiState(
        val query: String = EMPTY,
        val filters: SearchFilters = SearchFilters(),
        val results: List<Restaurant> = emptyList(),
        val cuisines: List<Cuisine> = emptyList(),
        val isLoading: Boolean = false,
        val hasSearched: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow(EMPTY)

    private val _filters = MutableStateFlow(SearchFilters())

    init {
        loadCuisines()
        setupSearchPipeline()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupSearchPipeline() {
        viewModelScope.launch {
            combine(_query, _filters) { query, filters -> query to filters }
                .debounce(AppConstants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .flatMapLatest { (query, filters) ->
                    if (query.length < AppConstants.SEARCH_MIN_CHARS) {
                        _uiState.update {
                            it.copy(
                                results = emptyList(),
                                isLoading = false,
                                hasSearched = false,
                                error = null,
                            )
                        }
                        return@flatMapLatest emptyFlow()
                    }
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    searchUseCase(query, filters)
                        .catch { e -> emit(Result.failure(e)) }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { restaurants ->
                            _uiState.update {
                                it.copy(
                                    results = restaurants,
                                    isLoading = false,
                                    hasSearched = true,
                                    error = null,
                                )
                            }
                        },
                        onFailure = { throwable ->
                            if (throwable is CancellationException) throw throwable
                            _uiState.update {
                                it.copy(
                                    results = emptyList(),
                                    isLoading = false,
                                    hasSearched = true,
                                    error = throwable.message ?: SEARCH_FAILED,
                                )
                            }
                        },
                    )
                }
        }
    }

    private fun loadCuisines() = viewModelScope.launch {
        restaurantRepository.getCuisines()
            .catch { e -> Timber.e(e, FAILED_TO_LOAD) }
            .collect { result ->
                result.onSuccess { cuisines ->
                    _uiState.update { it.copy(cuisines = cuisines) }
                }
            }
    }

    fun onQueryChange(query: String) {
        _query.value = query
        _uiState.update { it.copy(query = query) }
    }

    fun onVegToggle() {
        _filters.update { it.copy(isVegOnly = !it.isVegOnly) }
        _uiState.update { it.copy(filters = _filters.value) }
    }

    fun onSortChange(sort: SortOption) {
        _filters.update { it.copy(sortBy = sort) }
        _uiState.update { it.copy(filters = _filters.value) }
    }

    fun onCuisineSelected(cuisineId: Int) {
        _filters.update {
            it.copy(cuisineId = if (it.cuisineId == cuisineId) null else cuisineId)
        }
        _uiState.update { it.copy(filters = _filters.value) }
    }

    fun onMinRatingSelected(rating: Double?) {
        _filters.update { it.copy(minRating = rating) }
        _uiState.update { it.copy(filters = _filters.value) }
    }

    fun onDeliveryTimeSelected(maxMinutes: Int?) {
        _filters.update { it.copy(maxDeliveryTime = maxMinutes) }
        _uiState.update { it.copy(filters = _filters.value) }
    }

    fun clearSearch() {
        _query.value = EMPTY
        _uiState.update {
            it.copy(
                query = EMPTY,
                results = emptyList(),
                hasSearched = false,
                isLoading = false,
                error = null,
            )
        }
    }

    fun clearFilters() {
        val defaults = SearchFilters()
        _filters.update { defaults }
        _uiState.update { it.copy(filters = defaults) }
    }
}