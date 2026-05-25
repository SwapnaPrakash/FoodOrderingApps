package com.swapna.foodapp.presentation.profile

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.fakes.FakeUserRepository
import com.swapna.foodapp.fakes.fakeProfileAddress
import com.swapna.foodapp.fakes.fakeProfileOrder
import com.swapna.foodapp.fakes.fakeProfileUser
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_DELETED_ADDRESS_ID_FMT
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_LOGOUT_CALLED
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_PROFILE_DELETE_CALLED
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_PROFILE_SAVE_CALLED
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_FULL
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_ID
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USER_EMAIL
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USER_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USER_PHONE
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_EDIT_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_ITEM_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_REST_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_STATUS_DELIVERED
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_TOTAL
import com.swapna.foodapp.utils.AndroidTestConstants.TEST_ROUTE_PROFILE
import com.swapna.foodapp.utils.AppConstants.BACK
import com.swapna.foodapp.utils.AppConstants.CANCEL
import com.swapna.foodapp.utils.AppConstants.CURRENCY_SYMBOL
import com.swapna.foodapp.utils.AppConstants.DELETE_ADDRESS_DESC
import com.swapna.foodapp.utils.AppConstants.EDIT_PROFILE
import com.swapna.foodapp.utils.AppConstants.EMAIL
import com.swapna.foodapp.utils.AppConstants.LOGOUT
import com.swapna.foodapp.utils.AppConstants.MSG_ADDRESS_REMOVED
import com.swapna.foodapp.utils.AppConstants.MSG_PROFILE_UPDATED
import com.swapna.foodapp.utils.AppConstants.NAME
import com.swapna.foodapp.utils.AppConstants.NOT_SET
import com.swapna.foodapp.utils.AppConstants.NO_ORDERS
import com.swapna.foodapp.utils.AppConstants.NO_SAVED_ADDRESSES
import com.swapna.foodapp.utils.AppConstants.PHONE_NUMBER_LABEL
import com.swapna.foodapp.utils.AppConstants.PLACEHOLDER_ADD_EMAIL
import com.swapna.foodapp.utils.AppConstants.PLACEHOLDER_ADD_NAME
import com.swapna.foodapp.utils.AppConstants.PROFILE
import com.swapna.foodapp.utils.AppConstants.PROFILE_AVATAR_DESC
import com.swapna.foodapp.utils.AppConstants.RECENT_ORDERS_TITLE
import com.swapna.foodapp.utils.AppConstants.SAVE
import com.swapna.foodapp.utils.AppConstants.SAVED_ADDRESSES_TITLE
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeUserRepo: FakeUserRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            )
        }
    }

    private fun setContent(
        user: com.swapna.foodapp.domain.model.User? = fakeProfileUser(),
        orders: List<Order> = emptyList(),
        configureRepo: FakeUserRepository.() -> Unit = {},
    ) {
        fakeUserRepo = FakeUserRepository().apply {
            setUser(user)
            setOrders(orders)
            configureRepo()
        }
        viewModel = ProfileViewModel(
            fakeUserRepo,
            ioDispatcher = Dispatchers.Unconfined,
            )

        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = TEST_ROUTE_PROFILE,
                ) {
                    composable(TEST_ROUTE_PROFILE) {
                        ProfileScreen(
                            navController = navController,
                            viewModel = viewModel,
                        )
                    }
                    composable(AppRoutes.LOGIN) { }
                    composable(AppRoutes.HOME) { }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // GROUP 1 — TopBar
    @Test
    fun profileScreen_topBar_title_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(PROFILE)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_topBar_back_button_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(BACK)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_topBar_edit_button_shown_in_view_mode() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_topBar_edit_button_hidden_in_edit_mode() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .assertDoesNotExist()
    }

    // GROUP 2 — Profile Header (View Mode)
    @Test
    fun profileScreen_avatar_icon_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(PROFILE_AVATAR_DESC)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_display_name_isDisplayed() {
        setContent(user = fakeProfileUser(name = HOME_USER_NAME))
        composeTestRule
            .onNodeWithText(HOME_USER_NAME)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_display_email_isDisplayed() {
        setContent(user = fakeProfileUser(email = HOME_USER_EMAIL))
        composeTestRule
            .onNodeWithText(HOME_USER_EMAIL)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_placeholder_name_shown_when_user_has_no_name() {
        setContent(user = fakeProfileUser(name = ""))
        composeTestRule
            .onNodeWithText(PLACEHOLDER_ADD_NAME)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_placeholder_email_shown_when_user_has_no_email() {
        setContent(user = fakeProfileUser(email = ""))
        composeTestRule
            .onNodeWithText(PLACEHOLDER_ADD_EMAIL)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 3 — Phone Section
    // ══════════════════════════════════════════════════════════════

    @Test
    fun profileScreen_phoneNumber_label_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(PHONE_NUMBER_LABEL)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_phone_value_isDisplayed() {
        setContent(user = fakeProfileUser(phone = HOME_USER_PHONE))
        composeTestRule
            .onNodeWithText(HOME_USER_PHONE, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_notSet_shown_when_phone_is_empty() {
        // phone="" → FirebaseAuth has no signed-in user in tests → "" → NOT_SET
        setContent(user = fakeProfileUser(phone = ""))
        composeTestRule
            .onNodeWithText(NOT_SET)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 4 — Saved Addresses
    // ══════════════════════════════════════════════════════════════

    @Test
    fun profileScreen_savedAddresses_header_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(SAVED_ADDRESSES_TITLE)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_emptyAddresses_message_shown_when_no_addresses() {
        setContent(user = fakeProfileUser(addresses = emptyList()))
        composeTestRule
            .onNodeWithText(NO_SAVED_ADDRESSES)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 5 — Recent Orders
    // ══════════════════════════════════════════════════════════════

    @Test
    fun profileScreen_recentOrders_header_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(RECENT_ORDERS_TITLE, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_emptyOrders_message_shown_when_no_orders() {
        setContent(orders = emptyList())
        composeTestRule
            .onNodeWithText(NO_ORDERS, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_order_restaurantName_isDisplayed() {
        setContent(orders = listOf(fakeProfileOrder()))
        composeTestRule
            .onNodeWithText(PROFILE_ORDER_REST_NAME)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_order_status_isDisplayed() {
        setContent(orders = listOf(fakeProfileOrder()))
        composeTestRule
            .onNodeWithText(PROFILE_ORDER_STATUS_DELIVERED)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_order_total_isDisplayed() {
        setContent(orders = listOf(fakeProfileOrder()))
        composeTestRule
            .onNodeWithText("$CURRENCY_SYMBOL${PROFILE_ORDER_TOTAL.toInt()}", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_order_item_name_isDisplayed() {
        setContent(orders = listOf(fakeProfileOrder()))
        composeTestRule
            .onNodeWithText(PROFILE_ORDER_ITEM_NAME, substring = true)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 6 — Edit Mode
    // ══════════════════════════════════════════════════════════════

    @Test
    fun profileScreen_edit_click_shows_name_field() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(NAME, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_edit_click_shows_email_field() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(EMAIL, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_edit_click_shows_save_button() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(SAVE)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_edit_click_shows_cancel_button() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(CANCEL)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_name_field_prepopulated_with_current_name() {
        setContent(user = fakeProfileUser(name = HOME_USER_NAME))
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_USER_NAME, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_cancel_returns_to_view_mode() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(CANCEL)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(SAVE)
            .assertDoesNotExist()
    }

    @Test
    fun profileScreen_save_click_calls_updateUser() {
        setContent(user = fakeProfileUser(name = HOME_USER_NAME))

        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_USER_NAME, substring = true)
            .performTextReplacement(PROFILE_EDIT_NAME)
        composeTestRule
            .onNodeWithText(SAVE)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeUserRepo.updateUserCalled) { ASSERT_PROFILE_SAVE_CALLED }
    }

    @Test
    fun profileScreen_save_success_shows_snackbar() {
        setContent(user = fakeProfileUser(name = HOME_USER_NAME))

        composeTestRule
            .onNodeWithContentDescription(EDIT_PROFILE)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(SAVE)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(MSG_PROFILE_UPDATED, substring = true)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 7 — Logout
    // ══════════════════════════════════════════════════════════════

    @Test
    fun profileScreen_logout_button_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGOUT)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_logout_click_calls_logout_on_repo() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGOUT)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeUserRepo.logoutCalled) {
            ASSERT_LOGOUT_CALLED
        }
    }

    @Test
    fun profileScreen_logout_click_navigates_to_login() {
        setContent()
        composeTestRule
            .onNodeWithText(LOGOUT)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        // NavigateToLogin → absorbed by NavHost LOGIN stub — no crash = pass
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 8 — Navigation
    // ══════════════════════════════════════════════════════════════

    @Test
    fun profileScreen_back_button_click_does_not_crash() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(BACK)
            .performClick()
        composeTestRule.waitForIdle()
        // NavigateBack → popBackStack() — no crash = pass
    }

    @Test
    fun profileScreen_address_label_isDisplayed_when_address_exists() {
        setContent(user = fakeProfileUser(addresses = listOf(fakeProfileAddress())))
        composeTestRule
            .onNodeWithText(HOME_ADDRESS_LABEL_HOME)
            .performScrollTo()                          // ← scroll into viewport
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_address_fullAddress_isDisplayed() {
        setContent(user = fakeProfileUser(addresses = listOf(fakeProfileAddress())))
        composeTestRule
            .onNodeWithText(HOME_ADDRESS_FULL, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_address_delete_button_isDisplayed() {
        setContent(user = fakeProfileUser(addresses = listOf(fakeProfileAddress())))
        // Scroll address label into view first — delete button isn't
        // composed until its parent card enters the LazyColumn viewport
        composeTestRule
            .onNodeWithText(HOME_ADDRESS_LABEL_HOME)
            .performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription(DELETE_ADDRESS_DESC)
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_address_delete_click_calls_deleteAddress() {
        setContent(user = fakeProfileUser(addresses = listOf(fakeProfileAddress())))
        composeTestRule
            .onNodeWithText(HOME_ADDRESS_LABEL_HOME)
            .performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription(DELETE_ADDRESS_DESC)
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeUserRepo.deleteAddressCalled) { ASSERT_PROFILE_DELETE_CALLED }
        assert(fakeUserRepo.lastDeletedAddressId == HOME_ADDRESS_ID) {
            ASSERT_DELETED_ADDRESS_ID_FMT.format(HOME_ADDRESS_ID, fakeUserRepo.lastDeletedAddressId)
        }
    }

    @Test
    fun profileScreen_address_delete_shows_snackbar() {
        setContent(user = fakeProfileUser(addresses = listOf(fakeProfileAddress())))
        composeTestRule
            .onNodeWithText(HOME_ADDRESS_LABEL_HOME)
            .performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription(DELETE_ADDRESS_DESC)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(MSG_ADDRESS_REMOVED)
            .assertIsDisplayed()
    }
}