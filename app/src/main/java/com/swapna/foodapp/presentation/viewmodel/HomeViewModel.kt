package com.swapna.foodapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.usecase.GetRestaurantsUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRestaurants: GetRestaurantsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _state.value = HomeState(isLoading = true)

            try {
                val result = getRestaurants()
                _state.value = HomeState(data = result)
            } catch (e: Exception) {
                _state.value = HomeState(error = e.message)
            }
        }
    }
}

data class HomeState(
    val isLoading: Boolean = false,
    val data: List<Restaurant> = emptyList(),
    val error: String? = null
)