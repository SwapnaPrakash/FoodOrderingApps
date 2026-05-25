package com.swapna.foodapp.presentation.cart

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.presentation.common.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.common.fakes.chickenBiryani
import com.swapna.foodapp.presentation.common.fakes.fakeMenuItem
import com.swapna.foodapp.presentation.common.fakes.muttonBiryani
import com.swapna.foodapp.presentation.common.fakes.plainNaan
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppConstants.ERR_CART_EMPTY
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_REMOVED
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_REMOVED_CART
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_3
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_349
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelSpec : BehaviorSpec({

    val dispatcher = UnconfinedTestDispatcher()

    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var vm: CartViewModel

    fun cartItemOf(
        item: com.swapna.foodapp.domain.model.MenuItem,
        quantity: Int = 1,
        id: String = item.id,
    ) = CartItem(
        id = id,
        menuItem = item,
        quantity = quantity,
        selectedCustomisations = emptyList(),
    )

    beforeEach {
        Dispatchers.setMain(dispatcher)
        fakeCartRepo = FakeCartRepository()
        vm = CartViewModel(
            cartRepository = fakeCartRepo,
            ioDispatcher = dispatcher,
        )
    }

    afterEach {
        Dispatchers.resetMain()
    }

    given("CartScreen opens with empty cart") {

        `when`("ViewModel is created") {

            then("isEmpty should be true") {
                vm.uiState.value.isEmpty shouldBe true
            }
            then("isLoading should be false") {
                vm.uiState.value.isLoading shouldBe false
            }
            then("items list should be empty") {
                vm.uiState.value.items shouldBe emptyList()
            }
            then("error should be null") {
                vm.uiState.value.error shouldBe null
            }
            then("breakdown subtotal should be 0.0") {
                vm.uiState.value.breakdown.subtotal shouldBe 0.0
            }
            then("breakdown deliveryFee should be 0.0") {
                vm.uiState.value.breakdown.deliveryFee shouldBe 0.0
            }
            then("breakdown taxes should be 0.0") {
                vm.uiState.value.breakdown.taxes shouldBe 0.0
            }
            then("breakdown total should be 0.0") {
                vm.uiState.value.breakdown.total shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Cart with Items
    // ══════════════════════════════════════════════════════════

    given("cart has items pre-seeded") {

        `when`("cart has 1 chicken biryani") {
            beforeEach {
                // Seed AFTER VM created
                // MutableStateFlow emits → VM updates reactively
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
            }

            then("isEmpty should be false") {
                vm.uiState.value.isEmpty shouldBe false
            }
            then("items size should be 1") {
                vm.uiState.value.items.size shouldBe 1
            }
            then("item name should be Chicken Biryani") {
                vm.uiState.value.items
                    .first().menuItem.name shouldBe MENU_ITEM_CHICK_BIR
            }
        }

        `when`("cart has 2 different items") {
            beforeEach {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                )
            }

            then("items size should be 2") {
                vm.uiState.value.items.size shouldBe 2
            }
            then("isEmpty should be false") {
                vm.uiState.value.isEmpty shouldBe false
            }
        }

        `when`("cart has 3 mixed items") {
            beforeEach {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                    cartItemOf(plainNaan, id = MENU_ID_3),
                )
            }

            then("items size should be 3") {
                vm.uiState.value.items.size shouldBe 3
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Price Breakdown
    // ══════════════════════════════════════════════════════════

    given("cart has items for price calculation") {

        `when`("1 chicken biryani at price 249") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 1))
            }

            then("subtotal equals item price 249") {
                vm.uiState.value.breakdown.subtotal shouldBe PRICE_249
            }
            then("taxes equals GST rate times subtotal") {
                vm.uiState.value.breakdown.taxes shouldBe
                        PRICE_249 * AppBusinessRules.GST_RATE
            }
            then("deliveryFee is DEFAULT when below threshold") {
                vm.uiState.value.breakdown.deliveryFee shouldBe
                        AppBusinessRules.DEFAULT_DELIVERY_FEE
            }
            then("total equals subtotal plus delivery plus taxes") {
                val b = vm.uiState.value.breakdown
                b.total shouldBe b.subtotal + b.deliveryFee + b.taxes
            }
        }

        `when`("item price above FREE delivery threshold") {
            beforeEach {
                val expensiveItem = fakeMenuItem(
                    id = "m_exp",
                    price = AppBusinessRules.FREE_DELIVERY_ABOVE + 100.0,
                )
                fakeCartRepo.seedCart(cartItemOf(expensiveItem))
            }

            then("deliveryFee should be 0 — free delivery") {
                vm.uiState.value.breakdown.deliveryFee shouldBe 0.0
            }
        }

        `when`("item price exactly at FREE delivery threshold") {
            beforeEach {
                val exactItem = fakeMenuItem(
                    id = "m_exact",
                    price = AppBusinessRules.FREE_DELIVERY_ABOVE,
                )
                fakeCartRepo.seedCart(cartItemOf(exactItem))
            }

            then("deliveryFee should be 0 — boundary case") {
                vm.uiState.value.breakdown.deliveryFee shouldBe 0.0
            }
        }

        `when`("2 items — chicken and mutton biryani") {
            beforeEach {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                )
            }

            then("subtotal is sum of both prices") {
                vm.uiState.value.breakdown.subtotal shouldBe PRICE_249 + PRICE_349
            }
        }

        `when`("chicken biryani with quantity 2") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 2))
            }

            then("subtotal doubles the item price") {
                vm.uiState.value.breakdown.subtotal shouldBe PRICE_249 * 2
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Increment Item
    // ══════════════════════════════════════════════════════════

    given("user taps + button on cart item") {

        `when`("item has qty 1") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 1))
            }

            then("updateQuantity called with qty 2") {
                vm.onIncrementItem(cartItemOf(chickenBiryani, quantity = 1))
                fakeCartRepo.updateQtyCalled shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe MENU_ID_1
                fakeCartRepo.lastUpdatedQty shouldBe 2
            }
        }

        `when`("item has qty 3") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 3))
            }

            then("updateQuantity called with qty 4") {
                vm.onIncrementItem(cartItemOf(chickenBiryani, quantity = 3))
                fakeCartRepo.lastUpdatedQty shouldBe 4
            }
        }

        `when`("item is at MAX_ITEM_QUANTITY") {
            beforeEach {
                fakeCartRepo.seedCart(
                    cartItemOf(
                        item = chickenBiryani,
                        quantity = AppBusinessRules.MAX_ITEM_QUANTITY,
                    )
                )
            }

            then("quantity capped at MAX — does not exceed") {
                vm.onIncrementItem(
                    cartItemOf(
                        item = chickenBiryani,
                        quantity = AppBusinessRules.MAX_ITEM_QUANTITY,
                    )
                )
                fakeCartRepo.lastUpdatedQty shouldBe AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Decrement Item
    // ══════════════════════════════════════════════════════════

    given("user taps - button on cart item") {

        `when`("item has qty 2") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 2))
            }

            then("updateQuantity called with qty 1") {
                vm.onDecrementItem(cartItemOf(chickenBiryani, quantity = 2))
                fakeCartRepo.updateQtyCalled shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe MENU_ID_1
                fakeCartRepo.lastUpdatedQty shouldBe 1
            }
            then("removeItem NOT called") {
                vm.onDecrementItem(cartItemOf(chickenBiryani, quantity = 2))
                fakeCartRepo.removeItemCalled shouldBe false
            }
        }

        `when`("item has qty 1") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 1))
            }

            then("removeItem called — item fully removed") {
                vm.onDecrementItem(cartItemOf(chickenBiryani, quantity = 1))
                fakeCartRepo.removeItemCalled shouldBe true
            }
            then("updateQuantity NOT called") {
                vm.onDecrementItem(cartItemOf(chickenBiryani, quantity = 1))
                fakeCartRepo.updateQtyCalled shouldBe false
            }
            then("ShowSnackbar emitted with item name removed") {
                vm.events.test {
                    vm.onDecrementItem(cartItemOf(chickenBiryani, quantity = 1))
                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowSnackbar("$MENU_ITEM_CHICK_BIR$MSG_ITEM_REMOVED")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Remove Item
    // ══════════════════════════════════════════════════════════

    given("user swipes to delete cart item") {

        `when`("removes chicken biryani") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 2))
            }

            then("removeItem called in repository") {
                vm.onRemoveItem(cartItemOf(chickenBiryani))
                fakeCartRepo.removeItemCalled shouldBe true
            }
            then("ShowSnackbar emitted with correct message") {
                vm.events.test {
                    vm.onRemoveItem(cartItemOf(chickenBiryani))
                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowSnackbar("$MENU_ITEM_CHICK_BIR$MSG_ITEM_REMOVED_CART")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("removes mutton biryani") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(muttonBiryani, id = MENU_ID_2))
            }

            then("ShowSnackbar emitted with mutton biryani name") {
                vm.events.test {
                    vm.onRemoveItem(cartItemOf(muttonBiryani, id = MENU_ID_2))
                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowSnackbar("$MENU_ITEM_MUTTON_BIR$MSG_ITEM_REMOVED_CART")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Place Order
    // ══════════════════════════════════════════════════════════

    given("user taps Place Order") {

        `when`("cart is empty") {
            // WHY no beforeEach:
            // cart already empty from spec-level beforeEach
            // no seeding needed

            then("ShowError emitted with ERR_CART_EMPTY") {
                vm.events.test {
                    vm.onPlaceOrder()
                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowError(ERR_CART_EMPTY)
                    cancelAndIgnoreRemainingEvents()
                }
            }
            then("clearCart NOT called") {
                vm.onPlaceOrder()
                fakeCartRepo.clearCartCalled shouldBe false
            }
        }

        `when`("cart has plain naan below minimum order") {
            beforeEach {
                // PRICE_50 < MIN_ORDER_VALUE → should show error
                fakeCartRepo.seedCart(cartItemOf(plainNaan))
            }

            then("ShowError emitted with min order message") {
                vm.events.test {
                    vm.onPlaceOrder()
                    val event = awaitItem() as CartViewModel.CartEvent.ShowError
                    event.message.contains(
                        AppBusinessRules.MIN_ORDER_VALUE.toInt().toString()
                    ) shouldBe true
                    cancelAndIgnoreRemainingEvents()
                }
            }
            then("clearCart NOT called") {
                vm.onPlaceOrder()
                fakeCartRepo.clearCartCalled shouldBe false
            }
        }

        `when`("cart has chicken biryani above minimum order") {
            beforeEach {
                // PRICE_249 > MIN_ORDER_VALUE → should place order
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
            }

            then("OrderPlaced event emitted") {
                vm.events.test {
                    vm.onPlaceOrder()
                    awaitItem() shouldBe CartViewModel.CartEvent.OrderPlaced
                    cancelAndIgnoreRemainingEvents()
                }
            }
            then("clearCart called on repository") {
                vm.onPlaceOrder()
                fakeCartRepo.clearCartCalled shouldBe true
            }
        }

        `when`("cart has 2 items above minimum") {
            beforeEach {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                )
            }

            then("clearCart called — both items removed") {
                vm.onPlaceOrder()
                fakeCartRepo.clearCartCalled shouldBe true
            }
            then("OrderPlaced emitted") {
                vm.events.test {
                    vm.onPlaceOrder()
                    awaitItem() shouldBe CartViewModel.CartEvent.OrderPlaced
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Navigation Events
    // ══════════════════════════════════════════════════════════

    given("user is on CartScreen") {

        `when`("user taps back arrow") {
            then("NavigateBack event emitted") {
                vm.events.test {
                    vm.onBackPressed()
                    awaitItem() shouldBe CartViewModel.CartEvent.NavigateBack
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Reactive State Updates
    // ══════════════════════════════════════════════════════════

    given("cart changes dynamically after VM created") {

        `when`("item incremented from qty 1 to qty 2") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 1))
            }

            then("breakdown subtotal doubles reactively") {
                vm.uiState.value.breakdown.subtotal shouldBe PRICE_249

                vm.onIncrementItem(cartItemOf(chickenBiryani, quantity = 1))

                vm.uiState.value.breakdown.subtotal shouldBe PRICE_249 * 2
            }
        }

        `when`("item removed — cart becomes empty") {
            beforeEach {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
            }

            then("isEmpty becomes true after remove") {
                vm.uiState.value.isEmpty shouldBe false

                vm.onRemoveItem(cartItemOf(chickenBiryani))

                vm.uiState.value.isEmpty shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Tracking Flags Start Clean
    // ══════════════════════════════════════════════════════════

    given("fresh CartViewModel with no actions") {

        `when`("no methods called yet") {
            then("addItemCalled is false") {
                fakeCartRepo.addItemCalled shouldBe false
            }
            then("updateQtyCalled is false") {
                fakeCartRepo.updateQtyCalled shouldBe false
            }
            then("removeItemCalled is false") {
                fakeCartRepo.removeItemCalled shouldBe false
            }
            then("clearCartCalled is false") {
                fakeCartRepo.clearCartCalled shouldBe false
            }
        }
    }
})