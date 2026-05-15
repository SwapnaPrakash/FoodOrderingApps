package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.SortOptionCheckSize
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ACTIVE
import com.swapna.foodapp.utils.AppConstants.ALPHA_FILTER_CHIP_SELECTED
import com.swapna.foodapp.utils.AppConstants.ALPHA_VEG_SWITCH_TRACK
import com.swapna.foodapp.utils.AppConstants.APPLY
import com.swapna.foodapp.utils.AppConstants.CLEAR_ALL
import com.swapna.foodapp.utils.AppConstants.DELIVERY_MINUTES_20
import com.swapna.foodapp.utils.AppConstants.DELIVERY_MINUTES_30
import com.swapna.foodapp.utils.AppConstants.DELIVERY_MINUTES_45
import com.swapna.foodapp.utils.AppConstants.DELIVERY_UNDER_20
import com.swapna.foodapp.utils.AppConstants.DELIVERY_UNDER_30
import com.swapna.foodapp.utils.AppConstants.DELIVERY_UNDER_45
import com.swapna.foodapp.utils.AppConstants.EMOJI_STAR
import com.swapna.foodapp.utils.AppConstants.FILTERS_TITLE
import com.swapna.foodapp.utils.AppConstants.FILTER_CUISINES
import com.swapna.foodapp.utils.AppConstants.FILTER_DELIVERY_TIME
import com.swapna.foodapp.utils.AppConstants.FILTER_DIETARY
import com.swapna.foodapp.utils.AppConstants.FILTER_MIN_RATING
import com.swapna.foodapp.utils.AppConstants.FILTER_SORT_BY
import com.swapna.foodapp.utils.AppConstants.RATING_EXCELLENT
import com.swapna.foodapp.utils.AppConstants.RATING_GOOD
import com.swapna.foodapp.utils.AppConstants.RATING_VERY_GOOD
import com.swapna.foodapp.utils.AppConstants.SELECTED
import com.swapna.foodapp.utils.AppConstants.VEG_ONLY

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    filters: SearchFilters,
    cuisines: List<Cuisine>,
    onVegToggle: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    onCuisineSelected: (Int) -> Unit,
    onMinRatingSelected: (Double?) -> Unit,
    onDeliveryTimeSelected: (Int?) -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimens.Space32),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = FILTERS_TITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                val count = filters.activeCount()
                if (count > 0) {
                    Text(
                        text = "$count $ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZomatoRed,
                    )
                }
            }

            HorizontalDivider()

            FilterSection(title = FILTER_DIETARY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = VEG_ONLY,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = filters.isVegOnly,
                        onCheckedChange = { onVegToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VegGreen,
                            checkedTrackColor = VegGreen.copy(alpha = ALPHA_VEG_SWITCH_TRACK),
                        ),
                    )
                }
            }

            HorizontalDivider()

            FilterSection(title = FILTER_MIN_RATING) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.SpaceS
                    )
                ) {
                    listOf(RATING_GOOD, RATING_VERY_GOOD, RATING_EXCELLENT).forEach { rating ->
                        FilterChip(
                            selected = filters.minRating == rating,
                            onClick = {
                                onMinRatingSelected(
                                    if (filters.minRating == rating) null else rating
                                )
                            },
                            label = { Text("$rating $EMOJI_STAR") },
                            leadingIcon = if (filters.minRating == rating) {
                                {
                                    Icon(Icons.Default.Check, null)
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZomatoRed.copy(alpha = 0.12f),
                                selectedLabelColor = ZomatoRed,
                            ),
                        )
                    }
                }
            }

            HorizontalDivider()

            FilterSection(title = FILTER_DELIVERY_TIME) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.SpaceS
                    )
                ) {
                    listOf(
                        DELIVERY_MINUTES_20 to DELIVERY_UNDER_20,
                        DELIVERY_MINUTES_30 to DELIVERY_UNDER_30,
                        DELIVERY_MINUTES_45 to DELIVERY_UNDER_45,
                    ).forEach { (minutes, label) ->
                        FilterChip(
                            selected = filters.maxDeliveryTime == minutes,
                            onClick = {
                                onDeliveryTimeSelected(
                                    if (filters.maxDeliveryTime == minutes) null else minutes
                                )
                            },
                            label = { Text(label) },
                            leadingIcon = if (filters.maxDeliveryTime == minutes) {
                                {
                                    Icon(Icons.Default.Check, null)
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZomatoRed.copy(alpha = 0.12f),
                                selectedLabelColor = ZomatoRed,
                            ),
                        )
                    }
                }
            }

            HorizontalDivider()

            FilterSection(title = FILTER_SORT_BY) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        Dimens.SpaceS
                    )
                ) {
                    SortOption.values().forEach { sort ->
                        SortOptionRow(
                            label = sort.displayLabel,
                            isSelected = filters.sortBy == sort,
                            onClick = { onSortChange(sort) },
                        )
                    }
                }
            }

            HorizontalDivider()

            if (cuisines.isNotEmpty()) {
                FilterSection(title = FILTER_CUISINES) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            Dimens.SpaceS
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            Dimens.SpaceS
                        ),
                    ) {
                        cuisines.forEach { cuisine ->
                            FilterChip(
                                selected = filters.cuisineId == cuisine.id,
                                onClick = { onCuisineSelected(cuisine.id) },
                                label = { Text(cuisine.name) },
                                leadingIcon = if (filters.cuisineId == cuisine.id) {
                                    {
                                        Icon(Icons.Default.Check, null)
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZomatoRed.copy(
                                        alpha = ALPHA_FILTER_CHIP_SELECTED
                                    ),
                                    selectedLabelColor = ZomatoRed,
                                ),
                            )
                        }
                    }
                }
                HorizontalDivider()
            }

            Spacer(Modifier.height(Dimens.SpaceL))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL),
                horizontalArrangement = Arrangement.spacedBy(
                    Dimens.SpaceM
                ),
            ) {
                OutlinedButton(
                    onClick = { onClearAll(); onDismiss() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(CLEAR_ALL)
                }

                Button(
                    onClick = { onApply(); onDismiss() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZomatoRed,
                    ),
                ) {
                    Text(APPLY)
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.SpaceL,
                vertical = Dimens.SpaceM
            ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimens.SpaceM))
        content()
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = SELECTED,
                tint = ZomatoRed,
                modifier = Modifier.width(SortOptionCheckSize),
            )
        }
    }
}