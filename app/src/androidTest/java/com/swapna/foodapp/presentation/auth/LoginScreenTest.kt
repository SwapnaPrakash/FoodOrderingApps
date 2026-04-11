package com.swapna.foodapp.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.fakes.FakeUserRepositoryForTest
import com.swapna.foodapp.presentation.ui.theme.FoodAppTheme
import com.swapna.foodapp.utils.LoginTestTags
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeRepo: FakeUserRepositoryForTest
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeUserRepositoryForTest()
        viewModel = AuthViewModel(fakeRepo)
    }

    // ─────────────────────────────────────────────
    // LAUNCH SCREEN (IMPORTANT)
    // ─────────────────────────────────────────────

    private fun launchScreen() {
        composeRule.setContent {
            FoodAppTheme {
                val navController = rememberNavController()
                LoginScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
        composeRule.waitForIdle()

        // ✅ Ensure root exists before proceeding
        composeRule.onRoot().assertExists()
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private fun waitForTag(tag: String, timeout: Long = 5000) {
        composeRule.waitUntil(timeout) {
            composeRule.onAllNodesWithTag(tag)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun sendOtp(phone: String = "9876543210") {

        composeRule.waitForIdle() // ✅ ensure UI ready

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertExists() // ✅ debug safety
            .performTextInput(phone)

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .assertExists()
            .performClick()

        composeRule.waitForIdle() // ✅ wait after click

        waitForTag(LoginTestTags.OTP_FIELD)
    }

    // ─────────────────────────────────────────────
    // INITIAL UI
    // ─────────────────────────────────────────────

    @Test
    fun initial_ui_isCorrect() {
        launchScreen()

        composeRule.onNodeWithTag(LoginTestTags.LOGO).assertExists()
        composeRule.onNodeWithTag(LoginTestTags.TITLE).assertTextContains("Login")
        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD).assertExists()
        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON).assertIsEnabled()
        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD).assertDoesNotExist()
    }

    // ─────────────────────────────────────────────
    // PHONE VALIDATION
    // ─────────────────────────────────────────────

    @Test
    fun emptyPhone_showsError() {
        launchScreen()

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        waitForTag(LoginTestTags.PHONE_ERROR_CARD)

        composeRule.onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertIsDisplayed()
    }

    @Test
    fun invalidPhone_showsError() {
        launchScreen()

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("123")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        waitForTag(LoginTestTags.PHONE_ERROR_CARD)
    }

    @Test
    fun validPhone_removesError() {
        launchScreen()

        // trigger error
        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        waitForTag(LoginTestTags.PHONE_ERROR_CARD)

        // type valid
        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextClearance()

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertDoesNotExist()
    }

    // ─────────────────────────────────────────────
    // OTP FLOW
    // ─────────────────────────────────────────────

    @Test
    fun sendOtp_showsOtpField() {
        launchScreen()
        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun sendOtp_changesButtonText() {
        launchScreen()
        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .assertTextContains("Verify OTP")
    }

    @Test
    fun sendOtp_disablesPhoneField() {
        launchScreen()
        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsNotEnabled()
    }

    @Test
    fun sendOtp_showsSuccessCard() {
        launchScreen()
        sendOtp()

        waitForTag(LoginTestTags.SUCCESS_CARD)

        composeRule.onNodeWithTag(LoginTestTags.SUCCESS_CARD)
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // OTP VALIDATION
    // ─────────────────────────────────────────────

    @Test
    fun shortOtp_showsError() {
        launchScreen()
        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("123")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        waitForTag(LoginTestTags.OTP_ERROR_CARD)
    }

    @Test
    fun correctOtp_successState() {
        launchScreen()

        fakeRepo.verifyOtpResult = Result.success(
            FakeUserRepositoryForTest.testUser()
        )

        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("123456")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        composeRule.waitUntil(5000) {
            viewModel.state.value is AuthViewModel.AuthState.Success
        }
    }

    @Test
    fun wrongOtp_showsError() {
        launchScreen()

        fakeRepo.verifyOtpResult = Result.failure(
            Exception("Wrong OTP")
        )

        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("000000")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        waitForTag(LoginTestTags.OTP_ERROR_CARD)
    }

    // ─────────────────────────────────────────────
    // RESEND OTP
    // ─────────────────────────────────────────────

    @Test
    fun resendOtp_works() {
        launchScreen()
        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.RESEND_BUTTON)
            .performClick()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // ERROR STATES
    // ─────────────────────────────────────────────

    @Test
    fun networkError_showsError() {
        launchScreen()

        fakeRepo.sendOtpResult = Result.failure(Exception("Network error"))

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .performTextInput("9876543210")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        waitForTag(LoginTestTags.PHONE_ERROR_CARD)
    }
}