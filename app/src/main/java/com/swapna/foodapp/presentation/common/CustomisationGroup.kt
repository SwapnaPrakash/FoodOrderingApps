package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.DividerColor

@Composable
fun CustomisationGroup(
    customisation: Customisation,
    selectedOptionId: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {

        Text(
            text = customisation.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = Dimens.SpaceL,
                vertical = Dimens.SpaceM,
            ),
        )

        customisation.options.forEach { option ->
            CustomisationOptionRow(
                option = option,
                isSelected = option.id == selectedOptionId,
                onSelect = { onOptionSelected(option.id) },
            )
        }

        HorizontalDivider(
            color = DividerColor,
            modifier = Modifier.padding(top = Dimens.SpaceS),
        )
    }
}