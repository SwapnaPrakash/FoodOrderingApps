package com.swapna.foodapp.utils

import com.swapna.foodapp.presentation.ui.theme.Dimens
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier   = modifier.padding(
            start  = Dimens.SpaceL,
            end    = Dimens.SpaceL,
            top    = Dimens.SpaceL,
            bottom = Dimens.SpaceS,
        ),
    )
}