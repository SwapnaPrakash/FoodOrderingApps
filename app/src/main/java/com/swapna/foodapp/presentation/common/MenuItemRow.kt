package com.swapna.foodapp.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.BestsellerBgColor
import com.swapna.foodapp.presentation.ui.theme.BestsellerTextColor
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.MenuItemImageSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.MenuItemTextSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingBadgePaddingH
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingBadgePaddingV
import com.swapna.foodapp.presentation.ui.theme.Dimens.RatingBadgeRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegIndicatorBorder
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegIndicatorInner
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegIndicatorInnerRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegIndicatorPadding
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegIndicatorRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegIndicatorSize
import com.swapna.foodapp.presentation.ui.theme.DividerColor
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ADD_OR_QUANTITY_LABEL
import com.swapna.foodapp.utils.AppConstants.BESTSELLER_LABEL
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL

@Composable
fun MenuItemRow(
    item: MenuItem,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.Top,
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.SpaceM),
            ) {
                VegIndicator(isVeg = item.isVeg)

                Spacer(Modifier.height(MenuItemTextSpacing))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(MenuItemTextSpacing))

                Text(
                    text = "$CURRENCY_SYMBOL${item.price.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (item.isBestseller) {
                    Spacer(Modifier.height(MenuItemTextSpacing))
                    BestsellerBadge()
                }

                if (item.description.isNotEmpty()) {
                    Spacer(Modifier.height(MenuItemTextSpacing))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(MenuItemImageSize)
                            .clip(RoundedCornerShape(Dimens.RadiusM)),
                    )

                    // WHY AnimatedContent?
                    // Smoothly transitions ADD → qty selector when item added
                    AnimatedContent(
                        targetState = quantity > 0,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = ADD_OR_QUANTITY_LABEL,
                    ) { hasItems ->
                        if (hasItems) {
                            QuantitySelector(
                                quantity = quantity,
                                onIncrement = onIncrement,
                                onDecrement = onDecrement,
                            )
                        } else {
                            AddButton(
                                onClick = onIncrement,
                                enabled = item.isAvailable,
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = DividerColor,
            modifier = Modifier.padding(horizontal = Dimens.SpaceL),
        )
    }
}

@Composable
private fun VegIndicator(isVeg: Boolean) {
    Box(
        modifier = Modifier
            .size(VegIndicatorSize)
            .border(
                width = VegIndicatorBorder,
                color = if (isVeg) VegGreen else ZomatoRed,
                shape = RoundedCornerShape(VegIndicatorRadius),
            )
            .padding(VegIndicatorPadding),
    ) {
        Box(
            modifier = Modifier
                .size(VegIndicatorInner)
                .background(
                    color = if (isVeg) VegGreen else ZomatoRed,
                    shape = RoundedCornerShape(VegIndicatorInnerRadius),
                ),
        )
    }
}

@Composable
private fun BestsellerBadge() {
    Text(
        text = BESTSELLER_LABEL,
        style = MaterialTheme.typography.labelSmall,
        color = BestsellerTextColor,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(
                color = BestsellerBgColor,
                shape = RoundedCornerShape(RatingBadgeRadius),
            )
            .padding(
                horizontal = RatingBadgePaddingH,
                vertical = RatingBadgePaddingV,
            ),
    )
}