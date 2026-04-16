package com.swapna.foodapp.presentation.common

import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.domain.model.User

// WHY separate ProfileHeader?
// Profile screen header = avatar + name + email + phone
// Separate component = reusable in other screens
// Easy to change avatar loading logic independently

@Composable
fun ProfileHeader(
    user:     User,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(Dimens.SpaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // ── Avatar circle ─────────────────────────────────────
        // WHY Box + Icon instead of AsyncImage?
        // Profile image is optional in our User model
        // Fallback to initials/icon when no image
        Box(
            modifier         = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    ZomatoRed.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (user.profileImage.isNotEmpty()) {
                // Future: load actual profile image
                // coil.compose.AsyncImage(model = user.profileImage)
                Icon(
                    imageVector        = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(40.dp),
                )
            } else {
                // Fallback — person icon
                Icon(
                    imageVector        = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint               = ZomatoRed,
                    modifier           = Modifier.size(40.dp),
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceM))

        // ── User name ─────────────────────────────────────────
        Text(
            text       = user.name,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Dimens.SpaceXS))

        // ── Email ─────────────────────────────────────────────
        if (user.email.isNotEmpty()) {
            Text(
                text  = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = AppGray,
            )
            Spacer(Modifier.height(Dimens.SpaceXS))
        }

        // ── Phone ─────────────────────────────────────────────
        Text(
            text  = "+91 ${user.phone}",
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
        )
    }
}