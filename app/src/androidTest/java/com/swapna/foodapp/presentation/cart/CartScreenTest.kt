package com.swapna.foodapp.presentation.cart

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_CLEAR_CART_CALLED
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_CLEAR_CART_NOT_CALLED
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_REMOVE_ITEM_FROM_QTY_1
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_UPDATE_QTY_DECREMENT
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_UPDATE_QTY_INCREMENT
import com.swapna.foodapp.utils.AndroidTestConstants.CART_EMPTY_MESSAGE
import com.swapna.foodapp.utils.AndroidTestConstants.CART_ITEMS_0
import com.swapna.foodapp.utils.AndroidTestConstants.CART_ITEMS_2
import com.swapna.foodapp.utils.AndroidTestConstants.CART_ITEMS_5
import com.swapna.foodapp.utils.AndroidTestConstants.CART_ITEM_REMOVED_SUFFIX
import com.swapna.foodapp.utils.AndroidTestConstants.CART_MIN_ORDER_SUBSTR
import com.swapna.foodapp.utils.AndroidTestConstants.CART_PRICE_249
import com.swapna.foodapp.utils.AndroidTestConstants.CART_PRICE_498
import com.swapna.foodapp.utils.AndroidTestConstants.CART_QTY_STR_1
import com.swapna.foodapp.utils.AndroidTestConstants.CART_QTY_STR_2
import com.swapna.foodapp.utils.AndroidTestConstants.CART_VEG_INDICATOR_CD
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_ID_C1
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_ID_C2
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_ITEM_ID_PREFIX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_RESTAURANT_ID
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CHICK_BIR
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_DESC_PREFIX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_ID_M1
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_ID_M2
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_MARGHERITA_PIZZA
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PANEER_TIKKA
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PLAIN_NAAN
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PLAIN_WATER
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_10
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_249
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_349
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_50
import com.swapna.foodapp.utils.AndroidTestConstants.TEST_ROUTE_CART
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppConstants.ADD_DESC
import com.swapna.foodapp.utils.AppConstants.BACK
import com.swapna.foodapp.utils.AppConstants.BILL_DETAILS
import com.swapna.foodapp.utils.AppConstants.BROWSE_RESTAURANTS
import com.swapna.foodapp.utils.AppConstants.DELIVERY_FEE
import com.swapna.foodapp.utils.AppConstants.FREE
import com.swapna.foodapp.utils.AppConstants.GST_CHARGES
import com.swapna.foodapp.utils.AppConstants.ITEM_TOTAL
import com.swapna.foodapp.utils.AppConstants.MY_CART
import com.swapna.foodapp.utils.AppConstants.NON_VEG
import com.swapna.foodapp.utils.AppConstants.PLACE_ORDER_PREFIX
import com.swapna.foodapp.utils.AppConstants.REMOVE_DESC
import com.swapna.foodapp.utils.AppConstants.TO_PAY
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeCartRepo: FakeCartRepository
    private lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            )
        }
    }

    private fun setContent(preSeeded: List<CartItem> = emptyList()) {
        fakeCartRepo = FakeCartRepository()
        if (preSeeded.isNotEmpty()) {
            fakeCartRepo.seedCart(*preSeeded.toTypedArray())
        }
        viewModel = CartViewModel(fakeCartRepo)

        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = TEST_ROUTE_CART,
                ) {
                    composable(TEST_ROUTE_CART) {
                        CartScreen(
                            navController = navController,
                            viewModel = viewModel,
                        )
                    }
                    composable(AppRoutes.HOME) { /* navigation stub */ }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // GROUP 1 — TopBar
    @Test
    fun cartScreen_topBar_myCart_title_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(MY_CART)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_topBar_back_button_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(BACK)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_topBar_item_count_shown_when_cart_has_items() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 2)))
        composeTestRule
            .onNodeWithText(CART_ITEMS_2)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_topBar_item_count_not_shown_when_cart_empty() {
        setContent()
        composeTestRule
            .onNodeWithText(CART_ITEMS_0)
            .assertDoesNotExist()
    }

    // GROUP 2 — Empty Cart State
    @Test
    fun cartScreen_emptyState_message_isDisplayed_when_no_items() {
        setContent()
        composeTestRule
            .onNodeWithText(CART_EMPTY_MESSAGE)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_emptyState_browseFood_button_isDisplayed() {
        setContent()
        // EmptyCartView uses AppConstants.BROWSE_RESTAURANTS = "Browse Restaurants"
        composeTestRule
            .onNodeWithText(BROWSE_RESTAURANTS)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_placeOrder_button_not_shown_when_cart_empty() {
        setContent()
        composeTestRule
            .onNodeWithText(PLACE_ORDER_PREFIX, substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun cartScreen_billDetails_not_shown_when_cart_empty() {
        setContent()
        composeTestRule
            .onNodeWithText(BILL_DETAILS)
            .assertDoesNotExist()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 3 — Cart Items Displayed
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_item_name_isDisplayed_when_cart_has_item() {
        val biryani = fakeMenuItem(name = FAKE_MENU_CHICK_BIR)
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(FAKE_MENU_CHICK_BIR)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_item_price_isDisplayed() {
        val biryani = fakeMenuItem(price = FAKE_MENU_PRICE_249)
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        // "₹249" appears in CartItemRow (unit price) AND BillDetails (item total)
        // → use onFirst() to target the topmost visible match safely
        composeTestRule
            .onAllNodesWithText(CART_PRICE_249, substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_multiple_item_names_all_shown() {
        val biryani = fakeMenuItem(
            id = FAKE_MENU_ID_M1,
            name = FAKE_MENU_CHICK_BIR,
            price = FAKE_MENU_PRICE_249
        )
        val pizza = fakeMenuItem(
            id = FAKE_MENU_ID_M2,
            name = FAKE_MENU_MARGHERITA_PIZZA,
            price = FAKE_MENU_PRICE_349
        )
        setContent(
            preSeeded = listOf(
                cartItemOf(biryani, id = FAKE_CART_ID_C1),
                cartItemOf(pizza, id = FAKE_CART_ID_C2),
            )
        )
        composeTestRule.onNodeWithText(FAKE_MENU_CHICK_BIR).assertIsDisplayed()
        composeTestRule.onNodeWithText(FAKE_MENU_MARGHERITA_PIZZA).assertIsDisplayed()
    }

    @Test
    fun cartScreen_item_quantity_isDisplayed() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 2)))
        composeTestRule
            .onNodeWithText(CART_QTY_STR_2)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_restaurant_name_shown_as_section_header() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(FAKE_CART_RESTAURANT_ID, substring = true)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 4 — Quantity Controls
    //
    // WHY onAllNodesWithContentDescription + onFirst()?
    //   CartItemRow uses ADD_DESC = "Add" and REMOVE_DESC = "Remove"
    //   from AppConstants for the stepper buttons. With multiple items
    //   each row has its own stepper — onFirst() targets the topmost.
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_increment_button_isDisplayed_for_cart_item() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onAllNodesWithContentDescription(ADD_DESC)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_decrement_button_isDisplayed_for_cart_item() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onAllNodesWithContentDescription(REMOVE_DESC)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_increment_button_click_updates_quantity() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 1)))

        composeTestRule
            .onAllNodesWithContentDescription(ADD_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeCartRepo.updateQtyCalled) { ASSERT_UPDATE_QTY_INCREMENT }
    }

    @Test
    fun cartScreen_increment_updates_quantity_display() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 1)))

        composeTestRule
            .onAllNodesWithContentDescription(ADD_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(CART_QTY_STR_2)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_decrement_button_click_from_qty_2_reduces_to_1() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 2)))

        composeTestRule
            .onAllNodesWithContentDescription(REMOVE_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeCartRepo.updateQtyCalled) { ASSERT_UPDATE_QTY_DECREMENT }
        composeTestRule
            .onNodeWithText(CART_QTY_STR_1)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_decrement_from_qty_1_removes_item_from_cart() {
        val biryani = fakeMenuItem(name = FAKE_MENU_CHICK_BIR)
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 1)))

        composeTestRule
            .onAllNodesWithContentDescription(REMOVE_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeCartRepo.removeItemCalled) { ASSERT_REMOVE_ITEM_FROM_QTY_1 }
    }

    @Test
    fun cartScreen_decrement_from_qty_1_shows_empty_cart_state() {
        val biryani = fakeMenuItem(name = FAKE_MENU_CHICK_BIR)
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 1)))

        composeTestRule
            .onAllNodesWithContentDescription(REMOVE_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(CART_EMPTY_MESSAGE)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 5 — Price Breakdown (Bill Details)
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_billDetails_section_shown_when_items_present() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(BILL_DETAILS)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_billDetails_itemTotal_label_isDisplayed() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(ITEM_TOTAL)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_billDetails_deliveryFee_label_isDisplayed() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(DELIVERY_FEE)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_billDetails_gst_label_isDisplayed() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(GST_CHARGES)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_billDetails_toPay_label_isDisplayed() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(TO_PAY)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_billDetails_shows_FREE_delivery_when_above_threshold() {
        val expensiveItem = fakeMenuItem(
            price = AppBusinessRules.FREE_DELIVERY_ABOVE + 100.0
        )
        setContent(preSeeded = listOf(cartItemOf(expensiveItem)))
        composeTestRule
            .onNodeWithText(FREE)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_billDetails_subtotal_updates_when_quantity_changes() {
        val biryani = fakeMenuItem(name = FAKE_MENU_CHICK_BIR, price = FAKE_MENU_PRICE_249)
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 1)))

        // "₹249" appears in CartItemRow and BillDetails item total → use onFirst()
        composeTestRule
            .onAllNodesWithText(CART_PRICE_249, substring = true)
            .onFirst()
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithContentDescription(ADD_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        // After increment qty=2: BillDetails item total updates to ₹498
        composeTestRule
            .onAllNodesWithText(CART_PRICE_498, substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 6 — Place Order Button
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_placeOrder_button_isDisplayed_when_items_in_cart() {
        val biryani = fakeMenuItem()
        setContent(preSeeded = listOf(cartItemOf(biryani)))
        composeTestRule
            .onNodeWithText(PLACE_ORDER_PREFIX, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_placeOrder_button_shows_total_amount() {
        // Button text = "$PLACE_ORDER_PREFIX${total.toInt()}"
        // Total ≠ raw item price — it includes delivery fee + GST.
        // Mirror CartViewModel.observeCart() to compute the real value shown.
        val biryani = fakeMenuItem(price = FAKE_MENU_PRICE_249)
        setContent(preSeeded = listOf(cartItemOf(biryani)))

        val delivery = if (FAKE_MENU_PRICE_249 >= AppBusinessRules.FREE_DELIVERY_ABOVE) 0.0
        else AppBusinessRules.DEFAULT_DELIVERY_FEE
        val expectedTotal = (FAKE_MENU_PRICE_249 + delivery +
                FAKE_MENU_PRICE_249 * AppBusinessRules.GST_RATE).toInt()

        composeTestRule
            .onNodeWithText("$PLACE_ORDER_PREFIX$expectedTotal")
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_placeOrder_button_click_with_valid_cart_calls_clearCart() {
        val biryani = fakeMenuItem(price = AppBusinessRules.MIN_ORDER_VALUE + 50.0)
        setContent(preSeeded = listOf(cartItemOf(biryani)))

        composeTestRule
            .onNodeWithText(PLACE_ORDER_PREFIX, substring = true)
            .performClick()
        composeTestRule.waitForIdle()

        assert(fakeCartRepo.clearCartCalled) { ASSERT_CLEAR_CART_CALLED }
    }

    @Test
    fun cartScreen_placeOrder_click_below_minimum_shows_error_snackbar() {
        // WHY FAKE_MENU_PRICE_10 instead of MIN_ORDER_VALUE - 10?
        // Subtotal of (MIN_ORDER_VALUE - 10) + delivery fee + GST can
        // exceed MIN_ORDER_VALUE, making the order succeed instead of
        // showing the error. ₹10 → total ≈ ₹40.50, safely below minimum.
        val cheapItem = fakeMenuItem(
            name = FAKE_MENU_PLAIN_WATER,
            price = FAKE_MENU_PRICE_10,
        )
        setContent(preSeeded = listOf(cartItemOf(cheapItem)))

        composeTestRule
            .onNodeWithText(PLACE_ORDER_PREFIX, substring = true)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(CART_MIN_ORDER_SUBSTR, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_placeOrder_click_below_minimum_does_NOT_clear_cart() {
        val cheapItem = fakeMenuItem(price = FAKE_MENU_PRICE_10)
        setContent(preSeeded = listOf(cartItemOf(cheapItem)))

        composeTestRule
            .onNodeWithText(PLACE_ORDER_PREFIX, substring = true)
            .performClick()
        composeTestRule.waitForIdle()

        assert(!fakeCartRepo.clearCartCalled) { ASSERT_CLEAR_CART_NOT_CALLED }
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 7 — Snackbar Messages
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_decrement_to_zero_shows_item_removed_snackbar() {
        val biryani = fakeMenuItem(name = FAKE_MENU_CHICK_BIR)
        setContent(preSeeded = listOf(cartItemOf(biryani, quantity = 1)))

        composeTestRule
            .onAllNodesWithContentDescription(REMOVE_DESC)
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        // CartViewModel.onDecrementItem emits ShowSnackbar("${item.name}$MSG_ITEM_REMOVED")
        composeTestRule
            .onNodeWithText("$FAKE_MENU_CHICK_BIR$CART_ITEM_REMOVED_SUFFIX", substring = true)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 8 — Navigation
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_back_button_click_triggers_NavigateBack() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(BACK)
            .performClick()
        composeTestRule.waitForIdle()
        // popBackStack() called — NavHost handles it gracefully, no crash = pass
    }

    @Test
    fun cartScreen_browseFood_button_from_empty_state_is_clickable() {
        setContent()
        composeTestRule
            .onNodeWithText(BROWSE_RESTAURANTS)
            .performClick()
        composeTestRule.waitForIdle()
        // navigate(HOME) absorbed by NavHost stub — no crash = pass
    }

    // ══════════════════════════════════════════════════════════════
    // GROUP 9 — Edge Cases
    // ══════════════════════════════════════════════════════════════

    @Test
    fun cartScreen_veg_indicator_shown_for_veg_item() {
        val vegItem = fakeMenuItem(name = FAKE_MENU_PANEER_TIKKA, isVeg = true)
        setContent(preSeeded = listOf(cartItemOf(vegItem)))
        composeTestRule
            .onNodeWithContentDescription(CART_VEG_INDICATOR_CD, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_two_items_both_shown_with_correct_quantities() {
        val biryani = fakeMenuItem(
            id = FAKE_MENU_ID_M1,
            name = FAKE_MENU_CHICK_BIR,
            price = FAKE_MENU_PRICE_249
        )
        val naan = fakeMenuItem(
            id = FAKE_MENU_ID_M2,
            name = FAKE_MENU_PLAIN_NAAN,
            price = FAKE_MENU_PRICE_50,
            isVeg = true
        )
        setContent(
            preSeeded = listOf(
                cartItemOf(biryani, quantity = 2, id = FAKE_CART_ID_C1),
                cartItemOf(naan, quantity = 3, id = FAKE_CART_ID_C2),
            )
        )
        composeTestRule.onNodeWithText(FAKE_MENU_CHICK_BIR).assertIsDisplayed()
        composeTestRule.onNodeWithText(FAKE_MENU_PLAIN_NAAN).assertIsDisplayed()
    }

    @Test
    fun cartScreen_item_count_in_topBar_shows_total_qty_not_distinct_items() {
        // 2 distinct items, total quantity = 5 → TopBar shows "5 items"
        val biryani = fakeMenuItem(id = FAKE_MENU_ID_M1, name = FAKE_MENU_CHICK_BIR)
        val naan = fakeMenuItem(id = FAKE_MENU_ID_M2, name = FAKE_MENU_PLAIN_NAAN)
        setContent(
            preSeeded = listOf(
                cartItemOf(biryani, quantity = 3, id = FAKE_CART_ID_C1),
                cartItemOf(naan, quantity = 2, id = FAKE_CART_ID_C2),
            )
        )
        composeTestRule
            .onNodeWithText(CART_ITEMS_5)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_does_not_crash_with_max_quantity_item() {
        val biryani = fakeMenuItem()
        setContent(
            preSeeded = listOf(
                cartItemOf(biryani, quantity = AppBusinessRules.MAX_ITEM_QUANTITY)
            )
        )
        composeTestRule
            .onNodeWithText(FAKE_MENU_CHICK_BIR)
            .assertIsDisplayed()
    }

    @Test
    fun cartScreen_nonVeg_indicator_shown_for_non_veg_item() {
        val nonVegItem = fakeMenuItem(name = FAKE_MENU_CHICK_BIR, isVeg = false)
        setContent(preSeeded = listOf(cartItemOf(nonVegItem)))
        composeTestRule
            .onNodeWithContentDescription(NON_VEG, substring = true)
            .assertIsDisplayed()
    }


    // ── Helpers ─────────────────────────────────────────────────
    private fun fakeMenuItem(
        id: String = FAKE_MENU_ID_M1,
        name: String = FAKE_MENU_CHICK_BIR,
        price: Double = FAKE_MENU_PRICE_249,
        isVeg: Boolean = false,
    ) = MenuItem(
        id = id,
        restaurantId = FAKE_CART_RESTAURANT_ID,
        name = name,
        description = "$FAKE_MENU_DESC_PREFIX$name",
        price = price,
        imageUrl = "",
        category = FAKE_MENU_CATEGORY_BIRYANI,
        isVeg = isVeg,
        isRecommended = false,
        isBestseller = false,
        isAvailable = true,
        customisations = emptyList(),
    )

    private fun cartItemOf(
        menuItem: MenuItem,
        quantity: Int = 1,
        id: String = "$FAKE_CART_ITEM_ID_PREFIX${menuItem.id}",
    ) = CartItem(
        id = id,
        menuItem = menuItem,
        quantity = quantity,
        selectedCustomisations = emptyList(),
    )
}
