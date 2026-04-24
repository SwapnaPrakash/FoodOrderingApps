package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCaseImpl
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.fakeCustomisationOption
import com.swapna.foodapp.utils.fakeMenuItem
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
    val useCase        = AddToCartUseCaseImpl(cartRepository)

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

                slot.captured.menuItem.id   shouldBe item.id
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

                // WHY check uniqueness?
                // UUID.randomUUID() must differ per call
                // Duplicate id = Room primary key conflict
                slot1.captured.id shouldNotBe slot2.captured.id
            }
        }

        // ── Customisations ────────────────────────────────────

        context("item with customisations — Large (+₹50) + Hot (+₹0)") {
            val item = fakeMenuItem()
            val customisations = listOf(
                fakeCustomisationOption(id = "large", extraPrice = 50.0),
                fakeCustomisationOption(id = "hot",   extraPrice = 0.0),
            )

            it("CartItem carries all selected customisations") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1, customisations)

                slot.captured.selectedCustomisations.size shouldBe 2
                slot.captured.selectedCustomisations
                    .any { it.id == "large" } shouldBe true
                slot.captured.selectedCustomisations
                    .any { it.id == "hot"   } shouldBe true
            }

            it("customisation extraPrices are preserved") {
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1, customisations)

                val large = slot.captured.selectedCustomisations
                    .find { it.id == "large" }
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
                // WHY require() not if-else?
                // require() throws IllegalArgumentException
                // Consistent with Kotlin stdlib contract
                val below = AppConstants.MIN_CART_QUANTITY - 1
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), below)
                }
            }

            it("quantity 0 throws IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), 0)
                }
            }

            it("negative quantity throws IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    useCase(fakeMenuItem(), -1)
                }
            }

            it("cartRepository NOT called when quantity below MIN") {
                runCatching { useCase(fakeMenuItem(), 0) }
                coVerify(exactly = 0) { cartRepository.addItem(any()) }
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

            listOf(1, 2, 5, 9, AppConstants.MAX_CART_QUANTITY).forEach { qty ->
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
                // base = 249.0, extra = 50.0, qty = 2
                // total = (249 + 50) × 2 = 598.0
                val item = fakeMenuItem(price = 249.0)
                val customisations = listOf(
                    fakeCustomisationOption(extraPrice = 50.0)
                )
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 2, customisations)

                // WHY verify totalPrice?
                // CartItem.totalPrice drives cart total calculation
                // Wrong totalPrice → wrong bill shown to user
                slot.captured.totalPrice shouldBe 598.0
            }

            it("totalPrice with no customisations = basePrice × qty") {
                val item = fakeMenuItem(price = 249.0)
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 3)

                slot.captured.totalPrice shouldBe 747.0
            }

            it("totalPrice with qty 1 and no extras = basePrice") {
                val item = fakeMenuItem(price = 199.0)
                val slot = slot<CartItem>()
                coEvery { cartRepository.addItem(capture(slot)) } just runs

                useCase(item, 1)

                slot.captured.totalPrice shouldBe 199.0
            }
        }

        // ── Repository failure ────────────────────────────────

        context("cartRepository.addItem throws exception") {

            it("exception propagates to caller") {
                coEvery { cartRepository.addItem(any()) } throws
                        Exception("Database error")

                shouldThrow<Exception> {
                    useCase(fakeMenuItem(), 1)
                }
            }

            it("exception message is preserved") {
                coEvery { cartRepository.addItem(any()) } throws
                        Exception("Database error")

                val ex = shouldThrow<Exception> {
                    useCase(fakeMenuItem(), 1)
                }
                ex.message shouldBe "Database error"
            }
        }
    }
})