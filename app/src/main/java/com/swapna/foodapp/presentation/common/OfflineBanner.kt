package com.swapna.foodapp.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.OfflineBannerIconSize
import com.swapna.foodapp.presentation.ui.theme.OfflineBannerBg
import com.swapna.foodapp.utils.AppConstants.NO_INTERNET
import com.swapna.foodapp.utils.AppConstants.OFFLINE

@Composable
fun OfflineBanner(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OfflineBannerBg)
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceS,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = NO_INTERNET,
                tint = AppWhiteSurface,
                modifier = Modifier.size(OfflineBannerIconSize),
            )
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text = OFFLINE,
                color = AppWhiteSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}