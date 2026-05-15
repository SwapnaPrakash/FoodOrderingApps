package com.swapna.foodapp.presentation.cart

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.presentation.common.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.common.fakes.chickenBiryani
import com.swapna.foodapp.presentation.common.fakes.fakeMenuItem
import com.swapna.foodapp.presentation.common.fakes.muttonBiryani
import com.swapna.foodapp.presentation.common.fakes.plainNaan
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.TestConstants.CART_RESTAURANT_ID
import com.swapna.foodapp.utils.TestConstants.ERR_CART_EMPTY_MSG
import com.swapna.foodapp.utils.TestConstants.ERR_MIN_ORDER_PREFIX
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_3
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.MSG_REMOVED
import com.swapna.foodapp.utils.TestConstants.MSG_REMOVED_FROM_CART
import com.swapna.foodapp.utils.TestConstants.PRICE_100
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_50
import com.swapna.foodapp.utils.TestConstants.SUBTOTAL_249_349
import com.swapna.foodapp.utils.TestConstants.SUBTOTAL_249_X2
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelSpec : BehaviorSpec({

    lateinit var fakeCartRepo: FakeCartRepository

    fun cartItemOf(
        item: MenuItem,
        quantity: Int = 1,
        id: String = item.id,
    ) = CartItem(id = id, menuItem = item, quantity = quantity)

    fun createViewModel() = CartViewModel(fakeCartRepo)

    beforeEach {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeCartRepo = FakeCartRepository()
    }

    afterEach { Dispatchers.resetMain() }

    // GROUP 1 — Initial State
    given("CartScreen opens with empty cart") {

        `when`("ViewModel is created") {
            then("isEmpty should be true") {
                createViewModel().uiState.value.isEmpty shouldBe true
            }
        }

        `when`("ViewModel is created") {
            then("isLoading should be false") {
                createViewModel().uiState.value.isLoading shouldBe false
            }
        }

        `when`("ViewModel is created") {
            then("items list should be empty") {
                createViewModel().uiState.value.items shouldBe emptyList()
            }
        }

        `when`("ViewModel is created") {
            then("restaurantName should be empty string") {
                createViewModel().uiState.value.restaurantName shouldBe ""
            }
        }

        `when`("ViewModel is created") {
            then("all breakdown values should be 0.0") {
                val breakdown = createViewModel().uiState.value.breakdown
                breakdown.subtotal shouldBe 0.0
                breakdown.deliveryFee shouldBe 0.0
                breakdown.taxes shouldBe 0.0
                breakdown.total shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Cart with Items
    // ══════════════════════════════════════════════════════════

    given("cart has items pre-seeded") {

        `when`("cart has 1 item") {
            then("isEmpty should be false") {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
                createViewModel().uiState.value.isEmpty shouldBe false
            }
        }

        `when`("cart has 2 different items") {
            then("items list size should be 2") {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                )
                createViewModel().uiState.value.items.size shouldBe 2
            }
        }

        `when`("cart has Chicken Biryani") {
            then("restaurantName comes from first item restaurantId") {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
                // WHY "r1"? fakeMenuItem default restaurantId = CART_RESTAURANT_ID
                createViewModel().uiState.value.restaurantName shouldBe CART_RESTAURANT_ID
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Price Breakdown
    // ══════════════════════════════════════════════════════════

    given("cart has items for price calculation") {

        `when`("1 item at price 50 below delivery threshold") {
            then("breakdown.subtotal equals item price") {
                fakeCartRepo.seedCart(cartItemOf(plainNaan))
                createViewModel().uiState.value.breakdown.subtotal shouldBe PRICE_50
            }
        }

        `when`("1 item at price 50 — delivery threshold check") {
            then("breakdown.deliveryFee equals DEFAULT_DELIVERY_FEE") {
                fakeCartRepo.seedCart(cartItemOf(plainNaan))
                createViewModel().uiState.value.breakdown
                    .deliveryFee shouldBe AppBusinessRules.DEFAULT_DELIVERY_FEE
            }
        }

        `when`("1 item at price 50 — taxes check") {
            then("breakdown.taxes equals GST rate times subtotal") {
                fakeCartRepo.seedCart(cartItemOf(plainNaan))
                createViewModel().uiState.value.breakdown.taxes shouldBe
                        PRICE_50 * AppBusinessRules.GST_RATE
            }
        }

        `when`("1 item price 50 — total calculation") {
            then("breakdown.total equals subtotal plus delivery plus taxes") {
                fakeCartRepo.seedCart(cartItemOf(plainNaan))
                val expected = PRICE_50 +
                        AppBusinessRules.DEFAULT_DELIVERY_FEE +
                        PRICE_50 * AppBusinessRules.GST_RATE

                createViewModel().uiState.value.breakdown.total shouldBe expected
            }
        }

        `when`("subtotal is above FREE delivery threshold") {
            then("breakdown.deliveryFee should be 0 free delivery") {
                val expensiveItem = fakeMenuItem(
                    id = "m_exp",
                    price = AppBusinessRules.FREE_DELIVERY_ABOVE + PRICE_100,
                )
                fakeCartRepo.seedCart(cartItemOf(expensiveItem))
                createViewModel().uiState.value.breakdown.deliveryFee shouldBe 0.0
            }
        }

        `when`("subtotal exactly at FREE delivery threshold") {
            then("breakdown.deliveryFee is 0 boundary case") {
                val exactItem = fakeMenuItem(
                    id = "m_exact",
                    price = AppBusinessRules.FREE_DELIVERY_ABOVE,
                )
                fakeCartRepo.seedCart(cartItemOf(exactItem))
                createViewModel().uiState.value.breakdown.deliveryFee shouldBe 0.0
            }
        }

        `when`("2 items seeded in cart") {
            then("subtotal is sum of both item totals") {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                )
                createViewModel().uiState.value.breakdown.subtotal shouldBe SUBTOTAL_249_349
            }
        }

        `when`("item with qty 2 seeded") {
            then("subtotal doubles the item price") {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani, quantity = 2))
                createViewModel().uiState.value.breakdown.subtotal shouldBe SUBTOTAL_249_X2
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Increment Item
    // ══════════════════════════════════════════════════════════

    given("user taps + button on cart item") {

        `when`("item has qty 1 in cart") {
            then("updateQuantity called with qty 2") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 1)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.onIncrementItem(cartItem)

                fakeCartRepo.updateQtyCalled shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe MENU_ID_1
                fakeCartRepo.lastUpdatedQty shouldBe 2
            }
        }

        `when`("item has qty 3 in cart") {
            then("updateQuantity called with qty 4") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 3)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.onIncrementItem(cartItem)

                fakeCartRepo.lastUpdatedQty shouldBe 4
            }
        }

        `when`("item is at MAX_ITEM_QUANTITY") {
            then("quantity capped — does not exceed MAX") {
                val cartItem = cartItemOf(
                    item = chickenBiryani,
                    quantity = AppBusinessRules.MAX_ITEM_QUANTITY,
                )
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.onIncrementItem(cartItem)

                fakeCartRepo.lastUpdatedQty shouldBe AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Decrement Item
    // ══════════════════════════════════════════════════════════

    given("user taps - button on cart item") {

        `when`("item has qty 2 in cart") {
            then("updateQuantity called with qty 1") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 2)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.onDecrementItem(cartItem)

                fakeCartRepo.updateQtyCalled shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe MENU_ID_1
                fakeCartRepo.lastUpdatedQty shouldBe 1
            }
        }

        `when`("item has qty 1 in cart") {
            then("removeItem called — item fully removed from cart") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 1)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.onDecrementItem(cartItem)

                fakeCartRepo.removeItemCalled shouldBe true
                fakeCartRepo.updateQtyCalled shouldBe false
            }
        }

        `when`("item qty 1 is decremented to 0") {
            then("ShowSnackbar emitted with item name removed") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 1)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.events.test {
                    vm.onDecrementItem(cartItem)

                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowSnackbar("$MENU_ITEM_CHICK_BIR$MSG_REMOVED")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Remove Item
    // ══════════════════════════════════════════════════════════

    given("user swipes to delete cart item") {

        `when`("user removes Chicken Biryani") {
            then("removeItem called in repository") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 2)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.onRemoveItem(cartItem)

                fakeCartRepo.removeItemCalled shouldBe true
            }
        }

        `when`("user removes Mutton Biryani") {
            then("ShowSnackbar emitted with correct name") {
                val cartItem = cartItemOf(muttonBiryani)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.events.test {
                    vm.onRemoveItem(cartItem)

                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowSnackbar("$MENU_ITEM_MUTTON_BIR$MSG_REMOVED_FROM_CART")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Place Order
    // ══════════════════════════════════════════════════════════

    given("user taps Place Order") {

        `when`("cart has items above minimum order value") {
            then("OrderPlaced event emitted") {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
                val vm = createViewModel()

                vm.events.test {
                    vm.onPlaceOrder()
                    awaitItem() shouldBe CartViewModel.CartEvent.OrderPlaced
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("order placed successfully") {
            then("cart cleared after order") {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
                val vm = createViewModel()

                vm.onPlaceOrder()

                fakeCartRepo.clearCartCalled shouldBe true
            }
        }

        `when`("cart is empty") {
            then("ShowError emitted — cannot place empty order") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onPlaceOrder()

                    awaitItem() shouldBe CartViewModel.CartEvent
                        .ShowError(ERR_CART_EMPTY_MSG)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("total is below minimum order value") {
            then("ShowError emitted with min order message") {
                fakeCartRepo.seedCart(cartItemOf(plainNaan))
                val vm = createViewModel()

                vm.events.test {
                    vm.onPlaceOrder()

                    // WHY concat? PRICE_50 is dynamic from AppBusinessRules
                    awaitItem() shouldBe CartViewModel.CartEvent.ShowError(
                        "$ERR_MIN_ORDER_PREFIX${AppBusinessRules.MIN_ORDER_VALUE.toInt()}"
                    )

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("total exactly equals minimum order value") {
            then("OrderPlaced emitted — boundary case passes") {
                val exactItem = fakeMenuItem(
                    id = "m_min",
                    price = AppBusinessRules.MIN_ORDER_VALUE,
                )
                fakeCartRepo.seedCart(cartItemOf(exactItem))
                val vm = createViewModel()

                vm.events.test {
                    vm.onPlaceOrder()
                    awaitItem() shouldBe CartViewModel.CartEvent.OrderPlaced
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("order placed — cart had 2 items") {
            then("cart is empty after order placed") {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                )
                val vm = createViewModel()

                vm.onPlaceOrder()

                fakeCartRepo.clearCartCalled shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Navigation Events
    // ══════════════════════════════════════════════════════════

    given("user is on CartScreen") {

        `when`("user taps back arrow") {
            then("NavigateBack event emitted") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onBackPressed()
                    awaitItem() shouldBe CartViewModel.CartEvent.NavigateBack
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Reactive Breakdown Updates
    // ══════════════════════════════════════════════════════════

    given("cart changes dynamically after VM created") {

        `when`("item incremented from qty 1 to qty 2") {
            then("breakdown.subtotal doubles reactively") {
                val cartItem = cartItemOf(chickenBiryani, quantity = 1)
                fakeCartRepo.seedCart(cartItem)
                val vm = createViewModel()

                vm.uiState.value.breakdown.subtotal shouldBe PRICE_249

                vm.onIncrementItem(cartItem)

                vm.uiState.value.breakdown.subtotal shouldBe SUBTOTAL_249_X2
            }
        }

        `when`("adding item pushes total over free delivery threshold") {
            then("deliveryFee switches from DEFAULT to FREE") {
                fakeCartRepo.seedCart(cartItemOf(chickenBiryani))
                val vm = createViewModel()

                vm.uiState.value.breakdown
                    .deliveryFee shouldBe AppBusinessRules.DEFAULT_DELIVERY_FEE

                val aboveThresholdItem = fakeMenuItem(
                    id = "m_exp",
                    price = AppBusinessRules.FREE_DELIVERY_ABOVE,
                )
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(aboveThresholdItem, id = "m_exp"),
                )
                val vm2 = createViewModel()

                vm2.uiState.value.breakdown.deliveryFee shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Edge Cases
    // ══════════════════════════════════════════════════════════

    given("edge cases in cart operations") {

        `when`("same item added twice with different ids") {
            then("items list has 2 entries") {
                fakeCartRepo.seedCart(
                    CartItem(id = "cart_1", menuItem = chickenBiryani, quantity = 1),
                    CartItem(id = "cart_2", menuItem = chickenBiryani, quantity = 2),
                )
                createViewModel().uiState.value.items.size shouldBe 2
            }
        }

        `when`("cart has 3 mixed items") {
            then("isEmpty is false and size is 3") {
                fakeCartRepo.seedCart(
                    cartItemOf(chickenBiryani),
                    cartItemOf(muttonBiryani, id = MENU_ID_2),
                    cartItemOf(plainNaan, id = MENU_ID_3),
                )
                val vm = createViewModel()
                vm.uiState.value.isEmpty shouldBe false
                vm.uiState.value.items.size shouldBe 3
            }
        }

        `when`("remove tracking flags start as false") {
            then("no repository methods called before any action") {
                val vm = createViewModel()
                fakeCartRepo.addItemCalled shouldBe false
                fakeCartRepo.updateQtyCalled shouldBe false
                fakeCartRepo.removeItemCalled shouldBe false
                fakeCartRepo.clearCartCalled shouldBe false
            }
        }
    }
})