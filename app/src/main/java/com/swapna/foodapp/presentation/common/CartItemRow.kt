package com.swapna.foodapp.presentation.common

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartItemCustomisationSpacing
import com.swapna.foodapp.presentation.ui.theme.Dimens.CartItemImageSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegDotBorderRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegDotBorderWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegDotInnerRadius
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegDotInnerSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegDotPadding
import com.swapna.foodapp.presentation.ui.theme.Dimens.VegDotSize
import com.swapna.foodapp.presentation.ui.theme.DividerColor
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.CUSTOMISATIONS_SEPARATOR
import com.swapna.foodapp.utils.AppConstants.NON_VEG
import com.swapna.foodapp.utils.AppConstants.TEXT_MAX_LINES_1
import com.swapna.foodapp.utils.AppConstants.TEXT_MAX_LINES_2
import com.swapna.foodapp.utils.AppConstants.VEG

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = cartItem.menuItem

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.SpaceM),
            ) {
                VegDot(isVeg = item.isVeg)

                Spacer(Modifier.height(Dimens.SpaceXS))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = TEXT_MAX_LINES_2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (cartItem.selectedCustomisations.isNotEmpty()) {
                    Spacer(Modifier.height(CartItemCustomisationSpacing))
                    Text(
                        text = cartItem.selectedCustomisations
                            .joinToString(CUSTOMISATIONS_SEPARATOR) { it.label },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGray,
                        maxLines = TEXT_MAX_LINES_1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(Dimens.SpaceXS))

                Text(
                    text = "$CURRENCY_SYMBOL${cartItem.totalPrice.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(CartItemImageSize)
                            .clip(RoundedCornerShape(Dimens.RadiusM)),
                    )

                    QuantitySelector(
                        quantity = cartItem.quantity,
                        onIncrement = { onIncrement(cartItem) },
                        onDecrement = { onDecrement(cartItem) },
                    )
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
private fun VegDot(isVeg: Boolean) {
    val label = if (isVeg) VEG else NON_VEG
    Box(
        modifier = Modifier
            .size(VegDotSize)
            .semantics { contentDescription = label }
            .border(
                width = VegDotBorderWidth,
                color = if (isVeg) VegGreen else ZomatoRed,
                shape = RoundedCornerShape(VegDotBorderRadius),
            )
            .padding(VegDotPadding),
    ) {
        Box(
            modifier = Modifier
                .size(VegDotInnerSize)
                .background(
                    color = if (isVeg) VegGreen else ZomatoRed,
                    shape = RoundedCornerShape(VegDotInnerRadius),
                ),
        )
    }
}