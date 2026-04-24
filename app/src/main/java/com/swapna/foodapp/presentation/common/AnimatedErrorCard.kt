package com.swapna.foodapp.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swapna.foodapp.presentation.ui.theme.Dimens

@Composable
fun AnimatedErrorCard(
    visible: Boolean,
    message: String,
    testTag: String = "",
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Spacer(Modifier.height(Dimens.SpaceS))
        ErrorCard(
            message = message,
            modifier = if (testTag.isNotEmpty())
                modifier.testTag(testTag)
            else modifier,
        )
    }
}