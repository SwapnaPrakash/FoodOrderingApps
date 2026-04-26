package com.swapna.foodapp.presentation.common

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
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens.AddButtonBorderWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.AddButtonPaddingH
import com.swapna.foodapp.presentation.ui.theme.Dimens.AddButtonPaddingV
import com.swapna.foodapp.presentation.ui.theme.Dimens.AddButtonRadius
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.utils.AppConstants.ADD_LABEL

@Composable
fun AddButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = AppWhiteSurface,
                shape = RoundedCornerShape(AddButtonRadius),
            )
            .border(
                width = AddButtonBorderWidth,
                color = if (enabled) VegGreen else Color.Gray,
                shape = RoundedCornerShape(AddButtonRadius),
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(
                horizontal = AddButtonPaddingH,
                vertical = AddButtonPaddingV,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = ADD_LABEL,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (enabled) VegGreen else Color.Gray,
        )
    }
}