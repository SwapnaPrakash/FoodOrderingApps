package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.BillTextColor
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.BillShadowElevation
import com.swapna.foodapp.presentation.ui.theme.Dimens.DividerThickness
import com.swapna.foodapp.presentation.ui.theme.DividerColor
import com.swapna.foodapp.presentation.ui.theme.FreeDeliveryGreen
import com.swapna.foodapp.utils.AppConstants.BILL_DETAILS
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.DELIVERY_FEE
import com.swapna.foodapp.utils.AppConstants.FREE
import com.swapna.foodapp.utils.AppConstants.GST_CHARGES
import com.swapna.foodapp.utils.AppConstants.ITEM_TOTAL
import com.swapna.foodapp.utils.AppConstants.TO_PAY

@Composable
fun BillDetails(
    breakdown: CartPriceBreakdown,
    modifier:  Modifier = Modifier,
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = AppWhiteSurface,
        shadowElevation = BillShadowElevation,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
        ) {

            Text(
                text       = BILL_DETAILS,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            BillRow(
                label = ITEM_TOTAL,
                value = "$CURRENCY_SYMBOL${breakdown.subtotal.toInt()}",
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            BillRow(
                label      = DELIVERY_FEE,
                value      = if (breakdown.deliveryFee == 0.0) {
                    FREE
                } else {
                    "$CURRENCY_SYMBOL${breakdown.deliveryFee.toInt()}"
                },
                valueColor = if (breakdown.deliveryFee == 0.0) {
                    FreeDeliveryGreen
                } else {
                    BillTextColor
                },
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            BillRow(
                label = GST_CHARGES,
                value = "$CURRENCY_SYMBOL${breakdown.taxes.toInt()}",
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            HorizontalDivider(
                color     = DividerColor,
                thickness = DividerThickness,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            BillRow(
                label  = TO_PAY,
                value  = "$CURRENCY_SYMBOL${breakdown.total.toInt()}",
                isBold = true,
            )
        }
    }
}

@Composable
private fun BillRow(
    label:      String,
    value:      String,
    isBold:     Boolean = false,
    valueColor: Color   = BillTextColor,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            style      = if (isBold)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color      = if (isBold) BillTextColor else AppGray,
            modifier   = Modifier.weight(1f),
        )
        Text(
            text       = value,
            style      = if (isBold)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color      = valueColor,
        )
    }
}