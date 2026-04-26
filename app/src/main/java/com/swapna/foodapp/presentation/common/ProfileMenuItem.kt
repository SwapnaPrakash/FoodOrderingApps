package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.MenuItemChevronSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.MenuItemIconSize
import com.swapna.foodapp.presentation.ui.theme.ProfileDividerColor

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = Dimens.SpaceL,
                vertical = Dimens.SpaceM,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppGray,
            modifier = Modifier.size(MenuItemIconSize),
        )

        Spacer(Modifier.width(Dimens.SpaceM))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )

        if (badge != null && badge > 0) {
            Text(
                text = badge.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = AppGray,
                modifier = Modifier.padding(end = Dimens.SpaceS),
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppGray,
            modifier = Modifier.size(MenuItemChevronSize),
        )
    }

    HorizontalDivider(
        color = ProfileDividerColor,
        modifier = Modifier.padding(
            start = Dimens.SpaceL + MenuItemIconSize + Dimens.SpaceM,
        ),
    )
}