package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.MenuCategory
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.FIXED_ITEMS_BEFORE_MENU
import kotlinx.coroutines.launch

@Composable
fun MenuTabRow(
    categories: List<MenuCategory>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        containerColor = Color.White,
        contentColor = ZomatoRed,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
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
                onClick = {
                    onTabSelected(index)
                    scope.launch {
                        var targetIndex = FIXED_ITEMS_BEFORE_MENU

                        for (i in 0 until index) {
                            targetIndex += 1 + categories[i].itemCount
                        }

                        listState.animateScrollToItem(
                            index = targetIndex,
                        )
                    }
                },
                text = {
                    Text(
                        text = category.name,
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