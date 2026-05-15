package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppBusinessRules.FREE_DELIVERY_FEE
import com.swapna.foodapp.utils.AppConstants.INCLUDED

@Composable
fun CustomisationOptionRow(
    option: CustomisationOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(
                horizontal = Dimens.SpaceL,
                vertical = Dimens.SpaceS,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = ZomatoRed,
                unselectedColor = AppGray,
            ),
        )

        Spacer(Modifier.width(Dimens.SpaceS))

        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected)
                FontWeight.SemiBold
            else
                FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = when {
                option.extraPrice == FREE_DELIVERY_FEE -> INCLUDED
                else -> "+₹${option.extraPrice.toInt()}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when {
                option.extraPrice == FREE_DELIVERY_FEE -> AppGray
                isSelected -> ZomatoRed
                else -> AppGray
            },
            fontWeight = if (isSelected && option.extraPrice > 0)
                FontWeight.SemiBold
            else
                FontWeight.Normal,
        )
    }
}