package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotServiceableSection(
    requestedArea: String,
    availableAreas: List<String>,
    onChangeLocation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(Modifier.height(Dimens.SpaceXXL))

        // ── Sad emoji illustration ────────────────────────────
        Text(
            text = "😔",
            fontSize = 64.sp,
        )

        Spacer(Modifier.height(Dimens.SpaceL))

        // ── Main message ──────────────────────────────────────
        Text(
            text = "We're not in $requestedArea yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        // ── Sub message ───────────────────────────────────────
        Text(
            text = "We are rapidly expanding!\n" +
                    "Try selecting a nearby area.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceXL))

        // ── Available areas section ───────────────────────────
        if (availableAreas.isNotEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
                shape = RoundedCornerShape(Dimens.RadiusM),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceL),
                ) {

                    // Section label
                    Text(
                        text = "We currently deliver in:",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppGray,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(Dimens.SpaceM))

                    // ── Area chips ────────────────────────────
                    // Shows: Koramangala, Indiranagar,
                    //        HSR Layout, Whitefield,
                    //        Electronic City
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            Dimens.SpaceS
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            Dimens.SpaceS
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        availableAreas.forEach { area ->
                            AreaChip(
                                areaName = area,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpaceXL))

        // ── Change location button ────────────────────────────
        Button(
            onClick = onChangeLocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ZomatoRed,
            ),
            shape = RoundedCornerShape(Dimens.RadiusM),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text = "Change Delivery Location",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(Dimens.Space32))
    }
}

// ── Area chip ─────────────────────────────────────────────────
@Composable
private fun AreaChip(areaName: String) {
    Box(
        modifier = Modifier
            .background(
                color = ZomatoRed.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(
                horizontal = Dimens.SpaceM,
                vertical = Dimens.SpaceXS,
            ),
    ) {
        Text(
            text = areaName,
            style = MaterialTheme.typography.bodySmall,
            color = ZomatoRed,
        )
    }
}

// ── Empty restaurants card ────────────────────────────────────
// Shows when filter returns empty but not NOT_SERVICEABLE
@Composable
private fun EmptyRestaurantsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceXXL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No restaurants found\nfor this area",
            style = MaterialTheme.typography.bodyLarge,
            color = AppGray,
            textAlign = TextAlign.Center,
        )
    }
}