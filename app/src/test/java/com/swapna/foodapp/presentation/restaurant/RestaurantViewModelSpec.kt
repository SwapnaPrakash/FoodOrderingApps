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

    // ── Controllable cart flow ────────────────────────────────
    // WHY MutableStateFlow not flowOf()?
    // onIncrementItem/onDecrementItem mutate cart
    // answers block updates cartItemsFlow
    // → quantities StateFlow re-emits automatically
    val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())

    // ── Test data ─────────────────────────────────────────────
    val chickenBiryani =
        testMenuItem("m1", "Chicken Biryani", 249.0, isRecommended = true, isBestseller = true)
    val muttonBiryani = testMenuItem("m2", "Mutton Biryani", 349.0)
    val chicken65 =
        testMenuItem("m3", "Chicken 65", 199.0, isRecommended = true, category = "Starters")
    val paneerTikka = testMenuItem("m4", "Paneer Tikka", 179.0, isVeg = true, category = "Starters")

    fun createViewModel(restaurantId: String = "r1"): RestaurantViewModel {
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

        // Default stubs
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
                    else if (subtotal > 0) AppBusinessRules.DEFAULT_DELIVERY_FEE
                    else 0.0
                    val taxes = subtotal * AppBusinessRules.GST_RATE
                    com.swapna.foodapp.domain.model.CartPriceBreakdown(
                        subtotal = subtotal,
                        deliveryFee = delivery,
                        taxes = taxes,
                        total = subtotal + delivery + taxes,
                    )
                }
        coEvery { addToCartUseCase(any(), any(), any()) } just runs

        // WHY answers not just runs?
        // updateQuantity/removeItem must mutate cartItemsFlow
        // so quantities StateFlow re-emits with updated values
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
                    .restaurant?.name shouldBe "Meghana Foods"
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
                menu.size shouldBe 2
                menu.containsKey("Biryani") shouldBe true
                menu.containsKey("Starters") shouldBe true
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
                        flowOf(Result.failure(Exception("No internet")))

                val vm = createViewModel()
                vm.uiState.value.error shouldBe "Could not load restaurant"
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
                    testCartItem("c1", chickenBiryani, qty = 2),
                    testCartItem("c2", muttonBiryani, qty = 1),
                )
                createViewModel().uiState.value.cartItemCount shouldBe 3
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
                    testCartItem("c1", chickenBiryani, qty = 1),
                    testCartItem("c2", muttonBiryani, qty = 1),
                    testCartItem("c3", chicken65, qty = 1),
                )
                createViewModel().uiState.value.cartItemCount shouldBe 3
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
                    testCartItem("c1", testMenuItem("m_cheap", price = 50.0), qty = 1)
                )
                val vm = createViewModel()
                val subtotal = 50.0
                val delivery = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val taxes = subtotal * AppBusinessRules.GST_RATE
                vm.uiState.value.cartTotal shouldBe subtotal + delivery + taxes
            }
        }

        `when`("1 item above free delivery threshold") {
            then("cartTotal equals subtotal plus taxes only") {
                val price = AppBusinessRules.FREE_DELIVERY_ABOVE + 100.0
                cartItemsFlow.value = listOf(
                    testCartItem("c1", testMenuItem("m_exp", price = price), qty = 1)
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
                val price = 50.0
                cartItemsFlow.value = listOf(
                    testCartItem("c1", testMenuItem("m1", price = price), qty = 1)
                )
                val vm = createViewModel()
                val expectedDelivery = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val expectedTaxes = price * AppBusinessRules.GST_RATE
                val expectedTotal = price + expectedDelivery + expectedTaxes

                vm.cartBreakdown.value.subtotal shouldBe price
                vm.cartBreakdown.value.deliveryFee shouldBe expectedDelivery
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
                itemSlot.captured.id shouldBe "m1"
                itemSlot.captured.name shouldBe "Chicken Biryani"
            }
        }

        `when`("quickAddToCart completes successfully") {
            then("ItemAdded event emitted with Chicken Biryani name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.quickAddToCart(chickenBiryani)
                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.ItemAdded("Chicken Biryani")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("quickAddToCart with Mutton Biryani succeeds") {
            then("ItemAdded emitted with Mutton Biryani name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.quickAddToCart(muttonBiryani)
                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.ItemAdded("Mutton Biryani")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                coEvery { addToCartUseCase(any(), any(), any()) } throws
                        Exception("Could not add to cart")

                val vm = createViewModel()
                vm.events.test {
                    vm.quickAddToCart(chickenBiryani)
                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.ShowError("Could not add to cart")
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

        // ── THE KEY BUG FIX TEST ──────────────────────────────
        // WHY this test is critical?
        // Old bug: _quantities used cartItem.id (UUID) as key
        //          onIncrementItem looked up menuItem.id → miss → always 0
        //          → called addToCartUseCase every time → 4 separate rows
        // Fix: _quantities now uses menuItem.id as key → hit → updateQuantity called
        `when`("item already in cart — quantities keyed by menuItem.id") {
            then("updateQuantity called NOT addToCartUseCase — prevents duplicate rows") {
                // Seed cart with CartItem where menuItem.id = "m1"
                // cartItem.id = "cart_uuid_1" (different from menuItem.id)
                cartItemsFlow.value = listOf(
                    testCartItem(id = "cart_uuid_1", menuItem = chickenBiryani, qty = 1)
                )

                val vm = createViewModel()

                // quantities should be keyed by menuItem.id "m1" not "cart_uuid_1"
                vm.quantities.value["m1"] shouldBe 1
                vm.quantities.value["cart_uuid_1"] shouldBe null // UUID key must NOT exist

                vm.onIncrementItem(chickenBiryani)

                // updateQuantity called with CartItem UUID (cart_uuid_1) not menuItem.id
                coVerify { cartRepository.updateQuantity("cart_uuid_1", 2) }
                // addToCartUseCase must NOT be called — item already in cart
                coVerify(exactly = 0) { addToCartUseCase(any(), any(), any()) }
            }
        }

        `when`("item already in cart with qty 1") {
            then("updateQuantity called with qty 2") {
                cartItemsFlow.value = listOf(
                    testCartItem("cart_id_1", chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.onIncrementItem(chickenBiryani)

                coVerify { cartRepository.updateQuantity("cart_id_1", 2) }
            }
        }

        `when`("item in cart with qty 3") {
            then("updateQuantity called with qty 4") {
                cartItemsFlow.value = listOf(
                    testCartItem("cart_id_1", chickenBiryani, qty = 3)
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
                        "cart_id_1", chickenBiryani,
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

        // ── Regression test for original bug ──────────────────
        // Tapping + 4 times must produce 1 cart row with qty=4
        // not 4 separate cart rows
        `when`("user taps + four times on same item") {
            then("addToCartUseCase called only once — subsequent taps update quantity") {
                val vm = createViewModel()

                // First tap — item not in cart → addToCartUseCase
                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }

                // Simulate cart updated after first add
                cartItemsFlow.value = listOf(
                    testCartItem("cart_uuid_1", chickenBiryani, qty = 1)
                )

                // Second tap — item now in cart → updateQuantity
                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) } // still 1
                coVerify { cartRepository.updateQuantity("cart_uuid_1", 2) }

                // Update cart to qty=2
                cartItemsFlow.value = listOf(
                    testCartItem("cart_uuid_1", chickenBiryani, qty = 2)
                )

                // Third tap
                vm.onIncrementItem(chickenBiryani)
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) } // still 1
                coVerify { cartRepository.updateQuantity("cart_uuid_1", 3) }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — onDecrementItem
    // ══════════════════════════════════════════════════════════

    given("user taps - on menu item row") {

        // ── THE KEY BUG FIX TEST ──────────────────────────────
        // WHY critical?
        // onDecrementItem receives menuItem.id (e.g. "m1")
        // cartRepository.removeItem/updateQuantity need CartItem UUID
        // Must look up CartItem by menuItem.id to get correct UUID
        `when`("item in cart with qty 2 — uses CartItem UUID for updateQuantity") {
            then("updateQuantity called with CartItem UUID not menuItem.id") {
                cartItemsFlow.value = listOf(
                    testCartItem(id = "cart_uuid_1", menuItem = chickenBiryani, qty = 2)
                )

                val vm = createViewModel()
                vm.onDecrementItem("m1") // passes menuItem.id

                // Must use CartItem UUID "cart_uuid_1" not "m1"
                coVerify { cartRepository.updateQuantity("cart_uuid_1", 1) }
            }
        }

        `when`("item in cart with qty 2") {
            then("updateQuantity called with qty 1") {
                cartItemsFlow.value = listOf(
                    testCartItem("cart_id_1", chickenBiryani, qty = 2)
                )

                val vm = createViewModel()
                vm.onDecrementItem("m1") // menuItem.id

                coVerify { cartRepository.updateQuantity("cart_id_1", 1) }
            }
        }

        `when`("item in cart with qty 1") {
            then("removeItem called with CartItem UUID — item fully removed") {
                cartItemsFlow.value = listOf(
                    testCartItem("cart_uuid_1", chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.onDecrementItem("m1") // menuItem.id

                // removeItem must use CartItem UUID not menuItem.id
                coVerify(exactly = 1) { cartRepository.removeItem("cart_uuid_1") }
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
                    testCartItem("cart_uuid_1", chickenBiryani, qty = 2),
                    testCartItem("cart_uuid_2", muttonBiryani, qty = 1),
                )
                val vm = createViewModel()

                // WHY check menuItem.id keys?
                // After bug fix: quantities uses menuItem.id ("m1", "m2")
                // Before bug: it used cartItem.id ("cart_uuid_1") — wrong
                vm.quantities.value["m1"] shouldBe 2   // menuItem.id
                vm.quantities.value["m2"] shouldBe 1   // menuItem.id
                vm.quantities.value["cart_uuid_1"] shouldBe null // UUID must NOT be key
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
                    testCartItem("cart_uuid_1", chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.quantities.value.containsKey("m1") shouldBe true

                // removeItem answers block filters cartItemsFlow
                vm.onDecrementItem("m1")

                vm.quantities.value.containsKey("m1") shouldBe false
            }
        }

        `when`("quantity increments from 1 to 2") {
            then("quantities map updates to 2 for that menuItem.id") {
                cartItemsFlow.value = listOf(
                    testCartItem("cart_uuid_1", chickenBiryani, qty = 1)
                )

                val vm = createViewModel()
                vm.quantities.value["m1"] shouldBe 1

                vm.onIncrementItem(chickenBiryani)

                vm.quantities.value["m1"] shouldBe 2
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
                    vm.onMenuItemTapped("m1")
                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.NavigateToProduct("m1")
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
                    vm.onCategoryFooterTapped("Starters")
                    awaitItem() shouldBe "Starters"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps Biryani in category footer") {
            then("scrollToCategory emits Biryani") {
                val vm = createViewModel()
                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped("Biryani")
                    awaitItem() shouldBe "Biryani"
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
                    flowOf(Result.failure(Exception("No internet"))),
                    flowOf(Result.success(testRestaurant())),
                )

                val vm = createViewModel()
                vm.uiState.value.error shouldBe "Could not load restaurant"
                vm.uiState.value.restaurant shouldBe null

                vm.retry()

                vm.uiState.value.restaurant?.name shouldBe "Meghana Foods"
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
                names.size shouldBe 2
                names.contains("Biryani") shouldBe true
                names.contains("Starters") shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 14 — Different Restaurant IDs
    // ══════════════════════════════════════════════════════════

    given("RestaurantScreen opened with different restaurant IDs") {

        `when`("opened with restaurantId r1") {
            then("SavedStateHandle correctly passes r1") {
                createViewModel(restaurantId = "r1").restaurantId shouldBe "r1"
            }
        }

        `when`("opened with restaurantId r2") {
            then("SavedStateHandle correctly passes r2") {
                createViewModel(restaurantId = "r2").restaurantId shouldBe "r2"
            }
        }
    }
})

// ── Local test data helpers ───────────────────────────────────

private fun testRestaurant() = Restaurant(
    id = "r1",
    name = "Meghana Foods",
    imageUrl = "",
    thumbUrl = "",
    rating = 4.5,
    ratingText = "Excellent",
    ratingColor = "#3F7E00",
    totalVotes = 5000,
    avgDeliveryTime = 30,
    deliveryFee = 30.0,
    avgCostForTwo = 500,
    minOrder = 100,
    cuisines = listOf("Biryani", "South Indian"),
    address = "Koramangala, Bengaluru",
    locality = "Koramangala",
    distanceKm = 0.0,
    hasDelivery = true,
    isOpen = true,
    offers = listOf("50% off"),
    phoneNumber = "",
    openingHours = "",
    highlights = emptyList(),
    knownFor = "",
)

private fun testMenuItem(
    id: String = "m1",
    name: String = "Test Item",
    price: Double = 100.0,
    category: String = "Biryani",
    isVeg: Boolean = false,
    isRecommended: Boolean = false,
    isBestseller: Boolean = false,
) = MenuItem(
    id = id,
    restaurantId = "r1",
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

// WHY testCartItem has separate id and menuItem params?
// CartItem.id = UUID (e.g. "cart_uuid_1") — Room primary key
// CartItem.menuItem.id = MenuItem id (e.g. "m1") — what UI uses
// These are DIFFERENT — the bug was conflating them
// Tests must use different values to catch the bug
private fun testCartItem(
    id: String = "cart_uuid_1",
    menuItem: MenuItem = testMenuItem(),
    qty: Int = 1,
) = CartItem(
    id = id,
    menuItem = menuItem,
    quantity = qty,
    selectedCustomisations = emptyList(),
)

private fun testMenuByCategory() = mapOf(
    "Biryani" to listOf(
        testMenuItem("m1", "Chicken Biryani", 249.0, isRecommended = true, isBestseller = true),
        testMenuItem("m2", "Mutton Biryani", 349.0),
    ),
    "Starters" to listOf(
        testMenuItem("m3", "Chicken 65", 199.0, category = "Starters", isRecommended = true),
        testMenuItem("m4", "Paneer Tikka", 179.0, category = "Starters", isVeg = true),
    ),
)