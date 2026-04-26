package com.swapna.foodapp.presentation.restaurant

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.TestConstants.CART_COUNT_3
import com.swapna.foodapp.utils.TestConstants.CART_ID_STR_1
import com.swapna.foodapp.utils.TestConstants.CART_UUID_1
import com.swapna.foodapp.utils.TestConstants.CART_UUID_2
import com.swapna.foodapp.utils.TestConstants.CATEGORY_BIRYANI_CAT
import com.swapna.foodapp.utils.TestConstants.CATEGORY_STARTERS
import com.swapna.foodapp.utils.TestConstants.ERR_COULD_NOT_ADD_CART
import com.swapna.foodapp.utils.TestConstants.ERR_COULD_NOT_LOAD_REST
import com.swapna.foodapp.utils.TestConstants.ERR_NO_INTERNET_HOME
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.MENU_CATEGORY_COUNT
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_3
import com.swapna.foodapp.utils.TestConstants.MENU_ID_4
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_65
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_PANEER_TIKKA
import com.swapna.foodapp.utils.TestConstants.OFFER_50_OFF
import com.swapna.foodapp.utils.TestConstants.PRICE_100
import com.swapna.foodapp.utils.TestConstants.PRICE_179
import com.swapna.foodapp.utils.TestConstants.PRICE_199
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_349
import com.swapna.foodapp.utils.TestConstants.PRICE_50
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ADDRESS_KORA
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_COST_500
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_2
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MEGHANA
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MIN_ORDER_100
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_RATING_45
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_VOTES_5000
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelSpec : BehaviorSpec({

    val dispatcher = UnconfinedTestDispatcher()

    val restaurantRepository = mockk<RestaurantRepository>()
    val cartRepository = mockk<CartRepository>()
    val addToCartUseCase = mockk<AddToCartUseCase>()

    val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())

    // ── Test data ─────────────────────────────────────────────
    val chickenBiryani = testMenuItem(
        MENU_ID_1,
        MENU_ITEM_CHICK_BIR,
        PRICE_249,
        isRecommended = true,
        isBestseller = true
    )
    val muttonBiryani = testMenuItem(MENU_ID_2, MENU_ITEM_MUTTON_BIR, PRICE_349)
    val chicken65 = testMenuItem(
        MENU_ID_3,
        MENU_ITEM_CHICK_65,
        PRICE_199,
        isRecommended = true,
        category = CATEGORY_STARTERS
    )
    val paneerTikka = testMenuItem(
        MENU_ID_4,
        MENU_ITEM_PANEER_TIKKA,
        PRICE_179,
        isVeg = true,
        category = CATEGORY_STARTERS
    )

    fun createViewModel(restaurantId: String = RESTAURANT_ID_1): RestaurantViewModel {
        val handle = SavedStateHandle(
            mapOf(AppRoutes.ARG_RESTAURANT_ID to restaurantId)
        )
        return RestaurantViewModel(
            savedStateHandle = handle,
            restaurantRepository = restaurantRepository,
            cartRepository = cartRepository,
            addToCartUseCase = addToCartUseCase,
        )
    }

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)
        cartItemsFlow.value = emptyList()

        every { restaurantRepository.getRestaurantDetail(any()) } returns
                flowOf(Result.success(testRestaurant()))
        every { restaurantRepository.getMenuItems(any()) } returns
                flowOf(Result.success(testMenuByCategory()))
        every { restaurantRepository.getReviews(any()) } returns
                flowOf(Result.success(emptyList()))
        every { cartRepository.getCartItems() } returns cartItemsFlow
        every { cartRepository.getCartItemCount() } returns
                cartItemsFlow.map { items -> items.sumOf { it.quantity } }
        every { cartRepository.getCartTotal() } returns
                cartItemsFlow.map { items ->
                    val subtotal = items.sumOf { it.totalPrice }
                    val delivery = if (subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE) 0.0
                    else if (subtotal > 0) AppBusinessRules.DEFAULT_DELIVERY_FEE else 0.0
                    val taxes = subtotal * AppBusinessRules.GST_RATE
                    com.swapna.foodapp.domain.model.CartPriceBreakdown(
                        subtotal = subtotal,
                        deliveryFee = delivery,
                        taxes = taxes,
                        total = subtotal + delivery + taxes,
                    )
                }
        coEvery { addToCartUseCase(any(), any(), any()) } just runs

        coEvery { cartRepository.updateQuantity(any(), any()) } answers {
            val cartItemId = firstArg<String>()
            val newQty = secondArg<Int>()
            cartItemsFlow.update { items ->
                items.map { if (it.id == cartItemId) it.copy(quantity = newQty) else it }
            }
        }
        coEvery { cartRepository.removeItem(any()) } answers {
            val cartItemId = firstArg<String>()
            cartItemsFlow.update { items -> items.filter { it.id != cartItemId } }
        }
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial Data Loading
    // ══════════════════════════════════════════════════════════

    given("user opens RestaurantScreen") {

        `when`("restaurant detail loads successfully") {
            then("restaurant name should be Meghana Foods") {
                createViewModel().uiState.value
                    .restaurant?.name shouldBe RESTAURANT_MEGHANA
            }
        }

        `when`("restaurant detail loads — isLoading check") {
            then("isLoading should be false after loading") {
                createViewModel().uiState.value.isLoading shouldBe false
            }
        }

        `when`("restaurant detail loads — error check") {
            then("error should be null on success") {
                createViewModel().uiState.value.error shouldBe null
            }
        }

        `when`("menu loads successfully") {
            then("menuByCategory has 2 categories Biryani and Starters") {
                val menu = createViewModel().uiState.value.menuByCategory
                menu.size shouldBe MENU_CATEGORY_COUNT
                menu.containsKey(CATEGORY_BIRYANI_CAT) shouldBe true
                menu.containsKey(CATEGORY_STARTERS) shouldBe true
            }
        }

        `when`("menu loads — recommended items check") {
            then("recommended list not empty and all are isRecommended") {
                val vm = createViewModel()
                vm.uiState.value.recommended.isNotEmpty() shouldBe true
                vm.uiState.value.recommended.all { it.isRecommended } shouldBe true
            }
        }

        `when`("restaurant API throws network error") {
            then("error shown restaurant null isLoading false") {
                every { restaurantRepository.getRestaurantDetail(any()) } returns
                        flowOf(Result.failure(Exception(ERR_NO_INTERNET_HOME)))

                val vm = createViewModel()
                vm.uiState.value.error shouldBe ERR_COULD_NOT_LOAD_REST
                vm.uiState.value.restaurant shouldBe null
                vm.uiState.value.isLoading shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Cart Count in TopBar
    // ══════════════════════════════════════════════════════════

    given("cart has items — checking cartItemCount") {

        `when`("cart has item1 qty 2 and item2 qty 1") {
            then("cartItemCount should be 3 total") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 2),
                    testCartItem(CART_UUID_2, muttonBiryani, qty = 1),
                )
                createViewModel().uiState.value.cartItemCount shouldBe CART_COUNT_3
            }
        }

        `when`("cart is empty") {
            then("cartItemCount should be 0") {
                createViewModel().uiState.value.cartItemCount shouldBe 0
            }
        }

        `when`("cart has 3 single qty items") {
            then("cartItemCount should be 3") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 1),
                    testCartItem(CART_UUID_2, muttonBiryani, qty = 1),
                    testCartItem("cart_uuid_3", chicken65, qty = 1),
                )
                createViewModel().uiState.value.cartItemCount shouldBe CART_COUNT_3
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Cart Total
    // ══════════════════════════════════════════════════════════

    given("cart has items for total calculation") {

        `when`("1 item below free delivery threshold") {
            then("cartTotal equals subtotal plus delivery plus taxes") {
                cartItemsFlow.value = listOf(
                    testCartItem(
                        CART_UUID_1,
                        testMenuItem("m_cheap", price = PRICE_50),
                        qty = 1
                    ) // ✅
                )
                val vm = createViewModel()
                val subtotal = PRICE_50
                val delivery = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val taxes = subtotal * AppBusinessRules.GST_RATE
                vm.uiState.value.cartTotal shouldBe subtotal + delivery + taxes
            }
        }

        `when`("1 item above free delivery threshold") {
            then("cartTotal equals subtotal plus taxes only") {
                val price = AppBusinessRules.FREE_DELIVERY_ABOVE + PRICE_100
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, testMenuItem("m_exp", price = price), qty = 1)
                )
                val vm = createViewModel()
                val taxes = price * AppBusinessRules.GST_RATE
                vm.uiState.value.cartTotal shouldBe price + 0.0 + taxes
            }
        }

        `when`("cart is empty — total check") {
            then("cartTotal should be 0.0") {
                createViewModel().uiState.value.cartTotal shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — CartBreakdown StateFlow
    // ══════════════════════════════════════════════════════════

    given("cart has items — checking cartBreakdown fields") {

        `when`("1 item at price 50 — all fields checked") {
            then("subtotal deliveryFee taxes total all correct") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, testMenuItem(MENU_ID_1, price = PRICE_50), qty = 1)
                )
                val vm = createViewModel()
                val expectedDel = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val expectedTaxes = PRICE_50 * AppBusinessRules.GST_RATE
                val expectedTotal = PRICE_50 + expectedDel + expectedTaxes

                vm.cartBreakdown.value.subtotal shouldBe PRICE_50
                vm.cartBreakdown.value.deliveryFee shouldBe expectedDel
                vm.cartBreakdown.value.taxes shouldBe expectedTaxes
                vm.cartBreakdown.value.total shouldBe expectedTotal
            }
        }

        `when`("cart empty — breakdown zeros check") {
            then("all cartBreakdown fields should be 0.0") {
                val vm = createViewModel()
                vm.cartBreakdown.value.subtotal shouldBe 0.0
                vm.cartBreakdown.value.deliveryFee shouldBe 0.0
                vm.cartBreakdown.value.taxes shouldBe 0.0
                vm.cartBreakdown.value.total shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Quick Add to Cart
    // ══════════════════════════════════════════════════════════

    given("user taps ADD on recommended item") {

        `when`("quickAddToCart called with valid menu item") {
            then("AddToCartUseCase called once with correct item") {
                val itemSlot = slot<MenuItem>()
                coEvery { addToCartUseCase(capture(itemSlot), any(), any()) } just runs

                val vm = createViewModel()
                vm.quickAddToCart(chickenBiryani)

                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
                itemSlot.captured.id shouldBe MENU_ID_1
                itemSlot.captured.name shouldBe MENU_ITEM_CHICK_BIR
            }
        }

        `when`("quickAddToCart completes successfully") {
            then("ItemAdded event emitted with Chicken Biryani name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.quickAddToCart(chickenBiryani)
                    awaitItem() shouldBe RestaurantViewModel.RestaurantEvent
                        .ItemAdded(MENU_ITEM_CHICK_BIR)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("quickAddToCart with Mutton Biryani succeeds") {
            then("ItemAdded emitted with Mutton Biryani name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.quickAddToCart(muttonBiryani)
                    awaitItem() shouldBe RestaurantViewModel.RestaurantEvent
                        .ItemAdded(MENU_ITEM_MUTTON_BIR)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                coEvery { addToCartUseCase(any(), any(), any()) } throws
                        Exception(ERR_COULD_NOT_ADD_CART)

                val vm = createViewModel()
                vm.events.test {
                    vm.quickAddToCart(chickenBiryani)
                    awaitItem() shouldBe RestaurantViewModel.RestaurantEvent
                        .ShowError(ERR_COULD_NOT_ADD_CART)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — onIncrementItem
    // ══════════════════════════════════════════════════════════

    given("user taps + on menu item row") {

        `when`("item is NOT in cart — use case called check") {
            then("AddToCartUseCase called once") {
                val vm = createViewModel()
                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
            }
        }

        `when`("item is NOT in cart — event check") {
            then("ItemAdded event emitted on first add") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onIncrementItem(chickenBiryani)
                    val event = awaitItem()
                    assert(event is RestaurantViewModel.RestaurantEvent.ItemAdded) {
                        "Expected ItemAdded but got $event"
                    }
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("item already in cart — quantities keyed by menuItem.id") {
            then("updateQuantity called NOT addToCartUseCase — prevents duplicate rows") {
                cartItemsFlow.value = listOf(
                    testCartItem(id = CART_UUID_1, menuItem = chickenBiryani, qty = 1)
                )

                val vm = createViewModel()

                vm.quantities.value[MENU_ID_1] shouldBe 1
                vm.quantities.value[CART_UUID_1] shouldBe null

                vm.onIncrementItem(chickenBiryani)

                coVerify { cartRepository.updateQuantity(CART_UUID_1, 2) }
                coVerify(exactly = 0) { addToCartUseCase(any(), any(), any()) }
            }
        }

        `when`("item already in cart with qty 1") {
            then("updateQuantity called with qty 2") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_ID_STR_1, chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.onIncrementItem(chickenBiryani)

                coVerify { cartRepository.updateQuantity(CART_ID_STR_1, 2) }
            }
        }

        `when`("item in cart with qty 3") {
            then("updateQuantity called with qty 4") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_ID_STR_1, chickenBiryani, qty = 3)
                )

                val vm = createViewModel()
                vm.onIncrementItem(chickenBiryani)

                coVerify { cartRepository.updateQuantity(any(), 4) }
            }
        }

        `when`("item is at MAX_ITEM_QUANTITY") {
            then("quantity capped at MAX not exceeded") {
                cartItemsFlow.value = listOf(
                    testCartItem(
                        CART_ID_STR_1, chickenBiryani,
                        qty = AppBusinessRules.MAX_ITEM_QUANTITY
                    )
                )

                val vm = createViewModel()
                vm.onIncrementItem(chickenBiryani)

                coVerify {
                    cartRepository.updateQuantity(any(), AppBusinessRules.MAX_ITEM_QUANTITY)
                }
            }
        }

        `when`("user taps + four times on same item") {
            then("addToCartUseCase called only once — subsequent taps update quantity") {
                val vm = createViewModel()

                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }

                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 1)
                )

                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
                coVerify { cartRepository.updateQuantity(CART_UUID_1, 2) }

                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 2)
                )

                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
                coVerify { cartRepository.updateQuantity(CART_UUID_1, 3) }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — onDecrementItem
    // ══════════════════════════════════════════════════════════

    given("user taps - on menu item row") {

        `when`("item in cart with qty 2 — uses CartItem UUID for updateQuantity") {
            then("updateQuantity called with CartItem UUID not menuItem.id") {
                cartItemsFlow.value = listOf(
                    testCartItem(id = CART_UUID_1, menuItem = chickenBiryani, qty = 2)
                )

                val vm = createViewModel()
                vm.onDecrementItem(MENU_ID_1)

                coVerify { cartRepository.updateQuantity(CART_UUID_1, 1) }
            }
        }

        `when`("item in cart with qty 2") {
            then("updateQuantity called with qty 1") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_ID_STR_1, chickenBiryani, qty = 2)
                )

                val vm = createViewModel()
                vm.onDecrementItem(MENU_ID_1)

                coVerify { cartRepository.updateQuantity(CART_ID_STR_1, 1) }
            }
        }

        `when`("item in cart with qty 1") {
            then("removeItem called with CartItem UUID — item fully removed") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.onDecrementItem(MENU_ID_1)

                coVerify(exactly = 1) { cartRepository.removeItem(CART_UUID_1) }
                coVerify(exactly = 0) { cartRepository.updateQuantity(any(), any()) }
            }
        }

        `when`("itemId not in cart at all") {
            then("nothing called no crash — guard works") {
                val vm = createViewModel()
                vm.onDecrementItem("nonexistent_menu_id")

                coVerify(exactly = 0) { cartRepository.removeItem(any()) }
                coVerify(exactly = 0) { cartRepository.updateQuantity(any(), any()) }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Per-item Quantities StateFlow
    // ══════════════════════════════════════════════════════════

    given("cart has specific quantities per item") {

        `when`("cart has m1 qty 2 and m2 qty 1") {
            then("quantities keyed by menuItem.id not cartItem.id") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 2),
                    testCartItem(CART_UUID_2, muttonBiryani, qty = 1),
                )
                val vm = createViewModel()

                vm.quantities.value[MENU_ID_1] shouldBe 2          // menuItem.id
                vm.quantities.value[MENU_ID_2] shouldBe 1          // menuItem.id
                vm.quantities.value[CART_UUID_1] shouldBe null        // UUID must NOT be key
            }
        }

        `when`("cart is empty — quantities check") {
            then("quantities map should be empty") {
                createViewModel().quantities.value.isEmpty() shouldBe true
            }
        }

        `when`("item removed from cart via decrement") {
            then("quantities map no longer contains that menuItem.id") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.quantities.value.containsKey(MENU_ID_1) shouldBe true

                vm.onDecrementItem(MENU_ID_1)

                vm.quantities.value.containsKey(MENU_ID_1) shouldBe false
            }
        }

        `when`("quantity increments from 1 to 2") {
            then("quantities map updates to 2 for that menuItem.id") {
                cartItemsFlow.value = listOf(
                    testCartItem(CART_UUID_1, chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.quantities.value[MENU_ID_1] shouldBe 1

                vm.onIncrementItem(chickenBiryani)

                vm.quantities.value[MENU_ID_1] shouldBe 2
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Navigation Events
    // ══════════════════════════════════════════════════════════

    given("user is on RestaurantScreen") {

        `when`("user taps back button") {
            then("NavigateBack event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onBackPressed()
                    awaitItem() shouldBe RestaurantViewModel.RestaurantEvent.NavigateBack
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps cart bar") {
            then("NavigateToCart event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onCartBarTapped()
                    awaitItem() shouldBe RestaurantViewModel.RestaurantEvent.NavigateToCart
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps menu item with id m1") {
            then("NavigateToProduct emitted with itemId m1") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onMenuItemTapped(MENU_ID_1)
                    awaitItem() shouldBe RestaurantViewModel.RestaurantEvent
                        .NavigateToProduct(MENU_ID_1)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Tab Selection
    // ══════════════════════════════════════════════════════════

    given("default selected tab is MENU") {

        `when`("ViewModel first created") {
            then("selectedTab should default to MENU") {
                createViewModel().uiState.value.selectedTab shouldBe
                        RestaurantViewModel.MenuTab.MENU
            }
        }

        `when`("user taps REVIEWS tab") {
            then("selectedTab changes to REVIEWS") {
                val vm = createViewModel()
                vm.onTabSelected(RestaurantViewModel.MenuTab.REVIEWS)
                vm.uiState.value.selectedTab shouldBe RestaurantViewModel.MenuTab.REVIEWS
            }
        }

        `when`("user switches back to MENU after REVIEWS") {
            then("selectedTab goes back to MENU") {
                val vm = createViewModel()
                vm.onTabSelected(RestaurantViewModel.MenuTab.REVIEWS)
                vm.onTabSelected(RestaurantViewModel.MenuTab.MENU)
                vm.uiState.value.selectedTab shouldBe RestaurantViewModel.MenuTab.MENU
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 11 — Scroll to Category
    // ══════════════════════════════════════════════════════════

    given("restaurant has multiple menu categories") {

        `when`("user taps Starters in category footer") {
            then("scrollToCategory emits Starters") {
                val vm = createViewModel()
                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped(CATEGORY_STARTERS)
                    awaitItem() shouldBe CATEGORY_STARTERS
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps Biryani in category footer") {
            then("scrollToCategory emits Biryani") {
                val vm = createViewModel()
                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped(CATEGORY_BIRYANI_CAT)
                    awaitItem() shouldBe CATEGORY_BIRYANI_CAT
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 12 — Retry on Error
    // ══════════════════════════════════════════════════════════

    given("restaurant screen shows error on first load") {

        `when`("user taps retry after connection restored") {
            then("data reloads and restaurant name shown") {
                every { restaurantRepository.getRestaurantDetail(any()) } returnsMany listOf(
                    flowOf(Result.failure(Exception(ERR_NO_INTERNET_HOME))),
                    flowOf(Result.success(testRestaurant())),
                )

                val vm = createViewModel()
                vm.uiState.value.error shouldBe ERR_COULD_NOT_LOAD_REST
                vm.uiState.value.restaurant shouldBe null

                vm.retry()

                vm.uiState.value.restaurant?.name shouldBe RESTAURANT_MEGHANA
                vm.uiState.value.error shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 13 — getCategoryNames
    // ══════════════════════════════════════════════════════════

    given("menu is fully loaded with Biryani and Starters") {

        `when`("getCategoryNames called") {
            then("returns list containing Biryani and Starters") {
                val vm = createViewModel()
                val names = vm.getCategoryNames()
                names.size shouldBe MENU_CATEGORY_COUNT
                names.contains(CATEGORY_BIRYANI_CAT) shouldBe true
                names.contains(CATEGORY_STARTERS) shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 14 — Different Restaurant IDs
    // ══════════════════════════════════════════════════════════

    given("RestaurantScreen opened with different restaurant IDs") {

        `when`("opened with restaurantId r1") {
            then("SavedStateHandle correctly passes r1") {
                createViewModel(restaurantId = RESTAURANT_ID_1).restaurantId shouldBe RESTAURANT_ID_1
            }
        }

        `when`("opened with restaurantId r2") {
            then("SavedStateHandle correctly passes r2") {
                createViewModel(restaurantId = RESTAURANT_ID_2).restaurantId shouldBe RESTAURANT_ID_2
            }
        }
    }
})

private fun testRestaurant() = Restaurant(
    id = RESTAURANT_ID_1,
    name = RESTAURANT_MEGHANA,
    imageUrl = "",
    thumbUrl = "",
    rating = RESTAURANT_RATING_45,
    ratingText = "Excellent",
    ratingColor = "#3F7E00",
    totalVotes = RESTAURANT_VOTES_5000,
    avgDeliveryTime = 30,
    deliveryFee = 30.0,
    avgCostForTwo = RESTAURANT_COST_500,
    minOrder = RESTAURANT_MIN_ORDER_100,
    cuisines = listOf(CATEGORY_BIRYANI_CAT, "South Indian"),
    address = RESTAURANT_ADDRESS_KORA,
    locality = LOC_KORAMANGALA,
    distanceKm = 0.0,
    hasDelivery = true,
    isOpen = true,
    offers = listOf(OFFER_50_OFF),
    phoneNumber = "",
    openingHours = "",
    highlights = emptyList(),
    knownFor = "",
)

private fun testMenuItem(
    id: String = MENU_ID_1,
    name: String = "Test Item",
    price: Double = PRICE_100,
    category: String = CATEGORY_BIRYANI_CAT,
    isVeg: Boolean = false,
    isRecommended: Boolean = false,
    isBestseller: Boolean = false,
) = MenuItem(
    id = id,
    restaurantId = RESTAURANT_ID_1,
    name = name,
    description = "Delicious $name",
    price = price,
    imageUrl = "",
    category = category,
    isVeg = isVeg,
    isRecommended = isRecommended,
    isBestseller = isBestseller,
    isAvailable = true,
    customisations = emptyList(),
)

private fun testCartItem(
    id: String = CART_UUID_1,
    menuItem: MenuItem = testMenuItem(),
    qty: Int = 1,
) = CartItem(
    id = id,
    menuItem = menuItem,
    quantity = qty,
    selectedCustomisations = emptyList(),
)

private fun testMenuByCategory() = mapOf(
    CATEGORY_BIRYANI_CAT to listOf(

        testMenuItem(
            MENU_ID_1,
            MENU_ITEM_CHICK_BIR,
            PRICE_249,
            isRecommended = true,
            isBestseller = true
        ),
        testMenuItem(MENU_ID_2, MENU_ITEM_MUTTON_BIR, PRICE_349),
    ),
    CATEGORY_STARTERS to listOf(

        testMenuItem(
            MENU_ID_3,
            MENU_ITEM_CHICK_65,
            PRICE_199,
            category = CATEGORY_STARTERS,
            isRecommended = true
        ),
        testMenuItem(
            MENU_ID_4,
            MENU_ITEM_PANEER_TIKKA,
            PRICE_179,
            category = CATEGORY_STARTERS,
            isVeg = true
        ),
    ),
)