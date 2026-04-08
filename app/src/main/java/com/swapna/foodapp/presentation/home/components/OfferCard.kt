package com.swapna.foodapp.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.GradientEnd
import com.swapna.foodapp.presentation.ui.theme.GradientStart
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun OfferCard(
    collection: Collections,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(Dimens.OfferCardWidth)
            .height(Dimens.OfferCardHeight),
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Background image
            /*AsyncImage(
                model              = collection.imageUrl,
                contentDescription = collection.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )*/

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(collection.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = collection.title,
                contentScale       = ContentScale.Crop,
                placeholder        = painterResource(android.R.drawable.ic_menu_gallery),
                error              = painterResource(android.R.drawable.ic_menu_gallery),
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(Dimens.RestaurantCardHeight)
                    .clip(RoundedCornerShape(Dimens.RadiusM)),
            )

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientStart, GradientEnd),
                        )
                    ),
            )

            // Text content overlaid on image
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.SpaceM),
            ) {
                // Discount badge (e.g. "Up to 60% OFF")
                if (collection.discount.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(ZomatoRed, RoundedCornerShape(Dimens.RadiusXS))
                            .padding(
                                horizontal = Dimens.SpaceS,   // 8dp
                                vertical   = Dimens.SpaceXXS, // 2dp — tighter than before
                            ),
                    ) {
                        Text(
                            text       = collection.discount,
                            color      = Color.White,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.SpaceXS))

                // Collection title
                Text(
                    text       = collection.title,
                    color      = Color.White,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                // Restaurant count
                Text(
                    text  = "${collection.restaurantCount} restaurants",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}