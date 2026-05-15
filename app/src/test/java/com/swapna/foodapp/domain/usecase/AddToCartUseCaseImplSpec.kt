package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCaseImpl
import com.swapna.foodapp.presentation.common.fakes.fakeCustomisationOption
import com.swapna.foodapp.presentation.common.fakes.fakeMenuItem
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.TestConstants.BASE_PRICE_199
import com.swapna.foodapp.utils.TestConstants.BASE_PRICE_249
import com.swapna.foodapp.utils.TestConstants.CART_QTY_1
import com.swapna.foodapp.utils.TestConstants.CUSTOMISATION_HOT
import com.swapna.foodapp.utils.TestConstants.CUSTOMISATION_LARGE
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_LARGE
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_ZERO
import com.swapna.foodapp.utils.TestConstants.QTY_2
import com.swapna.foodapp.utils.TestConstants.QTY_3
import com.swapna.foodapp.utils.TestConstants.QTY_5
import com.swapna.foodapp.utils.TestConstants.QTY_9
import com.swapna.foodapp.utils.TestConstants.QTY_NEGATIVE
import com.swapna.foodapp.utils.TestConstants.QTY_ZERO
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_598
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_747
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot

class AddToCartUseCaseImplSpec : DescribeSpec({

    val cartRepository = mockk<CartRepository>()
    val useCase = AddToCartUseCaseImpl(cartRepository)

    beforeEach {
        clearAllMocks()
        coEvery { cartRepository.addItem(any()) } just runs
    }

    describe("AddToCartUseCaseImpl") {

        // ── Happy path ────────────────────────────────────────

        context("valid item with quantity 1 and no customisations") {
            val item = fakeMenuItem()

            it("calls cartRepository.addItem exactly once") {
                useCase(item, 1)
                coVerify(exactly = 1) { cartRepository.addItem(any()) }
            }

            it("CartItem has correct menuItem") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1)

                slot.captured.menuItem.id shouldBe item.id
                slot.captured.menuItem.name shouldBe item.name
            }

            it("CartItem has correct quantity") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1)

                slot.captured.quantity shouldBe 1
            }

            it("CartItem has empty customisations by default") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1)

                slot.captured.selectedCustomisations shouldBe emptyList()
            }

            it("CartItem id is a non-empty UUID string") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1)

                slot.captured.id.shouldNotBeEmpty()
            }

            it("each call generates a unique CartItem id") {
                val slot1 = slot<CartItem>()
                val slot2 = slot<CartItem>()

                coEvery { cartRepository.addItem(capture(slot1)) } just runs
                useCase(item, 1)

                coEvery { cartRepository.addItem(capture(slot2)) } just runs
                useCase(item, 1)

                slot1.captured.id shouldNotBe slot2.captured.id
            }
        }

        // ── Customisations ────────────────────────────────────

        context("item with customisations — Large (+₹50) + Hot (+₹0)") {
            val item = fakeMenuItem()
            val customisations = listOf(
                fakeCustomisationOption(id = CUSTOMISATION_LARGE, extraPrice = EXTRA_PRICE_LARGE),
                fakeCustomisationOption(id = CUSTOMISATION_HOT, extraPrice = EXTRA_PRICE_ZERO),
            )

            it("CartItem carries all selected customisations") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1, customisations)

                slot.captured.selectedCustomisations.size shouldBe 2
                slot.captured.selectedCustomisations
                    .any { it.id == CUSTOMISATION_LARGE } shouldBe true
                slot.captured.selectedCustomisations
                    .any { it.id == CUSTOMISATION_HOT } shouldBe true
            }

            it("customisation extraPrices are preserved") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1, customisations)

                val large = slot.captured.selectedCustomisations
                    .find { it.id == CUSTOMISATION_LARGE }
                large?.extraPrice shouldBe 50.0
            }
        }

        // ── Quantity boundary — MIN ───────────────────────────

        context("quantity at minimum boundary — MIN_CART_QUANTITY") {

            it("quantity exactly at MIN is accepted") {
                // No exception = passes
                useCase(fakeMenuItem(), AppConstants.MIN_CART_QUANTITY)
                coVerify(exactly = 1) { cartRepository.addItem(any()) }
            }

            it("quantity below MIN throws IllegalArgumentException") {
                val below = AppConstants.MIN_CART_QUANTITY - 1
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), below)
                }
            }

            it("quantity 0 throws IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), QTY_ZERO)
                }
            }

            it("negative quantity throws IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), QTY_NEGATIVE)
                }
            }

            it("cartRepository NOT called when quantity below MIN") {
                runCatching { useCase(fakeMenuItem(), QTY_ZERO) }
                coVerify(exactly = QTY_ZERO) { cartRepository.addItem(any()) }
            }

            it("error message contains MIN_CART_QUANTITY value") {
                val below = AppConstants.MIN_CART_QUANTITY - 1
                val ex = shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), below)
                }
                ex.message shouldNotBe null
                ex.message!!.contains(
                    AppConstants.MIN_CART_QUANTITY.toString()
                ) shouldBe true
            }
        }

        // ── Quantity boundary — MAX ───────────────────────────

        context("quantity at maximum boundary — MAX_CART_QUANTITY") {

            it("quantity exactly at MAX is accepted") {
                useCase(fakeMenuItem(), AppConstants.MAX_CART_QUANTITY)
                coVerify(exactly = 1) { cartRepository.addItem(any()) }
            }

            it("quantity above MAX throws IllegalArgumentException") {
                val above = AppConstants.MAX_CART_QUANTITY + 1
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), above)
                }
            }

            it("cartRepository NOT called when quantity above MAX") {
                val above = AppConstants.MAX_CART_QUANTITY + 1
                runCatching { useCase(fakeMenuItem(), above) }
                coVerify(exactly = 0) { cartRepository.addItem(any()) }
            }

            it("error message contains MAX_CART_QUANTITY value") {
                val above = AppConstants.MAX_CART_QUANTITY + 1
                val ex = shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), above)
                }
                ex.message shouldNotBe null
                ex.message!!.contains(
                    AppConstants.MAX_CART_QUANTITY.toString()
                ) shouldBe true
            }
        }

        // ── Quantity inside valid range ────────────────────────

        context("various valid quantities between MIN and MAX") {

            listOf(
                CART_QTY_1, QTY_2, QTY_5, QTY_9,
                AppConstants.MAX_CART_QUANTITY
            ).forEach { qty ->
                it("quantity $qty is valid — addItem called once") {
                    useCase(fakeMenuItem(), qty)
                    coVerify(exactly = 1) { cartRepository.addItem(any()) }
                    clearAllMocks()
                    coEvery { cartRepository.addItem(any()) } just runs
                }
            }
        }

        // ── CartItem totalPrice ───────────────────────────────

        context("CartItem totalPrice with qty and customisations") {

            it("totalPrice = (basePrice + extras) × qty") {
                val item = fakeMenuItem(price = BASE_PRICE_249)
                val customisations = listOf(
                    fakeCustomisationOption(extraPrice = EXTRA_PRICE_LARGE)
                )
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, QTY_2, customisations)

                slot.captured.totalPrice shouldBe TOTAL_PRICE_598
            }

            it("totalPrice with no customisations = basePrice × qty") {
                val item = fakeMenuItem(price = BASE_PRICE_249)
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, QTY_3)

                slot.captured.totalPrice shouldBe TOTAL_PRICE_747
            }

            it("totalPrice with qty 1 and no extras = basePrice") {
                val item = fakeMenuItem(price = BASE_PRICE_199)
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1)

                slot.captured.totalPrice shouldBe BASE_PRICE_199
            }
        }

        // ── Repository failure ────────────────────────────────

        context("cartRepository.addItem throws exception") {

            it("exception propagates to caller") {
                coEvery { cartRepository.addItem(any()) } throws
                        Exception("Database error")

                shouldThrow<Exception> {
                    useCase(fakeMenuItem(), CART_QTY_1)
                }
            }

            it("exception message is preserved") {
                coEvery { cartRepository.addItem(any()) } throws
                        Exception("Database error")

                val ex = shouldThrow<Exception> {
                    useCase(fakeMenuItem(), CART_QTY_1)
                }
                ex.message shouldBe "Database error"
            }
        }
    }
})