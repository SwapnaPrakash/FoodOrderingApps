package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.SpaceXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Error icon
        Icon(
            imageVector        = Icons.Default.WifiOff,
            contentDescription = "Error",
            tint               = ZomatoRed,
            modifier           = Modifier.size(Dimens.IconXL),
        )

        Spacer(Modifier.height(Dimens.SpaceL))

        Text(
            text      = "Something went wrong",
            style     = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.semantics {
                contentDescription = "Error: $message"
            },
        )

        Spacer(Modifier.height(Dimens.SpaceXXL))

        Button(
            onClick  = onRetry,
            colors   = ButtonDefaults.buttonColors(
                containerColor = ZomatoRed,
            ),
            modifier = Modifier.semantics {
                contentDescription = "Retry button"
            },
        ) {
            Text("Try Again")
        }
    }
}