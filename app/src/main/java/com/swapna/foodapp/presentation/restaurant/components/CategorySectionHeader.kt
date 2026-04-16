package com.swapna.foodapp.presentation.restaurant.components

import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
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

// Why stickyHeader?
// As user scrolls menu, category name stays pinned at top
// User always knows which section they're in
// Matches Zomato/Swiggy UX exactly
@Composable
fun CategorySectionHeader(
    categoryName: String,
    itemCount:    Int,
    modifier:     Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // ✅ White background — hides content scrolling behind
            .background(Color.White),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical   = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Bookmark emoji — Zomato style section marker
            Text(
                text  = "🔖",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.width(Dimens.SpaceS))

            // Category name — bold
            Text(
                text       = categoryName,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.width(Dimens.SpaceXS))

            // Item count — gray
            Text(
                text  = "($itemCount)",
                style = MaterialTheme.typography.bodyMedium,
                color = AppGray,
            )
        }
    }
}