package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.AppLightGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "",
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ButtonHeight)
            .then(
                if (testTag.isNotEmpty())
                    Modifier.then(Modifier)
                else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = ZomatoRed,
            disabledContainerColor = AppLightGray,
        ),
        shape = RoundedCornerShape(Dimens.RadiusM),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = AppWhite,
                modifier = Modifier.size(Dimens.IconM),
                strokeWidth = Dimens.SpaceXS,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppWhite,
            )
        }
    }
}