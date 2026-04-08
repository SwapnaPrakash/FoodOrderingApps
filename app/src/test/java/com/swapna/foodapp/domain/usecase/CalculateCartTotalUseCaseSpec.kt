package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.domain.usecase.cart.CalculateCartTotalUseCase
import com.swapna.foodapp.utils.AppTestConstants
import com.swapna.foodapp.utils.fakeCartItem
import com.swapna.foodapp.utils.fakeCustomisationOption
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class CalculateCartTotalUseCaseSpec : DescribeSpec({

    val useCase = CalculateCartTotalUseCase()

    describe("CalculateCartTotalUseCase") {

        // ── Empty cart ─────────────────────────────────────────
        context("cart is empty") {
            it("all values should be 0.0") {
                val result = useCase(emptyList())
                result.subtotal    shouldBe 0.0
                result.deliveryFee shouldBe 0.0
                result.taxes       shouldBe 0.0
                result.total       shouldBe 0.0
            }

            it("no delivery fee charged on empty cart") {
                val result = useCase(emptyList())
                result.deliveryFee shouldBe 0.0
            }
        }

        // ── Single item ────────────────────────────────────────
        context("cart has one item — Chicken Biryani ₹249, qty 1") {
            val item   = fakeCartItem(
                price = AppTestConstants.TEST_PRICE_BIRYANI,
                qty   = 1,
            )
            val result = useCase(listOf(item))

            it("subtotal should be 249.0") {
                result.subtotal shouldBe AppTestConstants.TEST_PRICE_BIRYANI
            }

            it("deliveryFee should be default (₹30)") {
                result.deliveryFee shouldBe AppBusinessRules.DEFAULT_DELIVERY_FEE
            }

            it("taxes should be 5% of subtotal — ₹12.45") {
                val expectedTax = AppTestConstants.TEST_PRICE_BIRYANI *
                        AppBusinessRules.GST_RATE
                result.taxes shouldBe expectedTax.plusOrMinus(0.001)
            }

            it("total should be subtotal + delivery + taxes — ₹291.45") {
                val expectedTotal = AppTestConstants.TEST_PRICE_BIRYANI +
                        AppBusinessRules.DEFAULT_DELIVERY_FEE +
                        (AppTestConstants.TEST_PRICE_BIRYANI * AppBusinessRules.GST_RATE)
                result.total shouldBe expectedTotal.plusOrMinus(0.001)
            }
        }

        // ── Multiple items ─────────────────────────────────────
        context("cart has 2 items — ₹249 qty1 + ₹449 qty1") {
            val items = listOf(
                fakeCartItem("c1", qty = 1, price = AppTestConstants.TEST_PRICE_BIRYANI),
                fakeCartItem("c2", qty = 1, price = AppTestConstants.TEST_PRICE_PIZZA),
            )
            val result = useCase(items)

            it("subtotal should be 249 + 449 = 698") {
                result.subtotal shouldBe (AppTestConstants.TEST_PRICE_BIRYANI +
                        AppTestConstants.TEST_PRICE_PIZZA)
            }

            it("delivery fee should still be flat ₹30") {
                result.deliveryFee shouldBe AppBusinessRules.DEFAULT_DELIVERY_FEE
            }

            it("taxes should be 5% of 698 = 34.9") {
                val expectedTax = (AppTestConstants.TEST_PRICE_BIRYANI +
                        AppTestConstants.TEST_PRICE_PIZZA) *
                        AppBusinessRules.GST_RATE
                result.taxes shouldBe expectedTax.plusOrMinus(0.001)
            }

            it("total = 698 + 30 + 34.9 = 762.9") {
                val subtotal = AppTestConstants.TEST_PRICE_BIRYANI +
                        AppTestConstants.TEST_PRICE_PIZZA
                val expected = subtotal +
                        AppBusinessRules.DEFAULT_DELIVERY_FEE +
                        (subtotal * AppBusinessRules.GST_RATE)
                result.total shouldBe expected.plusOrMinus(0.001)
            }
        }

        // ── Quantity > 1 ───────────────────────────────────────
        context("cart has one item — ₹249 qty 3") {
            val item   = fakeCartItem(
                price = AppTestConstants.TEST_PRICE_BIRYANI,
                qty   = 3,
            )
            val result = useCase(listOf(item))

            it("subtotal should be 249 × 3 = 747") {
                result.subtotal shouldBe (AppTestConstants.TEST_PRICE_BIRYANI * 3)
            }

            it("taxes should be 5% of 747 = 37.35") {
                val expectedTax = AppTestConstants.TEST_PRICE_BIRYANI * 3 *
                        AppBusinessRules.GST_RATE
                result.taxes shouldBe expectedTax.plusOrMinus(0.001)
            }
        }

        // ── With customisations ────────────────────────────────
        context("item has customisation adding ₹80 extra, qty 2") {
            val option = fakeCustomisationOption(extraPrice = AppTestConstants.TEST_PRICE_EXTRA)
            val item   = fakeCartItem(
                price = AppTestConstants.TEST_PRICE_BIRYANI,
                qty   = 2,
            ).copy(selectedCustomisations = listOf(option))
            val result = useCase(listOf(item))

            it("subtotal should include extra — (249+80)×2 = 658") {
                val expectedSubtotal = (AppTestConstants.TEST_PRICE_BIRYANI +
                        AppTestConstants.TEST_PRICE_EXTRA) * 2
                result.subtotal shouldBe expectedSubtotal
            }

            it("taxes should be 5% of 658 = 32.9") {
                val subtotal    = (AppTestConstants.TEST_PRICE_BIRYANI +
                        AppTestConstants.TEST_PRICE_EXTRA) * 2
                val expectedTax = subtotal * AppBusinessRules.GST_RATE
                result.taxes shouldBe expectedTax.plusOrMinus(0.001)
            }
        }

        // ── Max quantity boundary ──────────────────────────────
        context("item with maximum allowed quantity (20)") {
            val item   = fakeCartItem(
                price = AppTestConstants.TEST_PRICE_BIRYANI,
                qty   = AppBusinessRules.MAX_CART_QUANTITY,
            )
            val result = useCase(listOf(item))

            it("subtotal should be 249 × 20 = 4980") {
                result.subtotal shouldBe
                        AppTestConstants.TEST_PRICE_BIRYANI *
                        AppBusinessRules.MAX_CART_QUANTITY
            }
        }

        // ── Breakdown consistency ──────────────────────────────
        context("any cart with items") {
            val items  = listOf(
                fakeCartItem("c1", qty = 2, price = AppTestConstants.TEST_PRICE_BIRYANI),
                fakeCartItem("c2", qty = 1, price = AppTestConstants.TEST_PRICE_PIZZA),
            )
            val result = useCase(items)

            it("total should equal subtotal + deliveryFee + taxes") {
                val reconstructed = result.subtotal + result.deliveryFee + result.taxes
                result.total shouldBe reconstructed.plusOrMinus(0.001)
            }

            it("taxes should always be exactly GST_RATE × subtotal") {
                val expectedTax = result.subtotal * AppBusinessRules.GST_RATE
                result.taxes shouldBe expectedTax.plusOrMinus(0.001)
            }

            it("deliveryFee should never be negative") {
                (result.deliveryFee >= 0.0) shouldBe true
            }

            it("total should never be less than subtotal") {
                (result.total >= result.subtotal) shouldBe true
            }
        }
    }
})