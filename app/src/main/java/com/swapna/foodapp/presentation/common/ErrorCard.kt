package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ErrorRed
import com.swapna.foodapp.presentation.ui.theme.ErrorRedBg
import com.swapna.foodapp.utils.AppConstants.ERROR

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = ErrorRedBg,
        shape = RoundedCornerShape(Dimens.RadiusS),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.SpaceM,
                vertical = Dimens.SpaceS,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = ERROR,
                tint = ErrorRed,
                modifier = Modifier.size(Dimens.IconS),
            )
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}