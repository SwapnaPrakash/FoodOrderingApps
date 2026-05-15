package com.swapna.foodapp.domain.model

import com.swapna.foodapp.presentation.common.fakes.fakeCartItem
import com.swapna.foodapp.presentation.common.fakes.fakeCustomisationOption
import com.swapna.foodapp.utils.TestConstants.BREAKDOWN_TAXES_249
import com.swapna.foodapp.utils.TestConstants.BREAKDOWN_TAXES_698
import com.swapna.foodapp.utils.TestConstants.BREAKDOWN_TOTAL_249
import com.swapna.foodapp.utils.TestConstants.CART_ID_1
import com.swapna.foodapp.utils.TestConstants.CART_ID_2
import com.swapna.foodapp.utils.TestConstants.CART_QTY_1
import com.swapna.foodapp.utils.TestConstants.CART_QTY_2
import com.swapna.foodapp.utils.TestConstants.CART_QTY_3
import com.swapna.foodapp.utils.TestConstants.CART_QTY_5
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_10
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_20
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_30
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_5
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_LARGE
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_ZERO
import com.swapna.foodapp.utils.TestConstants.HOME_DELIVERY_FEE
import com.swapna.foodapp.utils.TestConstants.PRICE_100
import com.swapna.foodapp.utils.TestConstants.PRICE_1000
import com.swapna.foodapp.utils.TestConstants.PRICE_199
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_300
import com.swapna.foodapp.utils.TestConstants.PRICE_329
import com.swapna.foodapp.utils.TestConstants.PRICE_398
import com.swapna.foodapp.utils.TestConstants.PRICE_405
import com.swapna.foodapp.utils.TestConstants.PRICE_500
import com.swapna.foodapp.utils.TestConstants.QTY_10
import com.swapna.foodapp.utils.TestConstants.SUBTOTAL_QTY3
import com.swapna.foodapp.utils.TestConstants.SUBTOTAL_TWO_ITEMS
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_299
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_498
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_598
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CartItemSpec : DescribeSpec({

    describe("CartItem") {

        // ── totalPrice — no customisations ───────────────────

        context("totalPrice with no customisations") {

            it("totalPrice = basePrice × qty for qty 1") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                item.totalPrice shouldBe PRICE_249
            }

            it("totalPrice = basePrice × qty for qty 3") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_3)
                item.totalPrice shouldBe SUBTOTAL_QTY3
            }

            it("totalPrice = basePrice × qty for qty 10") {
                val item = fakeCartItem(price = PRICE_100, qty = QTY_10)
                item.totalPrice shouldBe PRICE_1000
            }

            it("totalPrice with zero extras = basePrice × qty") {
                val item = fakeCartItem(price = PRICE_199, qty = CART_QTY_2).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_ZERO),
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_ZERO),
                    )
                )
                item.totalPrice shouldBe PRICE_398
            }
        }

        // ── totalPrice — with customisations ─────────────────

        context("totalPrice with customisations") {

            it("totalPrice = (base + extra) × qty — single option") {
                // base = PRICE_249, extra = EXTRA_PRICE_LARGE, qty = CART_QTY_1
                // total = (249 + 50) × 1 = TOTAL_PRICE_299
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_LARGE),
                    )
                )
                item.totalPrice shouldBe TOTAL_PRICE_299
            }

            it("totalPrice sums all extras from multiple options") {
                // base = PRICE_249, extras = EXTRA_PRICE_LARGE + EXTRA_PRICE_30 = 80.0, qty = CART_QTY_1
                // total = (249 + 80) × 1 = PRICE_329
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_LARGE),
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_30),
                    )
                )
                item.totalPrice shouldBe PRICE_329
            }

            it("totalPrice multiplies (base + extras) by qty") {
                // base = PRICE_249, extra = EXTRA_PRICE_LARGE, qty = CART_QTY_2
                // total = (249 + 50) × 2 = TOTAL_PRICE_598
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_2).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_LARGE),
                    )
                )
                item.totalPrice shouldBe TOTAL_PRICE_598
            }

            it("totalPrice with 3 options and qty 3") {
                // base = PRICE_100, extras = EXTRA_20+EXTRA_10+EXTRA_5 = 35.0, qty = CART_QTY_3
                // total = (100 + 35) × 3 = PRICE_405
                val item = fakeCartItem(price = PRICE_100, qty = CART_QTY_3).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_20),
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_10),
                        fakeCustomisationOption(extraPrice = EXTRA_PRICE_5),
                    )
                )
                item.totalPrice shouldBe PRICE_405
            }
        }

        // ── totalPrice — edge cases ───────────────────────────

        context("totalPrice edge cases") {

            it("totalPrice is always recomputed — computed property not cached") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                item.totalPrice shouldBe PRICE_249

                val doubled = item.copy(quantity = CART_QTY_2)
                doubled.totalPrice shouldBe TOTAL_PRICE_498
            }
        }

        // ── withQuantity ──────────────────────────────────────

        context("withQuantity — creates copy with new quantity") {

            it("withQuantity returns new CartItem with updated qty") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                val result = item.withQuantity(CART_QTY_3)

                result.quantity shouldBe CART_QTY_3
            }

            it("withQuantity does not mutate original item") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                item.withQuantity(CART_QTY_5)

                item.quantity shouldBe CART_QTY_1
            }

            it("withQuantity preserves all other fields — id") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                val result = item.withQuantity(CART_QTY_2)

                result.id shouldBe item.id
            }

            it("withQuantity preserves all other fields — menuItem") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                val result = item.withQuantity(CART_QTY_2)

                result.menuItem shouldBe item.menuItem
            }

            it("withQuantity preserves selectedCustomisations") {
                val customisations = listOf(
                    fakeCustomisationOption(extraPrice = EXTRA_PRICE_LARGE)
                )
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1).copy(
                    selectedCustomisations = customisations
                )
                val result = item.withQuantity(CART_QTY_2)

                result.selectedCustomisations shouldBe customisations
            }

            it("withQuantity updates totalPrice via qty change") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_1)
                val result = item.withQuantity(CART_QTY_2)

                result.totalPrice shouldBe TOTAL_PRICE_498
            }

            it("withQuantity to same qty returns equivalent item") {
                val item = fakeCartItem(price = PRICE_249, qty = CART_QTY_2)
                val result = item.withQuantity(CART_QTY_2)

                result.quantity shouldBe item.quantity
                result.totalPrice shouldBe item.totalPrice
            }

            it("withQuantity chained twice gives final qty") {
                val item = fakeCartItem(price = PRICE_100, qty = CART_QTY_1)

                val result = item.withQuantity(CART_QTY_3).withQuantity(CART_QTY_5)

                result.quantity shouldBe CART_QTY_5
                result.totalPrice shouldBe PRICE_500
            }
        }

        // ── CartItem equality (data class) ────────────────────

        context("CartItem data class equality") {

            it("two CartItems with same fields are equal") {
                val item1 = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)
                val item2 = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)

                item1 shouldBe item2
            }

            it("CartItems with different qty are not equal") {
                val item1 = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)
                val item2 = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_2)

                item1 shouldNotBe item2
            }

            it("CartItems with different id are not equal") {
                val item1 = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)
                val item2 = fakeCartItem(id = CART_ID_2, price = PRICE_249, qty = CART_QTY_1)

                item1 shouldNotBe item2
            }
        }
    }

    // ── CartPriceBreakdown ────────────────────────────────────

    describe("CartPriceBreakdown") {

        context("default construction") {

            it("all fields stored correctly") {
                val breakdown = CartPriceBreakdown(
                    subtotal = PRICE_249,
                    deliveryFee = HOME_DELIVERY_FEE,
                    taxes = BREAKDOWN_TAXES_249,
                    total = BREAKDOWN_TOTAL_249,
                )

                breakdown.subtotal shouldBe PRICE_249
                breakdown.deliveryFee shouldBe HOME_DELIVERY_FEE
                breakdown.taxes shouldBe BREAKDOWN_TAXES_249
                breakdown.total shouldBe BREAKDOWN_TOTAL_249
            }

            it("zero breakdown when cart is empty") {
                val breakdown = CartPriceBreakdown(
                    subtotal = EXTRA_PRICE_ZERO,
                    deliveryFee = EXTRA_PRICE_ZERO,
                    taxes = EXTRA_PRICE_ZERO,
                    total = EXTRA_PRICE_ZERO,
                )

                breakdown.subtotal shouldBe EXTRA_PRICE_ZERO
                breakdown.deliveryFee shouldBe EXTRA_PRICE_ZERO
                breakdown.taxes shouldBe EXTRA_PRICE_ZERO
                breakdown.total shouldBe EXTRA_PRICE_ZERO
            }

            it("total consistency — total equals sum of parts") {
                val subtotal = SUBTOTAL_TWO_ITEMS
                val deliveryFee = HOME_DELIVERY_FEE
                val taxes = BREAKDOWN_TAXES_698
                val breakdown = CartPriceBreakdown(
                    subtotal = subtotal,
                    deliveryFee = deliveryFee,
                    taxes = taxes,
                    total = subtotal + deliveryFee + taxes,
                )

                breakdown.total shouldBe
                        breakdown.subtotal + breakdown.deliveryFee + breakdown.taxes
            }
        }

        context("CartPriceBreakdown equality") {

            it("two identical breakdowns are equal") {
                val b1 = CartPriceBreakdown(
                    PRICE_249,
                    HOME_DELIVERY_FEE,
                    BREAKDOWN_TAXES_249,
                    BREAKDOWN_TOTAL_249
                )
                val b2 = CartPriceBreakdown(
                    PRICE_249,
                    HOME_DELIVERY_FEE,
                    BREAKDOWN_TAXES_249,
                    BREAKDOWN_TOTAL_249
                )

                b1 shouldBe b2
            }

            it("breakdowns with different total are not equal") {
                val b1 = CartPriceBreakdown(
                    PRICE_249,
                    HOME_DELIVERY_FEE,
                    BREAKDOWN_TAXES_249,
                    BREAKDOWN_TOTAL_249
                )
                val b2 =
                    CartPriceBreakdown(PRICE_249, HOME_DELIVERY_FEE, BREAKDOWN_TAXES_249, PRICE_300)

                b1 shouldNotBe b2
            }
        }
    }
})