package com.swapna.foodapp.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.common.RatingBadge
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.DELIVERY
import com.swapna.foodapp.utils.AppConstants.DELIVERY_TIME

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceL),
    ) {
        Spacer(Modifier.height(Dimens.SpaceM))

        AsyncImage(
            model = restaurant.imageUrl,
            contentDescription = restaurant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.RestaurantCardHeight)
                .clip(RoundedCornerShape(Dimens.RadiusM)),
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.width(Dimens.SpaceS))

            RatingBadge(rating = restaurant.rating)
        }

        Spacer(Modifier.height(Dimens.SpaceXS))

        Text(
            text = restaurant.cuisines.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            color = AppGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(Dimens.SpaceXS))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = DELIVERY_TIME,
                tint = AppGray,
                modifier = Modifier.size(Dimens.IconXXS),
            )
            Spacer(Modifier.width(Dimens.SpaceXS))
            Text(
                text = "${restaurant.avgDeliveryTime} min",
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
            Text(
                text = "  •  ",
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
            Text(
                text = "₹${restaurant.deliveryFee.toInt()} $DELIVERY",
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
        }

        if (restaurant.offers.isNotEmpty()) {
            Spacer(Modifier.height(Dimens.SpaceXS))
            Text(
                text = "🏷 ${restaurant.offers.first()}",
                style = MaterialTheme.typography.labelMedium,
                color = ZomatoRed,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.height(Dimens.SpaceXS))

        HorizontalDivider()
    }
}