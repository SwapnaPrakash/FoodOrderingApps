package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.swapna.foodapp.presentation.ui.theme.Dimens.AreaChipRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.NotServiceableButtonHeight
import com.swapna.foodapp.presentation.ui.theme.Dimens.NotServiceableCardElevation
import com.swapna.foodapp.presentation.ui.theme.Dimens.NotServiceableIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.SadEmojiSize
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_CHIP_BG
import com.swapna.foodapp.utils.AppConstants.CHANGE_DELIVERY_LOCATION
import com.swapna.foodapp.utils.AppConstants.CURRENTLY_DELIVER_IN
import com.swapna.foodapp.utils.AppConstants.EMOJI_SAD
import com.swapna.foodapp.utils.AppConstants.NOT_SERVICEABLE_PREFIX
import com.swapna.foodapp.utils.AppConstants.NOT_SERVICEABLE_SUBTITLE
import com.swapna.foodapp.utils.AppConstants.NOT_SERVICEABLE_SUFFIX
import com.swapna.foodapp.utils.AppConstants.NO_RESTAURANTS_FOUND

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotServiceableSection(
    requestedArea: String,
    availableAreas: List<String>,
    onChangeLocation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(Modifier.height(Dimens.SpaceXXL))

        Text(
            text = EMOJI_SAD,
            fontSize = SadEmojiSize,
        )

        Spacer(Modifier.height(Dimens.SpaceL))

        Text(
            text = "$NOT_SERVICEABLE_PREFIX$requestedArea$NOT_SERVICEABLE_SUFFIX",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        Text(
            text = NOT_SERVICEABLE_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceXL))

        if (availableAreas.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppWhiteSurface,
                ),
                shape = RoundedCornerShape(Dimens.RadiusM),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = NotServiceableCardElevation,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceL),
                ) {
                    Text(
                        text = CURRENTLY_DELIVER_IN,
                        style = MaterialTheme.typography.labelLarge,
                        color = AppGray,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(Dimens.SpaceM))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        availableAreas.forEach { area ->
                            AreaChip(areaName = area)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpaceXL))

        Button(
            onClick = onChangeLocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(NotServiceableButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = ZomatoRed,
            ),
            shape = RoundedCornerShape(Dimens.RadiusM),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AppWhiteSurface,
                modifier = Modifier.size(NotServiceableIconSize),
            )
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text = CHANGE_DELIVERY_LOCATION,
                color = AppWhiteSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(Dimens.Space32))
    }
}

@Composable
private fun AreaChip(areaName: String) {
    Box(
        modifier = Modifier
            .background(
                color = ZomatoRed.copy(alpha = ALPHA_CHIP_BG),
                shape = RoundedCornerShape(AreaChipRadius),
            )
            .padding(
                horizontal = Dimens.SpaceM,
                vertical = Dimens.SpaceXS,
            ),
    ) {
        Text(
            text = areaName,
            style = MaterialTheme.typography.bodySmall,
            color = ZomatoRed,
        )
    }
}

@Composable
private fun EmptyRestaurantsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceXXL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = NO_RESTAURANTS_FOUND,
            style = MaterialTheme.typography.bodyLarge,
            color = AppGray,
            textAlign = TextAlign.Center,
        )
    }
}