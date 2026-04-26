package com.swapna.foodapp.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens.QtyButtonIconSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.QtyButtonSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.QtyNumberPaddingH
import com.swapna.foodapp.presentation.ui.theme.Dimens.QtyNumberWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.QtySelectorBorderWidth
import com.swapna.foodapp.presentation.ui.theme.Dimens.QtySelectorRadius
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.utils.AppConstants.ADD_DESC
import com.swapna.foodapp.utils.AppConstants.QUANTITY_LABEL
import com.swapna.foodapp.utils.AppConstants.REMOVE_DESC

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = VegGreen,
) {
    Row(
        modifier = modifier
            .background(
                color = AppWhiteSurface,
                shape = RoundedCornerShape(QtySelectorRadius),
            )
            .border(
                width = QtySelectorBorderWidth,
                color = color,
                shape = RoundedCornerShape(QtySelectorRadius),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        Box(
            modifier = Modifier
                .size(QtyButtonSize)
                .clickable { onDecrement() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = REMOVE_DESC,
                tint = color,
                modifier = Modifier.size(QtyButtonIconSize),
            )
        }

        AnimatedContent(
            targetState = quantity,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                } else {
                    slideInVertically { -it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut()
                }
            },
            label = QUANTITY_LABEL,
        ) { qty ->
            Text(
                text = qty.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier
                    .width(QtyNumberWidth)
                    .padding(horizontal = QtyNumberPaddingH),
            )
        }

        Box(
            modifier = Modifier
                .size(QtyButtonSize)
                .clickable { onIncrement() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = ADD_DESC,
                tint = color,
                modifier = Modifier.size(QtyButtonIconSize),
            )
        }
    }
}