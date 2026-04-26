package com.swapna.foodapp.presentation.common

import android.R
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.DELIVERY
import com.swapna.foodapp.utils.AppConstants.DELIVERY_INFO_SEPARATOR
import com.swapna.foodapp.utils.AppConstants.OFFER_TAG_EMOJI
import com.swapna.foodapp.utils.AppConstants.URI_FALL_BACK

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val imageUrl = when {
                restaurant.thumbUrl.isNotEmpty() -> restaurant.thumbUrl
                restaurant.imageUrl.isNotEmpty() -> restaurant.imageUrl
                else -> URI_FALL_BACK
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(Dimens.SearchResultThumb)
                    .clip(RoundedCornerShape(Dimens.RadiusM)),
            )

            Spacer(Modifier.width(Dimens.SpaceM))

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
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
                    text = restaurant.cuisines.take(3).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(Dimens.SpaceXS))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = AppGray,
                        modifier = Modifier.size(Dimens.IconXXS),
                    )
                    Spacer(Modifier.width(Dimens.SpaceXXS))
                    Text(
                        text = "${restaurant.avgDeliveryTime} $DELIVERY_INFO_SEPARATOR" +
                                "$CURRENCY_SYMBOL${restaurant.deliveryFee.toInt()} " + DELIVERY,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )
                }

                if (restaurant.offers.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpaceXS))
                    Text(
                        text = "$OFFER_TAG_EMOJI ${restaurant.offers.first()}",
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