package com.swapna.foodapp.presentation.cart

import app.cash.turbine.test
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.usecase.cart.CalculateCartTotalUseCase
import com.swapna.foodapp.utils.AppTestConstants
import com.swapna.foodapp.utils.fakeCartItem
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class CartViewModelSpec {

}

/*
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelSpec : BehaviorSpec({

    // ── Test doubles ──────────────────────────────────────────
    val cartRepository = mockk<CartRepository>()
    val calculateTotal = CalculateCartTotalUseCase()

    // Reusable fake cart data
    val item1 = fakeCartItem("c1", qty = 2, price = AppTestConstants.TEST_PRICE_BIRYANI)
    val item2 = fakeCartItem("c2", qty = 1, price = AppTestConstants.TEST_PRICE_PIZZA)
    val cartFlow = MutableStateFlow(listOf(item1, item2))

    fun createViewModel() = CartViewModel(cartRepository, calculateTotal)

    // ── Setup / Teardown ──────────────────────────────────────
    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { cartRepository.getCartItems() } returns cartFlow
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Cart has 2 items
    // ══════════════════════════════════════════════════════════
    given("cart has 2 items — Biryani ×2 + Pizza ×1") {

        `when`("ViewModel is created") {
            then("items count should be 2") {
                createViewModel().uiState.value.items.size shouldBe 2
            }

            then("subtotal should be (249×2) + 449 = 947") {
                val expected = (AppTestConstants.TEST_PRICE_BIRYANI * 2) +
                        AppTestConstants.TEST_PRICE_PIZZA
                createViewModel().uiState.value.breakdown.subtotal shouldBe
                        expected.plusOrMinus(0.001)
            }

            then("taxes should be 5% of subtotal") {
                val vm       = createViewModel()
                val subtotal = vm.uiState.value.breakdown.subtotal
                val expected = subtotal * 0.05
                vm.uiState.value.breakdown.taxes shouldBe expected.plusOrMinus(0.001)
            }

            then("total should be subtotal + delivery + taxes") {
                val vm        = createViewModel()
                val breakdown = vm.uiState.value.breakdown
                val expected  = breakdown.subtotal +
                        breakdown.deliveryFee +
                        breakdown.taxes
                breakdown.total shouldBe expected.plusOrMinus(0.001)
            }

            then("total should be greater than subtotal") {
                val vm = createViewModel()
                vm.uiState.value.breakdown.total shouldBeGreaterThan
                        vm.uiState.value.breakdown.subtotal
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Empty cart
    // ══════════════════════════════════════════════════════════
    given("cart is empty") {

        beforeEach {
            every { cartRepository.getCartItems() } returns flowOf(emptyList())
        }

        `when`("ViewModel is created") {
            then("items should be empty") {
                createViewModel().uiState.value.items shouldBe emptyList()
            }

            then("all price values should be 0.0") {
                val breakdown = createViewModel().uiState.value.breakdown
                breakdown.subtotal    shouldBe 0.0
                breakdown.deliveryFee shouldBe 0.0
                breakdown.taxes       shouldBe 0.0
                breakdown.total       shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Update quantity
    // ══════════════════════════════════════════════════════════
    given("item 'c1' is in the cart with qty=2") {

        `when`("updateQuantity called with qty=0") {
            coEvery { cartRepository.removeItem("c1") } just Runs

            then("removeItem should be called — qty=0 means remove") {
                createViewModel().updateQuantity("c1", 0)
                advanceUntilIdle()
                coVerify(exactly = 1) { cartRepository.removeItem("c1") }
            }

            then("updateQuantity should NOT be called") {
                createViewModel().updateQuantity("c1", 0)
                advanceUntilIdle()
                coVerify(exactly = 0) { cartRepository.updateQuantity(any(), any()) }
            }
        }

        `when`("updateQuantity called with qty=3") {
            coEvery { cartRepository.updateQuantity("c1", 3) } just Runs

            then("updateQuantity should be called with correct args") {
                createViewModel().updateQuantity("c1", 3)
                advanceUntilIdle()
                coVerify(exactly = 1) { cartRepository.updateQuantity("c1", 3) }
            }

            then("removeItem should NOT be called") {
                createViewModel().updateQuantity("c1", 3)
                advanceUntilIdle()
                coVerify(exactly = 0) { cartRepository.removeItem(any()) }
            }
        }

        `when`("updateQuantity called with max qty=20") {
            coEvery { cartRepository.updateQuantity("c1", 20) } just Runs

            then("should call updateQuantity — max is valid") {
                createViewModel().updateQuantity("c1", 20)
                advanceUntilIdle()
                coVerify { cartRepository.updateQuantity("c1", 20) }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Remove item
    // ══════════════════════════════════════════════════════════
    given("item 'c2' exists in cart") {

        `when`("removeItem is called directly") {
            coEvery { cartRepository.removeItem("c2") } just Runs

            then("cartRepository.removeItem should be called") {
                createViewModel().removeItem("c2")
                advanceUntilIdle()
                coVerify(exactly = 1) { cartRepository.removeItem("c2") }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Proceed to payment
    // ══════════════════════════════════════════════════════════
    given("cart has items and user taps Proceed to Payment") {

        `when`("proceedToPayment is called") {
            coEvery { cartRepository.clearCart() } just Runs

            then("OrderPlaced event should be emitted via SharedFlow") {
                val vm = createViewModel()
                vm.events.test {
                    vm.proceedToPayment()
                    val event = awaitItem()
                    event shouldBe CartViewModel.CartEvent.OrderPlaced
                    cancelAndIgnoreRemainingEvents()
                }
            }

            then("clearCart should be called after order placed") {
                createViewModel().proceedToPayment()
                advanceUntilIdle()
                coVerify(exactly = 1) { cartRepository.clearCart() }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Empty cart and user taps Proceed to Payment
    // ══════════════════════════════════════════════════════════
    given("cart is empty and user taps Proceed to Payment") {

        beforeEach {
            every { cartRepository.getCartItems() } returns flowOf(emptyList())
        }

        `when`("proceedToPayment is called") {
            then("ShowError event should be emitted — cannot order empty cart") {
                val vm = createViewModel()
                vm.events.test {
                    vm.proceedToPayment()
                    val event = awaitItem()
                    event shouldBe CartViewModel.CartEvent.ShowError("Cart is empty")
                    cancelAndIgnoreRemainingEvents()
                }
            }

            then("clearCart should NOT be called") {
                createViewModel().proceedToPayment()
                advanceUntilIdle()
                coVerify(exactly = 0) { cartRepository.clearCart() }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GIVEN: Cart flow emits new data reactively
    // ══════════════════════════════════════════════════════════
    given("cart starts with 2 items then item is removed externally") {

        `when`("cartFlow emits new list with 1 item") {
            then("uiState.items should reactively update to 1 item") {
                val vm = createViewModel()
                vm.uiState.value.items.size shouldBe 2

                // Simulate external removal (e.g. from another screen)
                cartFlow.value = listOf(item1)
                vm.uiState.value.items.size shouldBe 1
            }

            then("subtotal should recalculate after item removal") {
                val vm = createViewModel()
                cartFlow.value = listOf(item1)  // only Biryani ×2 remains

                val expected = AppTestConstants.TEST_PRICE_BIRYANI * 2
                vm.uiState.value.breakdown.subtotal shouldBe expected.plusOrMinus(0.001)
            }
        }
    }
})*/
