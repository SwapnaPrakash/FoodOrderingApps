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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swapna.foodapp.presentation.common.AnimatedErrorCard
import com.swapna.foodapp.presentation.common.OtpSuccessCard
import com.swapna.foodapp.presentation.common.PrimaryButton
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.AppWhite
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.Dimens.LogoEmojiSize
import com.swapna.foodapp.presentation.ui.theme.ErrorRed
import com.swapna.foodapp.presentation.ui.theme.SnackbarContainerColor
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_BORDER_DISABLED
import com.swapna.foodapp.utils.AppConstants.ALPHA_BORDER_UNFOCUSED
import com.swapna.foodapp.utils.AppConstants.ALPHA_LABEL_DISABLED
import com.swapna.foodapp.utils.AppConstants.ALPHA_PREFIX_DISABLED
import com.swapna.foodapp.utils.AppConstants.EMOJI_BURGER
import com.swapna.foodapp.utils.AppConstants.ENTER_MOB_NUM
import com.swapna.foodapp.utils.AppConstants.LOGIN
import com.swapna.foodapp.utils.AppConstants.PHONE
import com.swapna.foodapp.utils.AppConstants.PHONE_DIG
import com.swapna.foodapp.utils.AppConstants.PHONE_NUMBER
import com.swapna.foodapp.utils.AppConstants.PHONE_OTP
import com.swapna.foodapp.utils.AppConstants.RESEND_OTP
import com.swapna.foodapp.utils.AppConstants.SEND_OTP
import com.swapna.foodapp.utils.AppConstants.VERIFY_OTP
import com.swapna.foodapp.utils.LoginTestTags

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    var phone by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is AuthViewModel.AuthState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.testTag(LoginTestTags.SCREEN_ROOT),
        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SnackbarContainerColor,
                    contentColor = AppWhite,
                    actionColor = ZomatoRed,
                    shape = RoundedCornerShape(Dimens.RadiusM),
                    modifier = Modifier.padding(Dimens.SpaceM),
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(Dimens.Space64))

            Text(
                text = EMOJI_BURGER,
                fontSize = LogoEmojiSize,
                modifier = Modifier.testTag(LoginTestTags.LOGO),
            )

            Spacer(Modifier.height(Dimens.Space32))

            Text(
                text = LOGIN,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(LoginTestTags.TITLE),
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            Text(
                text = ENTER_MOB_NUM,
                style = MaterialTheme.typography.bodyMedium,
                color = AppGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(LoginTestTags.SUBTITLE),
            )

            Spacer(Modifier.height(Dimens.SpaceXXL))

            val isPhoneError = state is AuthViewModel.AuthState.Error
                    && otp.isEmpty()

            val isOtpVisible = state is AuthViewModel.AuthState.OtpSent
                    || (state is AuthViewModel.AuthState.Error && otp.isNotEmpty())
                    || (state is AuthViewModel.AuthState.Loading && otp.isNotEmpty())

            val isOtpError = state is AuthViewModel.AuthState.Error
                    && otp.isNotEmpty()

            val phoneErrorMsg =
                (state as? AuthViewModel.AuthState.Error)?.message ?: ""

            val otpErrorMsg =
                (state as? AuthViewModel.AuthState.Error)?.message ?: ""

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    if (input.length <= 10 && input.all { it.isDigit() }) {
                        phone = input
                        if (state is AuthViewModel.AuthState.Error) {
                            viewModel.resetState()
                        }
                    }
                },
                label = { Text(PHONE_NUMBER) },
                prefix = {
                    Text(
                        text = PHONE_DIG,
                        color = if (isPhoneError) ErrorRed else AppGray,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = PHONE,
                        tint = if (isPhoneError) ErrorRed else AppGray,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                singleLine = true,
                isError = isPhoneError,
                enabled = state !is AuthViewModel.AuthState.OtpSent
                        && state !is AuthViewModel.AuthState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZomatoRed,
                    unfocusedBorderColor = AppGray.copy(alpha = ALPHA_BORDER_UNFOCUSED),
                    errorBorderColor = ErrorRed,
                    errorLabelColor = ErrorRed,
                    errorLeadingIconColor = ErrorRed,
                    disabledBorderColor = AppGray.copy(alpha = ALPHA_BORDER_DISABLED),
                    disabledLabelColor = AppGray.copy(alpha = ALPHA_LABEL_DISABLED),
                    disabledPrefixColor = AppGray.copy(alpha = ALPHA_PREFIX_DISABLED),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.PHONE_FIELD),
            )

            AnimatedErrorCard(
                visible = isPhoneError,
                message = phoneErrorMsg,
                testTag = LoginTestTags.PHONE_ERROR_CARD,
            )

            AnimatedVisibility(
                visible = isOtpVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(Dimens.SpaceL))

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                otp = input
                                if (state is AuthViewModel.AuthState.Error) {
                                    viewModel.resetState()
                                }
                            }
                        },
                        label = { Text(PHONE_OTP) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                        ),
                        singleLine = true,
                        isError = isOtpError,
                        enabled = state !is AuthViewModel.AuthState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZomatoRed,
                            unfocusedBorderColor = AppGray.copy(alpha = ALPHA_BORDER_UNFOCUSED),
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(LoginTestTags.OTP_FIELD),
                    )

                    AnimatedErrorCard(
                        visible = isOtpError,
                        message = otpErrorMsg,
                        testTag = LoginTestTags.OTP_ERROR_CARD,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                otp = ""
                                viewModel.sendOtp(phone)
                            },
                            enabled = state !is AuthViewModel.AuthState.Loading,
                            modifier = Modifier.testTag(LoginTestTags.RESEND_BUTTON),
                        ) {
                            Text(
                                text = RESEND_OTP,
                                color = ZomatoRed,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    OtpSuccessCard(
                        phone = phone,
                        modifier = Modifier.testTag(LoginTestTags.SUCCESS_CARD),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpaceXXL))

            PrimaryButton(
                text = when (state) {
                    is AuthViewModel.AuthState.OtpSent -> VERIFY_OTP
                    else -> SEND_OTP
                },
                onClick = {
                    when (state) {
                        is AuthViewModel.AuthState.OtpSent -> viewModel.verifyOtp(otp)
                        else -> viewModel.sendOtp(phone)
                    }
                },
                isLoading = state is AuthViewModel.AuthState.Loading,
                enabled = state !is AuthViewModel.AuthState.Loading,
                testTag = LoginTestTags.AUTH_BUTTON,
            )

            Spacer(Modifier.height(Dimens.Space64))
        }
    }
}