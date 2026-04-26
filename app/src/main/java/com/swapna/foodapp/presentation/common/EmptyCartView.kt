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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.EmptyCartEmojiSize
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.BROWSE_RESTAURANTS
import com.swapna.foodapp.utils.AppConstants.CART_EMPTY_SUBTITLE
import com.swapna.foodapp.utils.AppConstants.CART_EMPTY_TITLE
import com.swapna.foodapp.utils.AppConstants.EMOJI_CART

@Composable
fun EmptyCartView(
    onBrowseFood: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.SpaceXXL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = EMOJI_CART,
            fontSize = EmptyCartEmojiSize,
        )

        Spacer(Modifier.height(Dimens.SpaceL))

        Text(
            text = CART_EMPTY_TITLE,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        Text(
            text = CART_EMPTY_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceXXL))

        Button(
            onClick = onBrowseFood,
            colors = ButtonDefaults.buttonColors(
                containerColor = ZomatoRed,
            ),
        ) {
            Text(
                text = BROWSE_RESTAURANTS,
                color = AppWhiteSurface,
            )
        }
    }
}