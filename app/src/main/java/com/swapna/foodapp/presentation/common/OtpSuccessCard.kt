package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.CheckEmojiSize
import com.swapna.foodapp.presentation.ui.theme.OtpSuccessBg
import com.swapna.foodapp.presentation.ui.theme.OtpSuccessText
import com.swapna.foodapp.utils.AppConstants.EMOJI_SUCCESS_TICK
import com.swapna.foodapp.utils.AppConstants.SENT_OTP

@Composable
fun OtpSuccessCard(
    phone: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = OtpSuccessBg,
        shape = RoundedCornerShape(Dimens.RadiusS),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.SpaceM,
                vertical = Dimens.SpaceS,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = EMOJI_SUCCESS_TICK,
                fontSize = CheckEmojiSize,
            )
            Spacer(Modifier.width(Dimens.SpaceS))
            Text(
                text = "$SENT_OTP $phone",
                style = MaterialTheme.typography.bodySmall,
                color = OtpSuccessText,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}