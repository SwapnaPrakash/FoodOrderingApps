package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.AvatarIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.AvatarSize
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_AVATAR_BG
import com.swapna.foodapp.utils.AppConstants.PHONE_COUNTRY_CODE
import com.swapna.foodapp.utils.AppConstants.PROFILE_ICON_DESC

@Composable
fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppWhiteSurface)
            .padding(Dimens.SpaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .clip(CircleShape)
                .background(ZomatoRed.copy(alpha = ALPHA_AVATAR_BG)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = PROFILE_ICON_DESC,
                tint = ZomatoRed,
                modifier = Modifier.size(AvatarIconSize),
            )
        }

        Spacer(Modifier.height(Dimens.SpaceM))

        Text(
            text = user.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Dimens.SpaceXS))

        if (user.email.isNotEmpty()) {
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = AppGray,
            )
            Spacer(Modifier.height(Dimens.SpaceXS))
        }

        Text(
            text = "$PHONE_COUNTRY_CODE ${user.phone}",
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
        )
    }
}