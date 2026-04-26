package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.HeroGradientStop1
import com.swapna.foodapp.presentation.ui.theme.Dimens.HeroGradientStop2
import com.swapna.foodapp.presentation.ui.theme.Dimens.HeroGradientStop3
import com.swapna.foodapp.presentation.ui.theme.RatingExcellent
import com.swapna.foodapp.presentation.ui.theme.RatingGood
import com.swapna.foodapp.presentation.ui.theme.RatingVeryGood
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_CUISINE_TEXT
import com.swapna.foodapp.utils.AppConstants.ALPHA_HERO_OVERLAY_DARK
import com.swapna.foodapp.utils.AppConstants.ALPHA_HERO_OVERLAY_MID
import com.swapna.foodapp.utils.AppConstants.CLOSED
import com.swapna.foodapp.utils.AppConstants.COST_FOR_TWO_LABEL
import com.swapna.foodapp.utils.AppConstants.DELIVERY_TIME_LABEL
import com.swapna.foodapp.utils.AppConstants.OFFER_TAG_PREFIX
import com.swapna.foodapp.utils.AppConstants.OPEN_NOW
import com.swapna.foodapp.utils.AppConstants.RATINGS_SUFFIX
import com.swapna.foodapp.utils.AppConstants.VOTES_FORMAT
import com.swapna.foodapp.utils.AppConstants.VOTES_THOUSAND

@Composable
fun RestaurantInfoHeader(
    restaurant: Restaurant,
    cartItemCount: Int,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.RestaurantHeroHeight),
        ) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.RestaurantHeroHeight),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.RestaurantHeroHeight)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                HeroGradientStop1 to Color.Transparent,
                                HeroGradientStop2 to Color.Black.copy(
                                    alpha = ALPHA_HERO_OVERLAY_MID
                                ),
                                HeroGradientStop3 to Color.Black.copy(
                                    alpha = ALPHA_HERO_OVERLAY_DARK
                                ),
                            )
                        )
                    ),
            )
            RestaurantTopBar(
                cartItemCount = cartItemCount,
                onBack = onBack,
                onShare = onShare,
                onSearch = onSearch,
                onCart = onCart,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.SpaceL),
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Dimens.SpaceXS))
                Text(
                    text = restaurant.cuisines.take(3).joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppWhite.copy(alpha = ALPHA_CUISINE_TEXT),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppWhiteSurface,
            shadowElevation = Dimens.ElevationS,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceL),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RatingPill(rating = restaurant.rating)

                    Spacer(Modifier.width(Dimens.SpaceS))

                    Text(
                        text = "${formatVotes(restaurant.totalVotes)}$RATINGS_SUFFIX",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )

                    Spacer(Modifier.weight(1f))

                    OpenStatusBadge(isOpen = restaurant.isOpen)
                }

                Spacer(Modifier.height(Dimens.SpaceM))
                HorizontalDivider(color = AppDivider)
                Spacer(Modifier.height(Dimens.SpaceM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    InfoMetric(
                        icon = Icons.Default.AccessTime,
                        value = restaurant.deliveryTimeFormatted,
                        label = DELIVERY_TIME_LABEL,
                    )

                    Box(
                        Modifier
                            .height(Dimens.Space40)
                            .width(Dimens.BorderThin)
                            .background(AppDivider)
                    )

                    InfoMetric(
                        icon = Icons.Default.CurrencyRupee,
                        value = restaurant.costForTwoFormatted,
                        label = COST_FOR_TWO_LABEL,
                    )

                    Box(
                        Modifier
                            .height(Dimens.Space40)
                            .width(Dimens.BorderThin)
                            .background(AppDivider)
                    )

                    InfoMetric(
                        icon = Icons.Default.Star,
                        value = restaurant.ratingText,
                        label = restaurant.ratingFormatted,
                    )
                }

                if (restaurant.offers.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpaceM))
                    HorizontalDivider(color = AppDivider)
                    Spacer(Modifier.height(Dimens.SpaceM))
                    restaurant.offers.take(2).forEach { offer ->
                        Text(
                            text = "$OFFER_TAG_PREFIX$offer",
                            style = MaterialTheme.typography.labelMedium,
                            color = ZomatoRed,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = Dimens.SpaceXXS),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingPill(rating: Double) {
    val bgColor = when {
        rating >= 4.5 -> RatingExcellent
        rating >= 4.0 -> RatingVeryGood
        else -> RatingGood
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(Dimens.RadiusXS))
            .padding(
                horizontal = Dimens.RatingBadgePaddingH,
                vertical = Dimens.RatingBadgePaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(Dimens.RatingIconSize),
        )
        Spacer(Modifier.width(Dimens.SpaceXXS))
        Text(
            text = String.format("%.1f", rating),
            color = Color.White,
            fontSize = Dimens.RatingFontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OpenStatusBadge(isOpen: Boolean) {
    val (text, color) = if (isOpen) {
        OPEN_NOW to Color(0xFF30D158)
    } else {
        CLOSED to Color(0xFFFF3B30)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun InfoMetric(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppGray,
            modifier = Modifier.size(Dimens.IconS),
        )
        Spacer(Modifier.height(Dimens.SpaceXXS))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppGray,
        )
    }
}

private fun formatVotes(votes: Int): String = when {
    votes >= VOTES_THOUSAND ->
        String.format(VOTES_FORMAT, votes / VOTES_THOUSAND.toDouble())

    else -> votes.toString()
}