package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.CategoryFooterBg
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun CategoryFooterBar(
    categories: List<String>,
    activeCategory: String,
    onCategoryTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CategoryFooterBg,
        shadowElevation = Dimens.ElevationL,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(
                        horizontal = Dimens.SpaceL,
                        vertical = Dimens.SpaceM,
                    ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            ) {
                categories.forEach { category ->
                    CategoryChipButton(
                        label = category,
                        isActive = category == activeCategory,
                        onClick = { onCategoryTap(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChipButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(Dimens.ChipHeight)
            .background(
                color = if (isActive) ZomatoRed else Color.Transparent,
                shape = RoundedCornerShape(Dimens.RadiusFull),
            )
            .border(
                width = Dimens.BorderThin,
                color = if (isActive) ZomatoRed else AppGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(Dimens.RadiusFull),
            )
            .clickable { onClick() }
            .padding(
                horizontal = Dimens.SpaceM,
                vertical = Dimens.SpaceXS,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.White else AppGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}