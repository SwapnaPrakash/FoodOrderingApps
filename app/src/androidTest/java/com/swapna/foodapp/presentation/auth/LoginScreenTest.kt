package com.swapna.foodapp.presentation.auth

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.fakes.FakeUserRepository
import com.swapna.foodapp.utils.AndroidTestConstants
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_LOGIN_NOT_CALLED
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_BTN_SEND_OTP
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_BTN_VERIFY_OTP
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_BURGER_EMOJI
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_GENERIC_ERROR
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_NETWORK_UNAVAILABLE
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_OTP_EXTRA_DIGITS
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_OTP_SINGLE_CHAR
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_PHONE_ERROR_MSG
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_SHORT_OTP
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_SHORT_PHONE
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_SUBTITLE
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_TITLE
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_USER_EMAIL
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_USER_ID
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_USER_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_USER_PHONE
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_VALID_OTP
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_VALID_PHONE
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_VALID_PHONE_10
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_WRONG_OTP
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_WRONG_OTP_FULL_MSG
import com.swapna.foodapp.utils.AndroidTestConstants.LOGIN_WRONG_OTP_MSG
import com.swapna.foodapp.utils.LoginTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val screenOnRule = object : org.junit.rules.ExternalResource() {
        override fun before() {
            val uiAutomation = androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation

            uiAutomation.executeShellCommand(AndroidTestConstants.CMD_WAKEUP)
            uiAutomation.executeShellCommand(AndroidTestConstants.CMD_DISMISS_KEYGUARD)
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeRepo: FakeUserRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeUserRepository()
        viewModel = AuthViewModel(fakeRepo)

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            )
        }
    }

    private fun setContent(onLoginSuccess: () -> Unit = {}) {
        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    viewModel = viewModel,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun hideKeyboard() {
        composeTestRule.runOnUiThread {
            val imm = composeTestRule.activity.getSystemService(
                android.content.Context.INPUT_METHOD_SERVICE
            ) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(
                composeTestRule.activity.currentFocus?.windowToken
                    ?: composeTestRule.activity.window.decorView.windowToken,
                android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS,
            )
        }
        Thread.sleep(300)
        composeTestRule.waitForIdle()
    }

    private fun sendOtp(phone: String = LOGIN_VALID_PHONE) {
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput(phone)
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun verifyOtp(otp: String) {
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput(otp)
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    // GROUP 1 — Initial UI State
    @Test
    fun loginScreen_burger_emoji_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGIN_BURGER_EMOJI)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_title_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGIN_TITLE)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_subtitle_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGIN_SUBTITLE)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_phoneField_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_countryCode_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_sendOtp_button_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGIN_BTN_SEND_OTP)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_sendOtp_button_isEnabled_initially() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_otpField_doesNotExist_initially() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_errorCard_doesNotExist_initially() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_resendButton_doesNotExist_initially() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_screenRoot_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.SCREEN_ROOT)
            .assertIsDisplayed()
    }

    // GROUP 2 — Phone Validation
    @Test
    fun loginScreen_emptyPhone_showsError_onSendOtp() {
        setContent()
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(LOGIN_PHONE_ERROR_MSG)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyPhone_showsErrorCard() {
        setContent()
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_shortPhone_showsError() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput(LOGIN_SHORT_PHONE)
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(LOGIN_PHONE_ERROR_MSG)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_phoneField_capped_at_10_digits() {
        setContent()
        LOGIN_VALID_PHONE_10.forEach { digit ->
            composeTestRule
                .onNodeWithTag(LoginTestTags.PHONE_FIELD)
                .performTextInput(digit.toString())
        }
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput(LOGIN_OTP_SINGLE_CHAR)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(LOGIN_VALID_PHONE_10)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_typing_validPhone_clearsErrorCard() {
        setContent()
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput(LOGIN_VALID_PHONE)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertDoesNotExist()
    }

    // GROUP 3 — Successful OTP Send
    @Test
    fun loginScreen_validPhone_sendOtp_showsOtpField() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_afterOtpSent_buttonText_changesTo_verifyOtp() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithText(LOGIN_BTN_VERIFY_OTP)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(LOGIN_BTN_SEND_OTP)
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_afterOtpSent_phoneField_isDisabled() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_afterOtpSent_successCard_isDisplayed() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_afterOtpSent_successCard_showsPhoneNumber() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_afterOtpSent_resendButton_isDisplayed() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .assertIsDisplayed()
    }

    // GROUP 4 — OTP Validation
    @Test
    fun loginScreen_shortOtp_showsOtpErrorCard() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput(LOGIN_SHORT_OTP)
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_otpField_capped_at_6_digits() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()

        LOGIN_VALID_OTP.forEach { digit ->
            composeTestRule
                .onNodeWithTag(LoginTestTags.OTP_FIELD)
                .performTextInput(digit.toString())
        }
        LOGIN_OTP_EXTRA_DIGITS.forEach { digit ->
            composeTestRule
                .onNodeWithTag(LoginTestTags.OTP_FIELD)
                .performTextInput(digit.toString())
        }
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(LOGIN_VALID_OTP)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_typingOtp_afterError_clearsOtpErrorCard() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        fakeRepo.verifyOtpResult = Result.failure(Exception(LOGIN_WRONG_OTP_MSG))
        setContent()
        sendOtp()

        // Step 2: submit wrong OTP
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput(LOGIN_WRONG_OTP)
        hideKeyboard()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertIsDisplayed()

        // Step 3: replace OTP → triggers onChange → resetState() → clears error
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextReplacement(LOGIN_OTP_SINGLE_CHAR)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertDoesNotExist()
    }

    // GROUP 5 — Successful Login
    @Test
    fun loginScreen_correctOtp_calls_onLoginSuccess() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        fakeRepo.verifyOtpResult = Result.success(
            User(
                id = LOGIN_USER_ID,
                name = LOGIN_USER_NAME,
                email = LOGIN_USER_EMAIL,
                phone = LOGIN_USER_PHONE,
            )
        )

        var loginSuccessCalled = false
        setContent(onLoginSuccess = { loginSuccessCalled = true })

        sendOtp()
        verifyOtp(LOGIN_VALID_OTP)

        assert(loginSuccessCalled) { ASSERT_LOGIN_NOT_CALLED }
    }

    // GROUP 6 — Error States
    @Test
    fun loginScreen_sendOtp_networkError_showsErrorCard() {
        fakeRepo.sendOtpResult = Result.failure(Exception(LOGIN_NETWORK_UNAVAILABLE))
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(LOGIN_NETWORK_UNAVAILABLE)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_sendOtp_error_button_remainsEnabled() {
        fakeRepo.sendOtpResult = Result.failure(Exception(LOGIN_GENERIC_ERROR))
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_wrongOtp_showsCorrectErrorMessage() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        fakeRepo.verifyOtpResult = Result.failure(Exception(LOGIN_WRONG_OTP_FULL_MSG))
        setContent()
        sendOtp()
        verifyOtp(LOGIN_WRONG_OTP)
        composeTestRule
            .onNodeWithText(LOGIN_WRONG_OTP_FULL_MSG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertIsDisplayed()
    }

    // GROUP 7 — Resend OTP
    @Test
    fun loginScreen_resendOtp_clearsOtpField() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput(LOGIN_SHORT_OTP)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(LOGIN_SHORT_OTP)
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_resendOtp_keepsSamePhoneNumber() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_resendOtp_successCard_stillDisplayed() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()
        sendOtp()
        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }
}