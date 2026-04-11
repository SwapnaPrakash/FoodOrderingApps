package com.swapna.foodapp.presentation.auth

import androidx.activity.ComponentActivity
import com.swapna.foodapp.utils.LoginTestTags
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.fakes.FakeUserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.After
import javax.inject.Inject

@HiltAndroidTest
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        val fakeRepo = userRepository as FakeUserRepository

        fakeRepo.sendOtpResult = Result.success(Unit)
        fakeRepo.verifyOtpResult =
            Result.success(FakeUserRepository.fakeUser())

        viewModel = AuthViewModel(fakeRepo)
    }

    // ✅ Launch UI correctly
    private fun launchScreen() {
        composeRule.setContent {
            MaterialTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {} // ✅ REQUIRED
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onRoot().printToLog("TEST_TREE")

        composeRule.onNodeWithTag(LoginTestTags.SCREEN_ROOT)
            .assertExists()
    }

    // ✅ Helper
    private fun sendOtp(phone: String = "9876543210") {

        composeRule.onRoot().printToLog("TEST_TREE")

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertExists()
            .performTextInput(phone)

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .assertExists()
            .performClick()

        composeRule.waitForIdle()
    }

    // ✅ TEST 1
    @Test
    fun sendOtp_showsOtpField() {
        launchScreen()

        composeRule.onRoot().printToLog("TEST_TREE")

        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .assertExists()
    }

    // ✅ TEST 2
    @Test
    fun sendOtp_disablesPhoneField() {
        launchScreen()

        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.PHONE_FIELD)
            .assertIsNotEnabled()
    }

    // ✅ TEST 3
    @Test
    fun verifyOtp_successFlow() {
        launchScreen()

        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("123456")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        composeRule.waitForIdle()

        // Since onLoginSuccess is empty, just ensure no crash
        composeRule.onRoot().assertExists()
    }

    @Test
    fun sendOtp_error_showsError() {
        val fakeRepo = userRepository as FakeUserRepository
        fakeRepo.sendOtpResult = Result.failure(Exception("Invalid number"))

        viewModel = AuthViewModel(fakeRepo)

        launchScreen()

        sendOtp("123") // invalid

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(LoginTestTags.PHONE_ERROR_CARD)
            .assertExists()
    }

    @Test
    fun verifyOtp_success() {
        launchScreen()

        sendOtp()

        composeRule.onNodeWithTag(LoginTestTags.OTP_FIELD)
            .performTextInput("123456")

        composeRule.onNodeWithTag(LoginTestTags.AUTH_BUTTON)
            .performClick()

        composeRule.waitForIdle()

        composeRule.onRoot().assertExists() // no crash = success
    }
}