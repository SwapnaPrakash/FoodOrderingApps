package com.swapna.foodapp.presentation.search.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.swapna.foodapp.presentation.ui.theme.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester     = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus when screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint               = AppGray,
                )
            }
        },
        title = {
            OutlinedTextField(
                value         = query,
                onValueChange = onQueryChange,
                placeholder   = {
                    Text(
                        text  = "Search restaurants, cuisines...",
                        color = AppGray,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = null,
                        tint               = AppGray,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector        = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint               = AppGray,
                            )
                        }
                    }
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboardController?.hide() }
                ),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = AppDivider,    // ← was ZomatoRed
                    unfocusedBorderColor    = AppDivider,
                    focusedContainerColor   = AppLightGray,
                    unfocusedContainerColor = AppLightGray,
                    cursorColor             = AppGray,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = Dimens.SpaceS)
                    .focusRequester(focusRequester),
            )
        },
    )
}