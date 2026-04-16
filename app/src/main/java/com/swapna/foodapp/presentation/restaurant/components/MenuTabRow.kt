package com.swapna.foodapp.presentation.restaurant.components

import androidx.compose.ui.unit.dp
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.MenuTabSelected
import com.swapna.foodapp.presentation.ui.theme.MenuTabUnselected
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.restaurant.RestaurantViewModel
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.rememberCoroutineScope
import com.swapna.foodapp.domain.model.MenuCategory
import kotlinx.coroutines.launch

@Composable
fun MenuTabRow(
    categories:    List<MenuCategory>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    listState:     LazyListState,
    modifier:      Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    // Fixed items before menu starts in LazyColumn
    // top_bar(1) + restaurant_info(1) + recommended(1) + tab_row(1) = 4
    val FIXED_ITEMS_BEFORE_MENU = 4

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier         = modifier
            .fillMaxWidth()
            .background(Color.White),
        containerColor   = Color.White,
        contentColor     = ZomatoRed,
        edgePadding      = 0.dp,
        indicator        = { tabPositions ->
            // ✅ Red animated underline indicator
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    tabPositions[selectedIndex.coerceIn(
                        0, tabPositions.size - 1
                    )]
                ),
                color = ZomatoRed,
            )
        },
        divider = { },
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = selectedIndex == index,
                onClick  = {
                    onTabSelected(index)

                    // ✅ Scroll LazyColumn to this category
                    // Calculate which index in LazyColumn
                    // this category header is at
                    scope.launch {
                        var targetIndex = FIXED_ITEMS_BEFORE_MENU

                        // Sum up items of all PREVIOUS categories
                        for (i in 0 until index) {
                            // +1 for category header
                            // + number of items
                            targetIndex += 1 + categories[i].itemCount
                        }

                        // Scroll to category header
                        listState.animateScrollToItem(
                            index = targetIndex,
                        )
                    }
                },
                text = {
                    Text(
                        text       = category.name,
                        fontWeight = if (selectedIndex == index) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (selectedIndex == index) {
                            ZomatoRed
                        } else {
                            AppGray
                        },
                    )
                },
            )
        }
    }
}