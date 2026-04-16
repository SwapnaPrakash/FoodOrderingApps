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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.common.RatingBadge
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.DELIVERY
import com.swapna.foodapp.utils.AppConstants.DELIVERY_TIME

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        // ✅ FIX: onClick() calls the passed-in lambda
        // NOT a hardcoded id or remembered stale lambda
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.SpaceL,
                vertical   = Dimens.SpaceS,
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.ElevationS,
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
    ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        AsyncImage(
            model = restaurant.imageUrl,
            contentDescription = restaurant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.RestaurantCardHeight)
                .clip(RoundedCornerShape(Dimens.RadiusM)),
        )
        Column(
            modifier = Modifier.padding(Dimens.SpaceM),
        ) {
            Text(
                text       = restaurant.name,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

        Row(
            modifier = Modifier.padding(top = Dimens.SpaceXS),
            verticalAlignment = Alignment.CenterVertically,
        ) {


            RatingBadge(rating = restaurant.rating)

            Text(
                text     = " • ${restaurant.cuisines.take(2)
                    .joinToString(", ")}",
                style    = MaterialTheme.typography.bodySmall,
                color    = AppGray,
                modifier = Modifier.weight(1f),
            )
        }

            Row(
                modifier = Modifier.padding(top = Dimens.SpaceXS),
            ) {
                Text(
                    text  = "${restaurant.avgDeliveryTime} mins",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
                Text(
                    text  = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
                Text(
                    text  = if (restaurant.deliveryFee == 0.0)
                        "Free delivery"
                    else
                        "₹${restaurant.deliveryFee.toInt()} delivery",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                )
            }
        }
    }
    }
}