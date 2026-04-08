package com.swapna.foodapp.presentation.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppLightGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ErrorRed
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    // Collect state as Compose State
    // collectAsStateWithLifecycle is lifecycle-aware
    // (stops collecting when screen is in background)
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Local UI state — only the text field values
    // These don't need to be in ViewModel
    var phone by remember { mutableStateOf("") }
    var otp   by remember { mutableStateOf("") }

    // Navigate to Home when login succeeds
    // LaunchedEffect re-runs when `state` changes
    LaunchedEffect(state) {
        if (state is AuthViewModel.AuthState.Success) {
            navController.navigate(AppRoutes.HOME) {
                // Remove Login from back stack
                // User can't go back to Login by pressing back
                popUpTo(AppRoutes.LOGIN) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
            .padding(horizontal = Dimens.SpaceXXL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // ── Logo + Title ──────────────────────────────────────
        Text(
            text     = "🍔",
            fontSize = 64.sp,
        )

        Spacer(Modifier.height(Dimens.Space32))

        Text(
            text       = "Login",
            style      = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color      = Color.Black,
        )

        Spacer(Modifier.height(Dimens.SpaceS))

        Text(
            text  = "Enter your mobile number to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = AppGray,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpaceXXL))

        // ── Phone Number Field ────────────────────────────────
        OutlinedTextField(
            value = phone,
            shape = MaterialTheme.shapes.small,
            onValueChange = { input ->
                // Only allow digits, max 10 characters
                if (input.length <= 10 && input.all { it.isDigit() }) {
                    phone = input
                    // Clear error when user starts typing
                    if (state is AuthViewModel.AuthState.Error) {
                        viewModel.resetState()
                    }
                }
            },
            label  = { Text("Phone Number") },
            prefix = { Text("+91  ") },
            placeholder = { Text("9876543210") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
            ),
            singleLine = true,
            isError    = state is AuthViewModel.AuthState.Error,
            // Disable once OTP is sent so phone can't be changed
            enabled    = state !is AuthViewModel.AuthState.OtpSent
                    && state !is AuthViewModel.AuthState.Loading,
            modifier   = Modifier.fillMaxWidth(),
        )

        // ── OTP Field (visible only after OTP sent) ────────────
        AnimatedVisibility(
            visible = state is AuthViewModel.AuthState.OtpSent
                    || state is AuthViewModel.AuthState.Loading
                    && otp.isNotEmpty(),
            enter   = fadeIn() + slideInVertically(),
            exit    = fadeOut(),
        ) {
            Column {
                Spacer(Modifier.height(Dimens.SpaceL))

                OutlinedTextField(
                    value = otp,
                    onValueChange = { input ->
                        // Only allow digits, max 6 characters
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            otp = input
                            if (state is AuthViewModel.AuthState.Error) {
                                viewModel.resetState()
                            }
                        }
                    },
                    label = { Text("Enter 6-digit OTP") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
                    singleLine = true,
                    isError    = state is AuthViewModel.AuthState.Error,
                    enabled    = state !is AuthViewModel.AuthState.Loading,
                    modifier   = Modifier.fillMaxWidth(),
                )

                // Resend OTP link
                Spacer(Modifier.height(Dimens.SpaceS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { viewModel.sendOtp(phone) },
                        enabled = state !is AuthViewModel.AuthState.Loading,
                    ) {
                        Text(
                            text  = "Resend OTP",
                            color = ZomatoRed,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        // ── Error Message ─────────────────────────────────────
        AnimatedVisibility(
            visible = state is AuthViewModel.AuthState.Error,
        ) {
            Column {
                Spacer(Modifier.height(Dimens.SpaceS))
                Text(
                    text  = (state as? AuthViewModel.AuthState.Error)?.message ?: "",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceXXL))

        // ── CTA Button ────────────────────────────────────────
        Button(
            onClick = {
                when (state) {
                    // OTP sent → verify it
                    is AuthViewModel.AuthState.OtpSent -> {
                        viewModel.verifyOtp(otp)
                    }
                    // Default → send OTP
                    else -> {
                        viewModel.sendOtp(phone)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape    = MaterialTheme.shapes.small,
            colors  = ButtonDefaults.buttonColors(
                containerColor = ZomatoRed,
                disabledContainerColor = AppLightGray,
            ),
            // Disable while loading
            enabled = state !is AuthViewModel.AuthState.Loading,
        ) {
            if (state is AuthViewModel.AuthState.Loading) {
                // Show spinner while loading
                CircularProgressIndicator(
                    color    = AppWhite,
                    modifier = Modifier.size(Dimens.IconM),
                    strokeWidth = Dimens.SpaceXS,
                )
            } else {
                Text(
                    text  = when (state) {
                        is AuthViewModel.AuthState.OtpSent -> "Verify OTP"
                        else -> "Send OTP"
                    },
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = AppWhite,
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpaceL))

        // ── Phone hint shown after OTP sent ───────────────────
        AnimatedVisibility(
            visible = state is AuthViewModel.AuthState.OtpSent,
        ) {
            Text(
                text  = "OTP sent to +91 $phone",
                style = MaterialTheme.typography.bodySmall,
                color = AppGray,
            )
        }
    }
}