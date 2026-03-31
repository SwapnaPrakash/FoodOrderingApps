package com.swapna.foodapp.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swapna.foodapp.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val data = viewModel.restaurants.value

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Restaurants")

        Spacer(modifier = Modifier.height(10.dp))

        data.forEach {
            Text("${it.name} ⭐ ${it.rating}")
        }
    }
}