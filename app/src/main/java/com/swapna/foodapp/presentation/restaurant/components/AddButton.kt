package com.swapna.foodapp.presentation.restaurant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.presentation.ui.theme.VegGreen

// Zomato-style ADD button
// Green border, white background, "ADD" text + "+"
@Composable
fun AddButton(
    onClick:  () -> Unit,
    enabled:  Boolean  = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier          = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = if (enabled) VegGreen else Color.Gray,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(
                horizontal = 24.dp,
                vertical   = 6.dp,
            ),
        contentAlignment  = Alignment.Center,
    ) {
        Text(
            text       = "ADD  +",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = if (enabled) VegGreen else Color.Gray,
        )
    }
}