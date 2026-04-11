package com.swapna.foodapp.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.*
import com.swapna.foodapp.utils.LoginTestTags

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel,
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    var phone by rememberSaveable { mutableStateOf("") }
    var otp   by rememberSaveable { mutableStateOf("") }

    // ✅ Navigate via callback — not navController directly
    LaunchedEffect(state) {
        if (state is AuthViewModel.AuthState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        modifier     = Modifier.testTag(LoginTestTags.SCREEN_ROOT),
        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = Color(0xFF323232),
                    contentColor   = AppWhite,
                    actionColor    = ZomatoRed,
                    shape          = RoundedCornerShape(Dimens.RadiusM),
                    modifier       = Modifier.padding(Dimens.SpaceM),
                )
            }
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppWhite)
                .padding(paddingValues)
                .padding(horizontal = Dimens.SpaceXXL)
                .verticalScroll(rememberScrollState()),
            verticalArrangement    = Arrangement.Center,
            horizontalAlignment    = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(Dimens.Space64))

            Text(
                text     = "🍔",
                fontSize = 64.sp,
                modifier = Modifier.testTag(LoginTestTags.LOGO),
            )

            Spacer(Modifier.height(Dimens.Space32))

            Text(
                text       = "Login",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.testTag(LoginTestTags.TITLE),
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            Text(
                text      = "Enter your mobile number to continue",
                style     = MaterialTheme.typography.bodyMedium,
                color     = AppGray,
                textAlign = TextAlign.Center,
                modifier  = Modifier.testTag(LoginTestTags.SUBTITLE),
            )

            Spacer(Modifier.height(Dimens.SpaceXXL))

            val isPhoneError = state is AuthViewModel.AuthState.Error
                    && otp.isEmpty()

            OutlinedTextField(
                value         = phone,
                onValueChange = { input ->
                    if (input.length <= 10 &&
                        input.all { it.isDigit() }) {
                        phone = input
                        if (state is AuthViewModel.AuthState.Error) {
                            viewModel.resetState()
                        }
                    }
                },
                label       = { Text("Phone Number") },
                prefix      = {
                    Text(
                        text  = "+91  ",
                        color = if (isPhoneError) ErrorRed else AppGray,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = if (isPhoneError) ErrorRed else AppGray,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                singleLine = true,
                isError    = isPhoneError,
                enabled    = state !is AuthViewModel.AuthState.OtpSent
                        && state !is AuthViewModel.AuthState.Loading,
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = ZomatoRed,
                    unfocusedBorderColor    = AppGray.copy(alpha = 0.5f),
                    errorBorderColor        = ErrorRed,
                    errorLabelColor         = ErrorRed,
                    errorLeadingIconColor   = ErrorRed,
                    disabledBorderColor     = AppGray.copy(alpha = 0.3f),
                    disabledLabelColor      = AppGray.copy(alpha = 0.5f),
                    disabledPrefixColor     = AppGray.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.PHONE_FIELD),
            )

            AnimatedVisibility(
                visible = state is AuthViewModel.AuthState.Error
                        && otp.isEmpty(),
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically(),
            ) {
                val errorMsg = (state as? AuthViewModel.AuthState.Error)
                    ?.message ?: ""
                Spacer(Modifier.height(Dimens.SpaceS))
                Surface(
                    color    = ErrorRedBg,
                    shape    = RoundedCornerShape(Dimens.RadiusS),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(LoginTestTags.PHONE_ERROR_CARD),
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            horizontal = Dimens.SpaceM,
                            vertical   = Dimens.SpaceS,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint               = ErrorRed,
                            modifier           = Modifier.size(Dimens.IconS),
                        )
                        Spacer(Modifier.width(Dimens.SpaceS))
                        Text(
                            text       = errorMsg,
                            style      = MaterialTheme.typography.bodySmall,
                            color      = ErrorRed,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state is AuthViewModel.AuthState.OtpSent
                        || (state is AuthViewModel.AuthState.Error
                        && otp.isNotEmpty())
                        || (state is AuthViewModel.AuthState.Loading
                        && otp.isNotEmpty()),
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(Dimens.SpaceL))

                    val isOtpError = state is AuthViewModel.AuthState.Error
                            && otp.isNotEmpty()

                    OutlinedTextField(
                        value         = otp,
                        onValueChange = { input ->
                            if (input.length <= 6 &&
                                input.all { it.isDigit() }) {
                                otp = input
                                if (state is AuthViewModel.AuthState.Error) {
                                    viewModel.resetState()
                                }
                            }
                        },
                        label           = { Text("Enter 6-digit OTP") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                        ),
                        singleLine = true,
                        isError    = isOtpError,
                        enabled    = state !is AuthViewModel.AuthState.Loading,
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = ZomatoRed,
                            unfocusedBorderColor = AppGray.copy(alpha = 0.5f),
                            errorBorderColor     = ErrorRed,
                            errorLabelColor      = ErrorRed,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(LoginTestTags.OTP_FIELD),
                    )

                    AnimatedVisibility(
                        visible = isOtpError,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically(),
                    ) {
                        val otpError = (state as? AuthViewModel.AuthState.Error)
                            ?.message ?: ""
                        Spacer(Modifier.height(Dimens.SpaceS))
                        Surface(
                            color    = ErrorRedBg,
                            shape    = RoundedCornerShape(Dimens.RadiusS),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(LoginTestTags.OTP_ERROR_CARD),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = Dimens.SpaceM,
                                    vertical   = Dimens.SpaceS,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint               = ErrorRed,
                                    modifier           = Modifier.size(Dimens.IconS),
                                )
                                Spacer(Modifier.width(Dimens.SpaceS))
                                Text(
                                    text       = otpError,
                                    style      = MaterialTheme.typography.bodySmall,
                                    color      = ErrorRed,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick  = {
                                otp = ""
                                viewModel.sendOtp(phone)
                            },
                            enabled  = state !is AuthViewModel.AuthState.Loading,
                            modifier = Modifier.testTag(LoginTestTags.RESEND_BUTTON),
                        ) {
                            Text(
                                text  = "Resend OTP",
                                color = ZomatoRed,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Surface(
                        color    = Color(0xFFE8FAF0),
                        shape    = RoundedCornerShape(Dimens.RadiusS),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(LoginTestTags.SUCCESS_CARD),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = Dimens.SpaceM,
                                vertical   = Dimens.SpaceS,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "✅", fontSize = 14.sp)
                            Spacer(Modifier.width(Dimens.SpaceS))
                            Text(
                                text       = "OTP sent to +91 $phone",
                                style      = MaterialTheme.typography.bodySmall,
                                color      = Color(0xFF1B8A3E),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpaceXXL))

            Button(
                onClick = {
                    when (state) {
                        is AuthViewModel.AuthState.OtpSent ->
                            viewModel.verifyOtp(otp)
                        else ->
                            viewModel.sendOtp(phone)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight)
                    .testTag(LoginTestTags.AUTH_BUTTON),
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = ZomatoRed,
                    disabledContainerColor = AppLightGray,
                ),
                shape   = RoundedCornerShape(Dimens.RadiusM),
                enabled = state !is AuthViewModel.AuthState.Loading,
            ) {
                if (state is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        color       = AppWhite,
                        modifier    = Modifier
                            .size(Dimens.IconM)
                            .testTag(LoginTestTags.LOADING_INDICATOR),
                        strokeWidth = Dimens.SpaceXS,
                    )
                } else {
                    Text(
                        text       = when (state) {
                            is AuthViewModel.AuthState.OtpSent -> "Verify OTP"
                            else                               -> "Send OTP"
                        },
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = AppWhite,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.Space64))
        }
    }
}