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
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens

// WHY separate BillDetails component?
// Used on CartScreen AND OrderConfirmationScreen
// Reusable — pass different CartPriceBreakdown each time

@Composable
fun BillDetails(
    breakdown: CartPriceBreakdown,
    modifier:  Modifier = Modifier,
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
        ) {

            Text(
                text       = "Bill Details",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── Item total ────────────────────────────────────
            BillRow(
                label = "Item total",
                value = "₹${breakdown.subtotal.toInt()}",
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            // ── Delivery fee ──────────────────────────────────
            BillRow(
                label = "Delivery fee",
                value = if (breakdown.deliveryFee == 0.0) {
                    "FREE"
                } else {
                    "₹${breakdown.deliveryFee.toInt()}"
                },
                valueColor = if (breakdown.deliveryFee == 0.0) {
                    Color(0xFF3D9B35) // green for FREE
                } else {
                    Color.Black
                },
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            // ── GST ───────────────────────────────────────────
            BillRow(
                label = "GST & charges",
                value = "₹${breakdown.taxes.toInt()}",
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            HorizontalDivider(
                color     = Color(0xFFF0F0F0),
                thickness = 1.dp,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── Total ─────────────────────────────────────────
            // Bold + larger — most important number
            BillRow(
                label      = "To Pay",
                value      = "₹${breakdown.total.toInt()}",
                isBold     = true,
            )
        }
    }
}

// ── Single bill row ───────────────────────────────────────────
@Composable
private fun BillRow(
    label:      String,
    value:      String,
    isBold:     Boolean = false,
    valueColor: Color   = Color.Black,
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
            color      = if (isBold) Color.Black else AppGray,
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