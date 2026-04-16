package com.swapna.foodapp.presentation.common

import com.swapna.foodapp.presentation.ui.theme.Dimens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.Customisation

// WHY CustomisationGroup wraps multiple option rows?
// Each Customisation = one category of options
//   "Size" group → Regular, Large, Extra Large
//   "Spice" group → Mild, Medium, Hot
// Group = name + list of options
// Clean separation of concerns

@Composable
fun CustomisationGroup(
    customisation:    Customisation,
    selectedOptionId: String,           // which option selected in THIS group
    onOptionSelected: (String) -> Unit, // option id selected
    modifier:         Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {

        // ── Group name ────────────────────────────────────────
        // e.g. "Size", "Spice Level", "Add-ons"
        Text(
            text       = customisation.name,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(
                horizontal = Dimens.SpaceL,
                vertical   = Dimens.SpaceM,
            ),
        )

        // ── Options ───────────────────────────────────────────
        // Each option in this group
        customisation.options.forEach { option ->
            CustomisationOptionRow(
                option     = option,
                // Is this the currently selected option?
                isSelected = option.id == selectedOptionId,
                // When tapped → notify parent with option id
                onSelect   = { onOptionSelected(option.id) },
            )
        }

        HorizontalDivider(
            color    = Color(0xFFF0F0F0),
            modifier = Modifier.padding(top = Dimens.SpaceS),
        )
    }
}