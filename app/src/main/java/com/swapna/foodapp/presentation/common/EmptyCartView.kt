package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun EmptyCartView(
    onBrowseFood: () -> Unit,
    modifier:     Modifier = Modifier,
) {
    Column(
        modifier              = modifier
            .fillMaxSize()
            .padding(Dimens.SpaceXXL),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {

        // Empty cart illustration (emoji for now)
        Text(text = "🛒", fontSize = 72.sp)

        Spacer(Modifier.height(Dimens.SpaceL))

        Text(
            text       = "Your cart is empty",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        Text(
            text      = "Add items from a restaurant\nto get started",
            style     = MaterialTheme.typography.bodyMedium,
            color     = AppGray,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceXXL))

        Button(
            onClick = onBrowseFood,
            colors  = ButtonDefaults.buttonColors(
                containerColor = ZomatoRed,
            ),
        ) {
            Text(
                text  = "Browse Restaurants",
                color = Color.White,
            )
        }
    }
}