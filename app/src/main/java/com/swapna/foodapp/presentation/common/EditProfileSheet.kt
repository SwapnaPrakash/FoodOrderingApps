package com.swapna.foodapp.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhiteSurface
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.EditProfileSheetElevation
import com.swapna.foodapp.presentation.ui.theme.Dimens.SaveLoadingIndicatorHeight
import com.swapna.foodapp.presentation.ui.theme.ErrorRed
import com.swapna.foodapp.presentation.ui.theme.FieldBorderUnfocused
import com.swapna.foodapp.presentation.ui.theme.SaveDisabledColor
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.EDIT_PROFILE
import com.swapna.foodapp.utils.AppConstants.EMAIL
import com.swapna.foodapp.utils.AppConstants.NAME
import com.swapna.foodapp.utils.AppConstants.SAVE_CHANGES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    name: String,
    email: String,
    nameError: String?,
    emailError: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppWhiteSurface,
        tonalElevation = EditProfileSheetElevation,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL)
                .navigationBarsPadding(),
        ) {

            Text(
                text = EDIT_PROFILE,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Dimens.SpaceXL))

            Text(
                text = NAME,
                style = MaterialTheme.typography.labelLarge,
                color = AppGray,
            )

            Spacer(Modifier.height(Dimens.SpaceXS))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                isError = nameError != null,
                singleLine = true,
                supportingText = nameError?.let {
                    {
                        Text(
                            text = it,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZomatoRed,
                    unfocusedBorderColor = FieldBorderUnfocused,
                    errorBorderColor = ErrorRed,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            Text(
                text = EMAIL,
                style = MaterialTheme.typography.labelLarge,
                color = AppGray,
            )

            Spacer(Modifier.height(Dimens.SpaceXS))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                isError = emailError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
                supportingText = emailError?.let {
                    {
                        Text(
                            text = it,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZomatoRed,
                    unfocusedBorderColor = FieldBorderUnfocused,
                    errorBorderColor = ErrorRed,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimens.SpaceXL))
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZomatoRed,
                    disabledContainerColor = SaveDisabledColor,
                ),
                shape = RoundedCornerShape(Dimens.RadiusM),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = AppWhiteSurface,
                        modifier = Modifier.height(SaveLoadingIndicatorHeight),
                    )
                } else {
                    Text(
                        text = SAVE_CHANGES,
                        color = AppWhiteSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpaceM))
        }
    }
}