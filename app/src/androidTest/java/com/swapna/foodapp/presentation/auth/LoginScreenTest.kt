package com.swapna.foodapp.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.fakes.FakeUserRepository
import com.swapna.foodapp.utils.LoginTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// WHY @RunWith(AndroidJUnit4::class)?
// Without this, JUnit doesn't know this is an instrumented test
// createComposeRule() needs AndroidJUnit4 to find the test Activity
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val screenOnRule = object : org.junit.rules.ExternalResource() {
        override fun before() {
            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("input keyevent KEYCODE_WAKEUP")

            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("wm dismiss-keyguard")
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeRepo: FakeUserRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        fakeRepo  = FakeUserRepository()
        viewModel = AuthViewModel(fakeRepo)
    }

    // WHY MaterialTheme wrapper?
    // LoginScreen uses Material3 components (Scaffold, OutlinedTextField)
    // Without MaterialTheme → components crash silently
    // → setContent fails → "No compose hierarchies found"
    // WHY collectAsStateWithLifecycle works inside createComposeRule?
    // createComposeRule() spins up ComponentActivity via ui-test-manifest
    // ComponentActivity provides LifecycleOwner automatically
    private fun setContent(onLoginSuccess: () -> Unit = {}) {
        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    viewModel      = viewModel,
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial UI State
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_burger_emoji_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("🍔").assertIsDisplayed()
    }

    @Test
    fun loginScreen_title_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_subtitle_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText("Enter your mobile number to continue")
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
        // Phone field exists and is displayed — verifies the field
        // with its prefix is rendered correctly
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_sendOtp_button_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Send OTP").assertIsDisplayed()
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

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Phone Validation
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_emptyPhone_showsError_onSendOtp() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Enter a valid 10-digit phone number")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyPhone_showsErrorCard() {
        setContent()
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
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
            .performTextInput("12345")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Enter a valid 10-digit phone number")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_phoneField_capped_at_10_digits() {
        setContent()

        // Type 10 valid digits one by one
        "1234567890".forEach { digit ->
            composeTestRule
                .onNodeWithTag(LoginTestTags.PHONE_FIELD)
                .performTextInput(digit.toString())
        }
        // Try to type one more — should be rejected
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("1")
        composeTestRule.waitForIdle()

        // Field still shows exactly 10 digits
        composeTestRule
            .onNodeWithText("1234567890")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_typing_validPhone_clearsErrorCard() {
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertDoesNotExist()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Successful OTP Send
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_validPhone_sendOtp_showsOtpField() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_afterOtpSent_buttonText_changesTo_verifyOtp() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Verify OTP").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send OTP").assertDoesNotExist()
    }

    @Test
    fun loginScreen_afterOtpSent_phoneField_isDisabled() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_afterOtpSent_successCard_isDisplayed() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_afterOtpSent_successCard_showsPhoneNumber() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        // WHY onNodeWithTag SUCCESS_CARD not text?
        // "9876543210" exists in BOTH phone field AND success card
        // → "Expected at most 1 node but found 2"
        // SUCCESS_CARD tag is unique → no ambiguity
        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_afterOtpSent_resendButton_isDisplayed() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — OTP Validation
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_shortOtp_showsOtpErrorCard() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("1234")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
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

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        // Type 6 valid digits one by one
        "123456".forEach { digit ->
            composeTestRule
                .onNodeWithTag(LoginTestTags.OTP_FIELD)
                .performTextInput(digit.toString())
        }
        // Try to type 2 more — should be rejected
        "78".forEach { digit ->
            composeTestRule
                .onNodeWithTag(LoginTestTags.OTP_FIELD)
                .performTextInput(digit.toString())
        }
        composeTestRule.waitForIdle()

        // Field shows only 6 digits
        composeTestRule.onNodeWithText("123456").assertIsDisplayed()
    }

    @Test
    fun loginScreen_typingOtp_afterError_clearsOtpErrorCard() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        fakeRepo.verifyOtpResult = Result.failure(Exception("Wrong OTP"))
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("000000")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        // Error card visible
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertIsDisplayed()

        // REPLACE text (not append) → triggers resetState() → error clears
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextReplacement("1")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertDoesNotExist()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Successful Login
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_correctOtp_calls_onLoginSuccess() {
        fakeRepo.sendOtpResult   = Result.success(Unit)
        fakeRepo.verifyOtpResult = Result.success(
            User(id = "u1", name = "Swapna", email = "", phone = "9876543210")
        )

        var loginSuccessCalled = false
        setContent(onLoginSuccess = { loginSuccessCalled = true })

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("123456")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        assert(loginSuccessCalled) {
            "onLoginSuccess was not called after correct OTP"
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Error States
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_sendOtp_networkError_showsErrorCard() {
        fakeRepo.sendOtpResult = Result.failure(Exception("Network unavailable"))
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Network unavailable")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_sendOtp_error_button_remainsEnabled() {
        fakeRepo.sendOtpResult = Result.failure(Exception("Error"))
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_wrongOtp_showsCorrectErrorMessage() {
        fakeRepo.sendOtpResult   = Result.success(Unit)
        fakeRepo.verifyOtpResult = Result.failure(
            Exception("Wrong OTP. Please check and try again.")
        )
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("000000")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Wrong OTP. Please check and try again.")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_ERROR_CARD)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Resend OTP
    // ══════════════════════════════════════════════════════════

    @Test
    fun loginScreen_resendOtp_clearsOtpField() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("1234")
        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1234").assertDoesNotExist()
    }

    @Test
    fun loginScreen_resendOtp_keepsSamePhoneNumber() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        // Phone field still has the number
        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsDisplayed()

        // Success card still shown after resend
        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }


    @Test
    fun loginScreen_resendOtp_successCard_stillDisplayed() {
        fakeRepo.sendOtpResult = Result.success(Unit)
        setContent()

        composeTestRule
            .onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")
        composeTestRule
            .onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }
}