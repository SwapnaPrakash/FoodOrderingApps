package com.swapna.foodapp.presentation.restaurant.components

import android.util.Log
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.RatingExcellent
import com.swapna.foodapp.presentation.ui.theme.RatingGood
import com.swapna.foodapp.presentation.ui.theme.RatingVeryGood
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

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

        // ══════════════════════════════════════════════════════
        // HERO IMAGE — full width with gradient overlay
        // ══════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.RestaurantHeroHeight),
        ) {
            Log.d("CLICK", "Test "+ {restaurant.name}.toString() + {restaurant.imageUrl})
            // Hero banner image
            AsyncImage(
                model              = restaurant.imageUrl,
                contentDescription = restaurant.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxWidth().height(Dimens.RestaurantHeroHeight),
            )

            // Dark gradient overlay — bottom half only
            // Makes restaurant name readable on any image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.RestaurantHeroHeight)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Black.copy(alpha = 0.2f),
                                1.0f to Color.Black.copy(alpha = 0.7f),
                            )
                        )
                    ),
            )

            // Top bar — overlaid on hero image
            RestaurantTopBar(
                cartItemCount = cartItemCount,
                onBack        = onBack,
                onShare       = onShare,
                onSearch      = onSearch,
                onCart        = onCart,
                modifier      = Modifier.align(Alignment.TopCenter),
            )

            // Restaurant name — overlaid at bottom of hero
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.SpaceL),
            ) {
                Text(
                    text       = restaurant.name,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = AppWhite,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Dimens.SpaceXS))
                Text(
                    text    = restaurant.cuisines.take(3).joinToString(", "),
                    style   = MaterialTheme.typography.bodyMedium,
                    color   = AppWhite.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // ══════════════════════════════════════════════════════
        // INFO CARD — rating + delivery + cost
        // White card below hero image
        // ══════════════════════════════════════════════════════
        Surface(
            modifier      = Modifier.fillMaxWidth(),
            color         = Color.White,
            shadowElevation = Dimens.ElevationS,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceL),
            ) {

                // ── Row 1: Rating + Votes ──────────────────────
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Rating pill — green background
                    RatingPill(rating = restaurant.rating)

                    Spacer(Modifier.width(Dimens.SpaceS))

                    Text(
                        text  = "${formatVotes(restaurant.totalVotes)} ratings",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                    )

                    Spacer(Modifier.weight(1f))

                    // Open/Closed indicator
                    OpenStatusBadge(isOpen = restaurant.isOpen)
                }

                Spacer(Modifier.height(Dimens.SpaceM))
                HorizontalDivider(color = AppDivider)
                Spacer(Modifier.height(Dimens.SpaceM))

                // ── Row 2: Delivery time + Cost for two ────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    InfoMetric(
                        icon  = Icons.Default.AccessTime,
                        value = restaurant.deliveryTimeFormatted,
                        label = "Delivery Time",
                    )

                    // Vertical divider
                    Box(
                        Modifier
                            .height(Dimens.Space40)
                            .width(Dimens.BorderThin)
                            .background(AppDivider)
                    )

                    InfoMetric(
                        icon  = Icons.Default.CurrencyRupee,
                        value = restaurant.costForTwoFormatted,
                        label = "Cost for two",
                    )

                    // Vertical divider
                    Box(
                        Modifier
                            .height(Dimens.Space40)
                            .width(Dimens.BorderThin)
                            .background(AppDivider)
                    )

                    InfoMetric(
                        icon  = Icons.Default.Star,
                        value = restaurant.ratingText,
                        label = restaurant.ratingFormatted,
                    )
                }

                // ── Offer tags ─────────────────────────────────
                if (restaurant.offers.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpaceM))
                    HorizontalDivider(color = AppDivider)
                    Spacer(Modifier.height(Dimens.SpaceM))
                    restaurant.offers.take(2).forEach { offer ->
                        Text(
                            text       = "🏷 $offer",
                            style      = MaterialTheme.typography.labelMedium,
                            color      = ZomatoRed,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.padding(vertical = Dimens.SpaceXXS),
                        )
                    }
                }
            }
        }
    }
}

// ── Rating Pill ────────────────────────────────────────────────
@Composable
private fun RatingPill(rating: Double) {
    val bgColor = when {
        rating >= 4.5 -> RatingExcellent
        rating >= 4.0 -> RatingVeryGood
        else          -> RatingGood
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(Dimens.RadiusXS))
            .padding(
                horizontal = Dimens.RatingBadgePaddingH,
                vertical   = Dimens.RatingBadgePaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Default.Star,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier.size(Dimens.RatingIconSize),
        )
        Spacer(Modifier.width(Dimens.SpaceXXS))
        Text(
            text       = String.format("%.1f", rating),
            color      = Color.White,
            fontSize   = Dimens.RatingFontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Open / Closed badge ────────────────────────────────────────
@Composable
private fun OpenStatusBadge(isOpen: Boolean) {
    val (text, color) = if (isOpen) {
        "Open Now" to Color(0xFF30D158)
    } else {
        "Closed"   to Color(0xFFFF3B30)
    }
    Text(
        text  = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.SemiBold,
    )
}

// ── Info metric box (delivery time, cost etc) ─────────────────
@Composable
private fun InfoMetric(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = AppGray,
            modifier           = Modifier.size(Dimens.IconS),
        )
        Spacer(Modifier.height(Dimens.SpaceXXS))
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppGray,
        )
    }
}

// ── Format votes: 12547 → "12.5K" ────────────────────────────
private fun formatVotes(votes: Int): String = when {
    votes >= 1000 -> String.format("%.1fK", votes / 1000.0)
    else          -> votes.toString()
}