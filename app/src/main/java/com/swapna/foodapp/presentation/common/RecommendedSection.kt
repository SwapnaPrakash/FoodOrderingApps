package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.presentation.ui.theme.AddButtonBorder
import com.swapna.foodapp.presentation.ui.theme.AddButtonText
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.RecommendedImageHeight
import com.swapna.foodapp.presentation.ui.theme.RecommendedCardBg
import com.swapna.foodapp.utils.AppConstants.ADD_BTN_LABEL
import com.swapna.foodapp.utils.AppConstants.ADD_TO_CART_DESC
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.EMOJI_STAR
import com.swapna.foodapp.utils.AppConstants.ITEMS_SUFFIX
import com.swapna.foodapp.utils.AppConstants.RECOMMENDED

@Composable
fun RecommendedSection(
    items: List<MenuItem>,
    onItemTap: (String) -> Unit,
    onAddTap: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpaceL,
                    vertical = Dimens.SpaceM,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = RECOMMENDED,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(Dimens.SpaceXS))
            Text(
                text = EMOJI_STAR,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${items.size}$ITEMS_SUFFIX",
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            items(
                items = items,
                key = { it.id },
            ) { menuItem ->
                RecommendedItemCard(
                    item = menuItem,
                    onTap = { onItemTap(menuItem.id) },
                    onAddTap = { onAddTap(menuItem) },
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceM))
    }
}

@Composable
private fun RecommendedItemCard(
    item: MenuItem,
    onTap: () -> Unit,
    onAddTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(Dimens.RecommendedCardWidth)
            .clickable { onTap() },
        color = RecommendedCardBg,
        shape = RoundedCornerShape(Dimens.RadiusM),
        shadowElevation = Dimens.ElevationS,
        border = BorderStroke(
            width = Dimens.BorderThin,
            color = Color(0xFFEEEEEE),
        ),
    ) {
        Column {
            Box {
                AsyncImage(
                    model = item.imageUrl.ifEmpty {
                        "https://picsum.photos/seed/${item.id}/160/120"
                    },
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RecommendedImageHeight)
                        .clip(
                            RoundedCornerShape(
                                topStart = Dimens.RadiusM,
                                topEnd = Dimens.RadiusM,
                                bottomStart = Dimens.Zero,
                                bottomEnd = Dimens.Zero,
                            )
                        ),
                )
                VegBadge(
                    isVeg = item.isVeg,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Dimens.SpaceXS),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.SpaceS,
                        vertical = Dimens.SpaceXS,
                    ),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(Dimens.SpaceXXS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$CURRENCY_SYMBOL${item.price.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AddButton(onTap = onAddTap)
                }
            }
        }
    }
}

@Composable
fun AddButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onTap,
        modifier = modifier.height(Dimens.SmallButtonHeight),
        shape = RoundedCornerShape(Dimens.RadiusS),
        contentPadding = PaddingValues(
            horizontal = Dimens.SpaceM,
            vertical = Dimens.SpaceXS,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = AddButtonText,
        ),
        border = BorderStroke(
            width = Dimens.BorderNormal,
            color = AddButtonBorder,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = ADD_TO_CART_DESC,
            tint = AddButtonText,
            modifier = Modifier.size(Dimens.IconS),
        )
        Spacer(Modifier.width(Dimens.SpaceXXS))
        Text(
            text = ADD_BTN_LABEL,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AddButtonText,
        )
    }
}