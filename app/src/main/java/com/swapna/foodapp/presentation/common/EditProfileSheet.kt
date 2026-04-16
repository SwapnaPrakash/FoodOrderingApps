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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ErrorRed
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

// WHY ModalBottomSheet for edit profile?
// Stays in context of profile screen
// User doesn't navigate away
// Easy to cancel by swiping down
// Standard Material3 pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    name:        String,
    email:       String,
    nameError:   String?,
    emailError:  String?,
    isSaving:    Boolean,
    onNameChange:  (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave:        () -> Unit,
    onDismiss:     () -> Unit,
) {
    // skipPartiallyExpanded = true
    // Sheet goes straight to full height
    // No awkward half-open state
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        // Pure white — no gray tint
        containerColor   = Color.White,
        tonalElevation   = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL)
                // navigationBarsPadding = respect system nav bar
                // Without this → Save button hidden behind nav
                .navigationBarsPadding(),
        ) {

            // ── Sheet title ───────────────────────────────────
            Text(
                text       = "Edit Profile",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Dimens.SpaceXL))

            // ── Name field ────────────────────────────────────
            Text(
                text  = "Name",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
            )

            Spacer(Modifier.height(Dimens.SpaceXS))

            OutlinedTextField(
                value         = name,
                onValueChange = onNameChange,
                // isError = true → red border + error text
                isError       = nameError != null,
                singleLine    = true,
                // supportingText = shown below field
                // Shows validation error
                supportingText = nameError?.let {
                    {
                        Text(
                            text  = it,
                            color = ErrorRed,
                            style = MaterialTheme.typography
                                .bodySmall,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = ZomatoRed,
                    unfocusedBorderColor = Color.LightGray,
                    errorBorderColor     = ErrorRed,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            // ── Email field ───────────────────────────────────
            Text(
                text  = "Email",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
            )

            Spacer(Modifier.height(Dimens.SpaceXS))

            OutlinedTextField(
                value         = email,
                onValueChange = onEmailChange,
                isError       = emailError != null,
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    // Email keyboard = shows @ and . easily
                    keyboardType = KeyboardType.Email,
                ),
                supportingText = emailError?.let {
                    {
                        Text(
                            text  = it,
                            color = ErrorRed,
                            style = MaterialTheme.typography
                                .bodySmall,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = ZomatoRed,
                    unfocusedBorderColor = Color.LightGray,
                    errorBorderColor     = ErrorRed,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimens.SpaceXL))

            // ── Save Button ───────────────────────────────────
            Button(
                onClick  = onSave,
                // Disabled while saving — prevent double-tap
                enabled  = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = ZomatoRed,
                    disabledContainerColor = Color.LightGray,
                ),
                shape    = RoundedCornerShape(Dimens.RadiusM),
            ) {
                if (isSaving) {
                    // Show spinner while saving
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.height(20.dp),
                    )
                } else {
                    Text(
                        text       = "Save Changes",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography
                            .titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpaceM))
        }
    }
}