package com.swapna.foodapp.presentation.cart

// ── Kotest imports ────────────────────────────────────────────
// BehaviorSpec = Given/When/Then style
// Perfect for ViewModel tests because ViewModels
// respond to USER actions (Given situation, When action, Then result)
import io.kotest.core.spec.style.BehaviorSpec

// shouldBe = assertion: actual shouldBe expected
// If they don't match → test fails with clear message
import io.kotest.matchers.shouldBe

// ── Coroutine test imports ────────────────────────────────────
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

// UnconfinedTestDispatcher = runs coroutines immediately
// without needing runBlocking or advance calls
// Perfect for ViewModel tests where we want instant results
import kotlinx.coroutines.test.UnconfinedTestDispatcher

// resetMain/setMain = replace real Main dispatcher with test one
// Required because ViewModel uses Dispatchers.Main internally
// Without this → "Module with Main dispatcher not found" error
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// first() = collect first emission from Flow then cancel
// We use this to get the first event from SharedFlow
import kotlinx.coroutines.flow.first

// ── App imports ───────────────────────────────────────────────
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.fakes.FakeCartRepository
import com.swapna.foodapp.utils.AppBusinessRules

/*@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelSpec : BehaviorSpec({

    // ── Test dispatcher ───────────────────────────────────────
    // UnconfinedTestDispatcher = runs coroutines synchronously
    // Means: when we call viewModel.onIncrementItem()
    //   → coroutine launches
    //   → runs immediately (no delay)
    //   → state updates before next assertion
    // Without this: state might not update before we assert
    val dispatcher = UnconfinedTestDispatcher()

    // ── Test dependencies ─────────────────────────────────────
    // lateinit = declared here, initialized in beforeEach
    // Each test gets FRESH instances
    // Prevents state leaking between tests
    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var viewModel:    CartViewModel
    // ── beforeEach ────────────────────────────────────────────
    // Runs before EVERY single test
    // Creates fresh fakeCartRepo + viewModel per test
    // WHY fresh per test?
    //   Test A adds item → marks addItemCalled = true
    //   Test B checks addItemCalled → would be wrong
    //   Fresh instance per test = no bleed
    beforeEach {
        // Set test dispatcher as Main
        // CartViewModel uses viewModelScope which uses Main
        Dispatchers.setMain(dispatcher)

        // Fresh fake repository per test
        fakeCartRepo = FakeCartRepository()

        // Fresh ViewModel per test
        // CartViewModel injects CartRepository via Hilt
        // In tests we pass fake directly — no Hilt needed
        viewModel = CartViewModel(fakeCartRepo)
    }

    // ── afterEach ─────────────────────────────────────────────
    // Runs after EVERY test
    // Reset Main dispatcher back to real one
    // Without this → other test classes might break
    afterEach {
        Dispatchers.resetMain()
    }

    // ── Helper functions ──────────────────────────────────────
    // fakeMenuItem() = creates test MenuItem
    // WHY helper? Avoids repeating same boilerplate
    // in every test — just call fakeMenuItem("m1", 249.0)
    fun fakeMenuItem(
        id:    String = "m1",
        name:  String = "Chicken Biryani",
        price: Double = 249.0,
    ) = MenuItem(
        id           = id,
        restaurantId = "r1",
        name         = name,
        description  = "Delicious $name",
        price        = price,
        imageUrl     = "",
        category     = "Biryani",
        isVeg        = false,
        isRecommended = false,
        isBestseller  = false,
        isAvailable   = true,
    )

    // fakeCartItem() = creates test CartItem
    // CartItem wraps MenuItem + quantity
    fun fakeCartItem(
        id:       String   = "c1",
        menuItem: MenuItem = fakeMenuItem(),
        quantity: Int      = 1,
    ) = CartItem(
        id       = id,
        menuItem = menuItem,
        quantity = quantity,
    )

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial State
    // Tests what CartViewModel looks like when it first loads
    // ══════════════════════════════════════════════════════════

    given("CartScreen is opened") {
        // given = the SITUATION the user is in
        // "User opened cart screen"

        `when`("cart repository has no items") {
            // when = the STATE of the system
            // Backtick needed because 'when' is Kotlin keyword

            then("isEmpty should be true") {
                // then = what user SEES as a result

                // WHY test isEmpty?
                // CartScreen shows EmptyCartView when isEmpty = true
                // If this is wrong → empty cart shows items (bug)

                viewModel.uiState.value.isEmpty shouldBe true
            }
        }

        `when`("cart repository has no items") {
            then("isLoading should be false after load") {

                // WHY test isLoading?
                // CartScreen shows CircularProgressIndicator when
                // isLoading = true
                // After repo emits → loading must stop
                // If still true → spinner shows forever (bug)

                viewModel.uiState.value.isLoading shouldBe false
            }
        }

        `when`("cart has 2 items") {
            then("items list should contain both items") {

                // Seed cart before creating ViewModel
                // seedCart() = directly set items in FakeCartRepository
                // WHY seed before VM creation?
                // VM.init calls observeCart() immediately
                // If we seed after, the first emission is already gone
                fakeCartRepo.seedCart(
                    fakeCartItem("c1", fakeMenuItem("m1"), 1),
                    fakeCartItem("c2", fakeMenuItem("m2", "Mutton Biryani", 349.0), 1),
                )

                // Recreate VM AFTER seeding so it sees the items
                viewModel = CartViewModel(fakeCartRepo)

                // Verify items loaded
                viewModel.uiState.value.items.size shouldBe 2
            }
        }

        `when`("cart has 1 item") {
            then("isEmpty should be false") {

                fakeCartRepo.seedCart(fakeCartItem())
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.isEmpty shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Price Breakdown
    // Tests CartPriceBreakdown computation
    // This is the CORE business logic of Cart
    // ══════════════════════════════════════════════════════════

    given("cart has items") {

        `when`("cart has 1 item at ₹249 qty 1") {
            then("subtotal should be 249.0") {

                // WHY test subtotal?
                // subtotal = sum of all CartItem.totalPrice
                // CartItem.totalPrice = (price + extras) × qty
                // = (249.0 + 0) × 1 = 249.0

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 249.0),
                        quantity = 1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.breakdown
                    .subtotal shouldBe 249.0
            }
        }

        `when`("cart has 1 item at ₹249 qty 2") {
            then("subtotal should be 498.0") {

                // qty 2 → totalPrice = 249.0 × 2 = 498.0
                // Tests that quantity multiplier works

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 249.0),
                        quantity = 2,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.breakdown
                    .subtotal shouldBe 498.0
            }
        }

        `when`("subtotal is ₹249 — below free delivery threshold") {
            then("delivery fee should be ₹${AppBusinessRules.DEFAULT_DELIVERY_FEE}") {

                // WHY test delivery?
                // AppBusinessRules.FREE_DELIVERY_ABOVE = ₹500
                // ₹249 < ₹500 → delivery = ₹30
                // If wrong → user sees wrong delivery charge (serious bug)

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 249.0),
                        quantity = 1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.breakdown
                    .deliveryFee shouldBe
                        AppBusinessRules.DEFAULT_DELIVERY_FEE
            }
        }

        `when`("subtotal is ₹600 — above free delivery threshold") {
            then("delivery fee should be 0.0 — FREE delivery") {

                // ₹600 ≥ ₹500 → delivery = FREE (0.0)
                // Tests the business rule boundary

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 600.0),
                        quantity = 1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.breakdown
                    .deliveryFee shouldBe 0.0
            }
        }

        `when`("subtotal is exactly ₹500 — at threshold") {
            then("delivery fee should be 0.0 — FREE delivery") {

                // Boundary test — exactly at threshold
                // ₹500 = FREE (≥ threshold)
                // Tests edge case that often has bugs

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 500.0),
                        quantity = 1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.breakdown
                    .deliveryFee shouldBe 0.0
            }
        }

        `when`("cart has 1 item at ₹249") {
            then("taxes should be 5% of subtotal") {

                // GST_RATE = 0.05
                // taxes = 249.0 × 0.05 = 12.45

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 249.0),
                        quantity = 1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                val expectedTax =
                    249.0 * AppBusinessRules.GST_RATE

                viewModel.uiState.value.breakdown
                    .taxes shouldBe expectedTax
            }
        }

        `when`("cart has 1 item at ₹249 with delivery") {
            then("total should be subtotal + delivery + taxes") {

                // total = 249.0 + 30.0 + 12.45 = 291.45
                // Tests that total = subtotal + delivery + taxes
                // Most critical number shown to user

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 249.0),
                        quantity = 1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                val subtotal  = 249.0
                val delivery  = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val taxes     = subtotal * AppBusinessRules.GST_RATE
                val expected  = subtotal + delivery + taxes

                viewModel.uiState.value.breakdown
                    .total shouldBe expected
            }
        }

        `when`("cart is empty") {
            then("all breakdown values should be 0.0") {

                // Empty cart → no subtotal, no delivery, no tax, no total
                // Tests initial/empty state of breakdown

                // No seedCart() call → cart stays empty
                viewModel = CartViewModel(fakeCartRepo)

                val breakdown = viewModel.uiState.value.breakdown

                breakdown.subtotal    shouldBe 0.0
                breakdown.deliveryFee shouldBe 0.0
                breakdown.taxes       shouldBe 0.0
                breakdown.total       shouldBe 0.0
            }
        }

        `when`("cart has 2 different items") {
            then("subtotal should be sum of both item totals") {

                // item1 total = 249.0 × 1 = 249.0
                // item2 total = 349.0 × 1 = 349.0
                // subtotal = 249.0 + 349.0 = 598.0

                val item1 = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                val item2 = fakeMenuItem("m2", "Mutton Biryani",  349.0)

                fakeCartRepo.seedCart(
                    fakeCartItem("c1", item1, 1),
                    fakeCartItem("c2", item2, 1),
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value.breakdown
                    .subtotal shouldBe 598.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Increment Item
    // Tests + button behavior on cart items
    // ══════════════════════════════════════════════════════════

    given("user taps + button on cart item") {

        `when`("item has qty 1") {
            then("updateQuantity called with qty 2") {

                // WHY test updateQuantity?
                // CartViewModel must call repository to persist qty
                // If it only updates local state → Room not updated
                // Next app open → qty reverts to old value (bug)

                val item     = fakeMenuItem()
                val cartItem = fakeCartItem("c1", item, 1)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                // Trigger + button
                viewModel.onIncrementItem(cartItem)

                // Verify repository was called correctly
                fakeCartRepo.updateQtyCalled   shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe "c1"
                fakeCartRepo.lastUpdatedQty    shouldBe 2
            }
        }

        `when`("item has qty 3") {
            then("updateQuantity called with qty 4") {

                val item     = fakeMenuItem()
                val cartItem = fakeCartItem("c1", item, 3)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onIncrementItem(cartItem)

                fakeCartRepo.lastUpdatedQty shouldBe 4
            }
        }

        `when`("item is at MAX_ITEM_QUANTITY") {
            then("quantity is capped — not exceeded") {

                // MAX_ITEM_QUANTITY = 10
                // User taps + at qty 10 → stays at 10
                // Without this cap → user could order 999 items

                val item = fakeMenuItem()
                val cartItem = fakeCartItem(
                    "c1",
                    item,
                    AppBusinessRules.MAX_ITEM_QUANTITY,
                )
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onIncrementItem(cartItem)

                // Should stay at MAX, not go to MAX+1
                fakeCartRepo.lastUpdatedQty shouldBe
                        AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Decrement Item
    // Tests - button behavior on cart items
    // ══════════════════════════════════════════════════════════

    given("user taps - button on cart item") {

        `when`("item has qty 2") {
            then("updateQuantity called with qty 1") {

                val item     = fakeMenuItem()
                val cartItem = fakeCartItem("c1", item, 2)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onDecrementItem(cartItem)

                fakeCartRepo.updateQtyCalled   shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe "c1"
                fakeCartRepo.lastUpdatedQty    shouldBe 1
            }
        }

        `when`("item has qty 1") {
            then("removeItem called — item fully removed from cart") {

                // WHY removeItem not updateQuantity(0)?
                // qty 0 = item still in cart list (wrong)
                // removeItem = item completely gone from cart
                // CartScreen should not show qty-0 items

                val item     = fakeMenuItem()
                val cartItem = fakeCartItem("c1", item, 1)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onDecrementItem(cartItem)

                // removeItem must be called, not updateQuantity
                fakeCartRepo.removeItemCalled shouldBe true
                fakeCartRepo.updateQtyCalled  shouldBe false
            }
        }

        `when`("item has qty 1 and is removed") {
            then("ShowSnackbar event emitted with item name") {

                // WHY snackbar on remove?
                // User gets visual feedback that item was removed
                // Same UX pattern as Gmail delete
                // Without this → silent removal feels broken

                val item     = fakeMenuItem(name = "Chicken Biryani")
                val cartItem = fakeCartItem("c1", item, 1)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onDecrementItem(cartItem)

                // Get first event emitted
                val event = viewModel.events.first()

                // Must be ShowSnackbar (not ShowError)
                assert(event is CartViewModel.CartEvent.ShowSnackbar) {
                    "Expected ShowSnackbar but got $event"
                }

                // Must mention the item name
                (event as CartViewModel.CartEvent.ShowSnackbar)
                    .message shouldBe "Chicken Biryani removed"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Remove Item
    // Tests explicit remove (swipe to delete)
    // ══════════════════════════════════════════════════════════

    given("user swipes to delete a cart item") {

        `when`("user removes item from cart") {
            then("removeItem called in repository") {

                val item     = fakeMenuItem()
                val cartItem = fakeCartItem("c1", item, 2)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                // Direct remove — not via decrement
                viewModel.onRemoveItem(cartItem)

                fakeCartRepo.removeItemCalled shouldBe true
            }
        }

        `when`("user removes item") {
            then("ShowSnackbar emitted with removed message") {

                val item     = fakeMenuItem(name = "Mutton Biryani")
                val cartItem = fakeCartItem("c1", item, 1)
                fakeCartRepo.seedCart(cartItem)
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onRemoveItem(cartItem)

                val event = viewModel.events.first()

                assert(event is CartViewModel.CartEvent.ShowSnackbar) {
                    "Expected ShowSnackbar but got $event"
                }

                (event as CartViewModel.CartEvent.ShowSnackbar)
                    .message shouldBe
                        "Mutton Biryani removed from cart"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Place Order
    // Tests Place Order button logic
    // ══════════════════════════════════════════════════════════

    given("user taps Place Order button") {

        `when`("cart has items above minimum order value") {
            then("OrderPlaced event emitted") {

                // WHY test OrderPlaced event?
                // CartScreen listens for this event
                // → navigates to confirmation screen
                // Without it → order button does nothing (bug)

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 300.0),
                        1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onPlaceOrder()

                val event = viewModel.events.first()

                assert(event is CartViewModel.CartEvent.OrderPlaced) {
                    "Expected OrderPlaced but got $event"
                }
            }
        }

        `when`("order is placed successfully") {
            then("cart is cleared after order") {

                // WHY clear cart?
                // After order placed → cart must be empty
                // If not cleared → next order shows old items (bug)

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 300.0),
                        1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onPlaceOrder()

                // clearCart must have been called
                fakeCartRepo.clearCartCalled shouldBe true
            }
        }

        `when`("cart is empty") {
            then("ShowError emitted — cannot place empty order") {

                // User somehow taps Place Order on empty cart
                // Must show error not crash

                // No seedCart → empty cart
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onPlaceOrder()

                val event = viewModel.events.first()

                assert(event is CartViewModel.CartEvent.ShowError) {
                    "Expected ShowError but got $event"
                }

                (event as CartViewModel.CartEvent.ShowError)
                    .message shouldBe "Cart is empty"
            }
        }

        `when`("cart total is below minimum order value") {
            then("ShowError emitted with minimum order message") {

                // AppBusinessRules.MIN_ORDER_VALUE = ₹100
                // Item at ₹50 → total < ₹100 → reject

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem("m1", price = 50.0),
                        1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onPlaceOrder()

                val event = viewModel.events.first()

                assert(event is CartViewModel.CartEvent.ShowError) {
                    "Expected ShowError but got $event"
                }

                (event as CartViewModel.CartEvent.ShowError)
                    .message shouldBe
                        "Minimum order is ₹${
                            AppBusinessRules.MIN_ORDER_VALUE.toInt()
                        }"
            }
        }

        `when`("cart total is exactly at minimum order value") {
            then("order should be placed successfully") {

                // Boundary test — exactly ₹100 should work
                // MIN_ORDER_VALUE = 100.0
                // require(total >= MIN_ORDER_VALUE) → true

                fakeCartRepo.seedCart(
                    fakeCartItem(
                        "c1",
                        fakeMenuItem(
                            "m1",
                            price = AppBusinessRules.MIN_ORDER_VALUE,
                        ),
                        1,
                    )
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.onPlaceOrder()

                val event = viewModel.events.first()

                assert(event is CartViewModel.CartEvent.OrderPlaced) {
                    "Expected OrderPlaced but got $event"
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Navigation Events
    // Tests back button and navigation
    // ══════════════════════════════════════════════════════════

    given("user is on CartScreen") {

        `when`("user taps back button") {
            then("NavigateBack event is emitted") {

                viewModel.onBackPressed()

                val event = viewModel.events.first()

                assert(event is CartViewModel.CartEvent.NavigateBack) {
                    "Expected NavigateBack but got $event"
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Restaurant Name
    // Tests restaurant name shown at top of cart
    // ══════════════════════════════════════════════════════════

    given("cart has items from a restaurant") {

        `when`("first item has restaurantId r1") {
            then("restaurantName in uiState is r1") {

                // WHY test restaurantName?
                // CartScreen shows "Ordering from: Meghana Foods"
                // This helps user know which restaurant they ordered from
                // Derived from menuItem.restaurantId of first cart item

                val item = MenuItem(
                    id           = "m1",
                    restaurantId = "r1",    // ← this is what we check
                    name         = "Chicken Biryani",
                    description  = "",
                    price        = 249.0,
                    imageUrl     = "",
                    category     = "Biryani",
                    isVeg        = false,
                    isRecommended = false,
                    isBestseller  = false,
                    isAvailable   = true,
                )

                fakeCartRepo.seedCart(
                    fakeCartItem("c1", item, 1)
                )
                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value
                    .restaurantName shouldBe "r1"
            }
        }

        `when`("cart is empty") {
            then("restaurantName should be empty string") {

                viewModel = CartViewModel(fakeCartRepo)

                viewModel.uiState.value
                    .restaurantName shouldBe ""
            }
        }
    }
})*/
