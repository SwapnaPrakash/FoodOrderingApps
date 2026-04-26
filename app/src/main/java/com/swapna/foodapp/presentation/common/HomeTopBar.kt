package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.swapna.foodapp.presentation.ui.theme.AppDivider
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.TopBarBadgeTextSize
import com.swapna.foodapp.presentation.ui.theme.Dimens.TopBarDeliveryTextSize
import com.swapna.foodapp.presentation.ui.theme.SearchFieldBg
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.CART
import com.swapna.foodapp.utils.AppConstants.CHANGE_LOCATION
import com.swapna.foodapp.utils.AppConstants.DELIVERING_TO
import com.swapna.foodapp.utils.AppConstants.GO_TO_CART
import com.swapna.foodapp.utils.AppConstants.LOCATION_ICON_DESC
import com.swapna.foodapp.utils.AppConstants.SEARCH_HINT
import com.swapna.foodapp.utils.AppConstants.SEARCH_ICON_DESC

@Composable
fun HomeTopBar(
    location: String,
    cartItemCount: Int,
    onLocationClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppWhite)
            .padding(
                horizontal = Dimens.SpaceL,
                vertical = Dimens.SpaceM,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocationClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = LOCATION_ICON_DESC,
                    tint = ZomatoRed,
                    modifier = Modifier.size(Dimens.IconM),
                )
                Spacer(Modifier.width(Dimens.SpaceXS))
                Column {
                    Text(
                        text = DELIVERING_TO,
                        fontSize = TopBarDeliveryTextSize,
                        fontWeight = FontWeight.Medium,
                        color = AppGray,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = CHANGE_LOCATION,
                            modifier = Modifier.size(Dimens.IconM),
                        )
                    }
                }
            }
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge(containerColor = ZomatoRed) {
                            Text(
                                text = cartItemCount.toString(),
                                fontSize = TopBarBadgeTextSize,
                            )
                        }
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = CART
                },
            ) {
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = GO_TO_CART,
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpaceM))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSearchClick() }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = {
                    Text(
                        text = SEARCH_HINT,
                        color = AppGray,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = SEARCH_ICON_DESC,
                        tint = AppGray,
                    )
                },
                readOnly = true,
                singleLine = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = AppDivider,
                    disabledContainerColor = SearchFieldBg,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        HorizontalDivider(color = AppDivider)
    }
}