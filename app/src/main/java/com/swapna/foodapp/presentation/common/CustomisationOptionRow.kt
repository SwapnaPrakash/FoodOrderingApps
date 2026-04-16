package com.swapna.foodapp.presentation.common

import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
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
import com.swapna.foodapp.presentation.ui.theme.Dimens

// WHY separate component for each option row?
// CustomisationGroup has multiple options
// Each option = same layout: RadioButton + name + price
// Separate component = easy to reuse + test

@Composable
fun CustomisationOptionRow(
    option:     CustomisationOption,
    isSelected: Boolean,
    onSelect:   () -> Unit,
    modifier:   Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // ✅ Entire row tappable — not just radio button
            // Better touch target for users
            .clickable { onSelect() }
            .padding(
                horizontal = Dimens.SpaceL,
                vertical   = Dimens.SpaceS,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Radio Button ──────────────────────────────────────
        // RadioButton = circle that fills when selected
        // Only ONE option per group can be selected
        RadioButton(
            selected = isSelected,
            onClick  = onSelect,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = ZomatoRed,
                unselectedColor = AppGray,
            ),
        )

        Spacer(Modifier.width(Dimens.SpaceS))

        // ── Option label ──────────────────────────────────────
        Text(
            text      = option.label,
            style     = MaterialTheme.typography.bodyMedium,
            // Bold when selected — visual emphasis
            fontWeight = if (isSelected)
                FontWeight.SemiBold
            else
                FontWeight.Normal,
            modifier  = Modifier.weight(1f),
        )

        // ── Extra price ───────────────────────────────────────
        // Show extra price only if > 0
        // 0.0 = included in base price
        Text(
            text  = when {
                option.extraPrice == 0.0 -> "Included"
                else -> "+₹${option.extraPrice.toInt()}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when {
                option.extraPrice == 0.0 -> AppGray
                isSelected               -> ZomatoRed
                else                     -> AppGray
            },
            fontWeight = if (isSelected && option.extraPrice > 0)
                FontWeight.SemiBold
            else
                FontWeight.Normal,
        )
    }
}