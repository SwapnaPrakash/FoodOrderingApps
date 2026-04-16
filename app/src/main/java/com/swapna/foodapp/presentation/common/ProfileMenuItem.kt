package com.swapna.foodapp.presentation.common

import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// WHY separate ProfileMenuItem?
// Profile screen has 4+ menu rows
// Each row = same layout: icon + label + chevron
// Separate component = no code duplication
// Easy to add more items later

@Composable
fun ProfileMenuItem(
    icon:     ImageVector,
    label:    String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    // Optional: show badge count (orders count etc)
    badge:    Int?     = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Full row is tappable — better touch target
            .clickable { onClick() }
            .padding(
                horizontal = Dimens.SpaceL,
                vertical   = Dimens.SpaceM,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Left icon ─────────────────────────────────────────
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = AppGray,
            modifier           = Modifier.size(24.dp),
        )

        Spacer(Modifier.width(Dimens.SpaceM))

        // ── Label ─────────────────────────────────────────────
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(1f),
        )

        // ── Optional badge ────────────────────────────────────
        if (badge != null && badge > 0) {
            Text(
                text  = badge.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = AppGray,
                modifier = Modifier.padding(
                    end = Dimens.SpaceS
                ),
            )
        }

        // ── Chevron arrow ─────────────────────────────────────
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = AppGray,
            modifier           = Modifier.size(20.dp),
        )
    }

    HorizontalDivider(
        color    = Color(0xFFF5F5F5),
        modifier = Modifier.padding(
            start = Dimens.SpaceL + 24.dp + Dimens.SpaceM
        ),
    )
}