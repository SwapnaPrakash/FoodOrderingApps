package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.FilterBadgeSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.FilterBadgeTextSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.FilterIconSize
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_FILTER_ACTIVE_BG
import com.swapna.foodapp.utils.AppConstants.FILTERS_TITLE

fun SearchFilters.activeCount(): Int {
    var count = 0
    if (isVegOnly) count++
    if (minRating != null) count++
    if (maxDeliveryTime != null) count++
    if (cuisineId != null) count++
    if (sortBy != SortOption.RELEVANCE) count++
    return count
}

@Composable
fun FilterButton(
    filters: SearchFilters,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeCount = filters.activeCount()
    val hasFilters = activeCount > 0

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (hasFilters)
                ZomatoRed.copy(alpha = ALPHA_FILTER_ACTIVE_BG)
            else
                Color.Transparent,
        ),
        shape = RoundedCornerShape(Dimens.RadiusS),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = FILTERS_TITLE,
                tint = if (hasFilters)
                    ZomatoRed
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(FilterIconSize),
            )

            Spacer(Modifier.width(Dimens.SpaceXS))

            Text(
                text = FILTERS_TITLE,
                color = if (hasFilters)
                    ZomatoRed
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )

            if (hasFilters) {
                Spacer(Modifier.width(Dimens.SpaceXS))
                Box(
                    modifier = Modifier
                        .size(FilterBadgeSize)
                        .background(ZomatoRed, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = activeCount.toString(),
                        color = AppWhiteSurface,
                        fontSize = FilterBadgeTextSize,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}