package com.swapna.foodapp.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Restaurants")

        Spacer(modifier = Modifier.height(10.dp))

        when {
            state.isLoading -> {
                Text("Loading...")
            }

            state.error != null -> {
                Text("Error: ${state.error}")
            }

            else -> {
                state.data.forEach {
                    Text("${it.name} ⭐ ${it.rating}")
                }
            }
        }
    }
}
