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
import com.swapna.foodapp.utils.AppConstants.SEARCH_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchRestaurantsUseCase,
    private val restaurantRepository: RestaurantRepository,
) : ViewModel() {

    data class SearchUiState(
        val query: String = "",
        val filters: SearchFilters = SearchFilters(),
        val results: List<Restaurant> = emptyList(),
        val cuisines: List<Cuisine> = emptyList(),
        val isLoading: Boolean = false,
        val hasSearched: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Separate flows for debounce pipeline
    private val _query = MutableStateFlow("")
    private val _filters = MutableStateFlow(SearchFilters())

    init {
        loadCuisines()
        setupSearchPipeline()
    }

    // SEARCH PIPELINE
    // combine → debounce → distinctUntilChanged → flatMapLatest
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupSearchPipeline() {
        viewModelScope.launch {
            combine(_query, _filters) { query, filters ->
                query to filters
            }
                .debounce(AppConstants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .flatMapLatest { (query, filters) ->

                    // Below minimum chars → clear and skip
                    if (query.length < AppConstants.SEARCH_MIN_CHARS) {
                        _uiState.update {
                            it.copy(
                                results = emptyList(),
                                isLoading = false,
                                hasSearched = false,
                                error = null,
                            )
                        }
                        // return@flatMapLatest flowOf(null)
                        return@flatMapLatest emptyFlow()
                    }

                    // Show loading
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    // Return search flow — flatMapLatest cancels previous
                    searchUseCase(query, filters)
                        .catch { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasSearched = true,
                                    error = e.message ?: SEARCH_FAILED,
                                    results = emptyList(),
                                )
                            }
                            emit(Result.failure(e))
                        }
                }
                .collect { result ->
                    result ?: return@collect

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

    // Load Cuisines
    private fun loadCuisines() = viewModelScope.launch {
        restaurantRepository.getCuisines()
            .catch { /* non-critical — silently ignore */ }
            .collect { result ->
                result.onSuccess { cuisines ->
                    _uiState.update { it.copy(cuisines = cuisines) }
                }
            }
    }

    // User Actions
    fun onQueryChange(query: String) {
        _query.value = query
        _uiState.update { it.copy(query = query) }
    }

    fun onVegToggle() {
        val updated = _filters.value.copy(
            isVegOnly = !_filters.value.isVegOnly
        )
        _filters.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onSortChange(sort: SortOption) {
        val updated = _filters.value.copy(sortBy = sort)
        _filters.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onCuisineSelected(cuisineId: Int) {
        val newId = if (_filters.value.cuisineId == cuisineId) null else cuisineId
        val updated = _filters.value.copy(cuisineId = newId)
        _filters.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onMinRatingSelected(rating: Double?) {
        val updated = _filters.value.copy(minRating = rating)
        _filters.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onDeliveryTimeSelected(maxMinutes: Int?) {
        val updated = _filters.value.copy(maxDeliveryTime = maxMinutes)
        _filters.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun clearSearch() {
        _query.value = ""
        _uiState.update {
            it.copy(
                query = "",
                results = emptyList(),
                hasSearched = false,
                isLoading = false,
                error = null,
            )
        }
    }

    fun clearFilters() {
        val defaults = SearchFilters()
        _filters.value = defaults
        _uiState.update { it.copy(filters = defaults) }
    }
}