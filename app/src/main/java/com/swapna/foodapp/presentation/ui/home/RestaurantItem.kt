package com.swapna.foodapp.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.Restaurant

@Composable
fun RestaurantItem(restaurant: Restaurant) {
    Card {
        Column {
            Text(text = restaurant.name)
            Text(text = "⭐ ${restaurant.rating}")
        }
    }
}