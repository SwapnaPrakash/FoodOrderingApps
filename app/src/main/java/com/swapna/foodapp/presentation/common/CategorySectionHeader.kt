package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.utils.AppConstants.CATEGORY_SEC

@Composable
fun CategorySectionHeader(
    categoryName: String,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = CATEGORY_SEC,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.width(Dimens.SpaceS))

            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.width(Dimens.SpaceXS))

            Text(
                text = "($itemCount)",
                style = MaterialTheme.typography.bodyMedium,
                color = AppGray,
            )
        }
    }
}