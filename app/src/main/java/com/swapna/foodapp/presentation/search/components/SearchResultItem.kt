package com.swapna.foodapp.presentation.search.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.common.RatingBadge
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

// Search result is a compact horizontal card (different from Home RestaurantCard)
// Matches Figma search result design — image on left, text on right

@Composable
fun SearchResultItem(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // ── Thumbnail Image (left) ────────────────────────
            AsyncImage(
                /*model              = restaurant.thumbUrl.ifEmpty { restaurant.imageUrl },
                contentDescription = restaurant.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusM)),*/

                model = ImageRequest.Builder(LocalContext.current)
                    .data(restaurant.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = restaurant.name,
                contentScale       = ContentScale.Crop,
                placeholder        = painterResource(android.R.drawable.ic_menu_gallery),
                error              = painterResource(android.R.drawable.ic_menu_gallery),
                modifier = Modifier
                    .size(Dimens.SearchResultThumb)
                    .clip(RoundedCornerShape(Dimens.RadiusS)),
            )

            Spacer(Modifier.width(Dimens.SpaceM))

            // ── Restaurant Info (right) ───────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Name + Rating on same row
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text       = restaurant.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(Dimens.SpaceS))
                    RatingBadge(rating = restaurant.rating)
                }

                Spacer(Modifier.height(Dimens.SpaceXS))

                // Cuisines
                Text(
                    text     = restaurant.cuisines.take(3).joinToString(", "),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = AppGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(Dimens.SpaceXS))

                // Delivery time + fee
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint               = AppGray,
                        modifier           = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text  = "${restaurant.avgDeliveryTime} min  •  " +
                                "₹${restaurant.deliveryFee.toInt()} delivery",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )
                }

                // Offer (if available)
                if (restaurant.offers.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpaceXS))
                    Text(
                        text  = "🏷 ${restaurant.offers.first()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZomatoRed,
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Dimens.SpaceL)
        )
    }
}