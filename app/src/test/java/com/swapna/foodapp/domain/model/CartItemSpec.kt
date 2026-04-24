package com.swapna.foodapp.domain.model

import com.swapna.foodapp.utils.fakeCartItem
import com.swapna.foodapp.utils.fakeCustomisationOption
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CartItemSpec : DescribeSpec({

    describe("CartItem") {

        // ── totalPrice — no customisations ───────────────────

        context("totalPrice with no customisations") {

            it("totalPrice = basePrice × qty for qty 1") {
                val item = fakeCartItem(price = 249.0, qty = 1)
                item.totalPrice shouldBe 249.0
            }

            it("totalPrice = basePrice × qty for qty 3") {
                val item = fakeCartItem(price = 249.0, qty = 3)
                item.totalPrice shouldBe 747.0
            }

            it("totalPrice = basePrice × qty for qty 10") {
                val item = fakeCartItem(price = 100.0, qty = 10)
                item.totalPrice shouldBe 1000.0
            }

            it("totalPrice with zero extras = basePrice × qty") {
                val item = fakeCartItem(price = 199.0, qty = 2).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = 0.0),
                        fakeCustomisationOption(extraPrice = 0.0),
                    )
                )
                item.totalPrice shouldBe 398.0
            }
        }

        // ── totalPrice — with customisations ─────────────────

        context("totalPrice with customisations") {

            it("totalPrice = (base + extra) × qty — single option") {
                // base = 249.0, extra = 50.0, qty = 1
                // total = (249 + 50) × 1 = 299.0
                val item = fakeCartItem(price = 249.0, qty = 1).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = 50.0),
                    )
                )
                item.totalPrice shouldBe 299.0
            }

            it("totalPrice sums all extras from multiple options") {
                // base = 249.0, extras = 50.0 + 30.0 = 80.0, qty = 1
                // total = (249 + 80) × 1 = 329.0
                val item = fakeCartItem(price = 249.0, qty = 1).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = 50.0),
                        fakeCustomisationOption(extraPrice = 30.0),
                    )
                )
                item.totalPrice shouldBe 329.0
            }

            it("totalPrice multiplies (base + extras) by qty") {
                // base = 249.0, extra = 50.0, qty = 2
                // total = (249 + 50) × 2 = 598.0
                val item = fakeCartItem(price = 249.0, qty = 2).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = 50.0),
                    )
                )
                item.totalPrice shouldBe 598.0
            }

            it("totalPrice with 3 options and qty 3") {
                // base = 100.0, extras = 20 + 10 + 5 = 35.0, qty = 3
                // total = (100 + 35) × 3 = 405.0
                val item = fakeCartItem(price = 100.0, qty = 3).copy(
                    selectedCustomisations = listOf(
                        fakeCustomisationOption(extraPrice = 20.0),
                        fakeCustomisationOption(extraPrice = 10.0),
                        fakeCustomisationOption(extraPrice = 5.0),
                    )
                )
                item.totalPrice shouldBe 405.0
            }
        }

        // ── totalPrice — edge cases ───────────────────────────

        context("totalPrice edge cases") {

            it("totalPrice is always recomputed — computed property not cached") {
                // WHY test this?
                // totalPrice is a get() property — no backing field
                // Must recompute on every call
                // copy() with new qty must give new total
                val item = fakeCartItem(price = 249.0, qty = 1)
                item.totalPrice shouldBe 249.0

                val doubled = item.copy(quantity = 2)
                doubled.totalPrice shouldBe 498.0
            }
        }

        // ── withQuantity ──────────────────────────────────────

        context("withQuantity — creates copy with new quantity") {

            it("withQuantity returns new CartItem with updated qty") {
                val item   = fakeCartItem(price = 249.0, qty = 1)
                val result = item.withQuantity(3)

                result.quantity shouldBe 3
            }

            it("withQuantity does not mutate original item") {
                val item   = fakeCartItem(price = 249.0, qty = 1)
                item.withQuantity(5)

                // Original unchanged
                item.quantity shouldBe 1
            }

            it("withQuantity preserves all other fields — id") {
                val item   = fakeCartItem(price = 249.0, qty = 1)
                val result = item.withQuantity(2)

                result.id shouldBe item.id
            }

            it("withQuantity preserves all other fields — menuItem") {
                val item   = fakeCartItem(price = 249.0, qty = 1)
                val result = item.withQuantity(2)

                result.menuItem shouldBe item.menuItem
            }

            it("withQuantity preserves selectedCustomisations") {
                val customisations = listOf(
                    fakeCustomisationOption(extraPrice = 50.0)
                )
                val item   = fakeCartItem(price = 249.0, qty = 1).copy(
                    selectedCustomisations = customisations
                )
                val result = item.withQuantity(2)

                result.selectedCustomisations shouldBe customisations
            }

            it("withQuantity updates totalPrice via qty change") {
                // WHY test totalPrice after withQuantity?
                // totalPrice uses quantity in calculation
                // withQuantity(2) must reflect in new totalPrice
                val item   = fakeCartItem(price = 249.0, qty = 1)
                val result = item.withQuantity(2)

                result.totalPrice shouldBe 498.0
            }

            it("withQuantity to same qty returns equivalent item") {
                val item   = fakeCartItem(price = 249.0, qty = 2)
                val result = item.withQuantity(2)

                result.quantity   shouldBe item.quantity
                result.totalPrice shouldBe item.totalPrice
            }

            it("withQuantity chained twice gives final qty") {
                val item = fakeCartItem(price = 100.0, qty = 1)

                val result = item.withQuantity(3).withQuantity(5)

                result.quantity   shouldBe 5
                result.totalPrice shouldBe 500.0
            }
        }

        // ── CartItem equality (data class) ────────────────────

        context("CartItem data class equality") {

            it("two CartItems with same fields are equal") {
                val item1 = fakeCartItem(id = "c1", price = 249.0, qty = 1)
                val item2 = fakeCartItem(id = "c1", price = 249.0, qty = 1)

                item1 shouldBe item2
            }

            it("CartItems with different qty are not equal") {
                val item1 = fakeCartItem(id = "c1", price = 249.0, qty = 1)
                val item2 = fakeCartItem(id = "c1", price = 249.0, qty = 2)

                item1 shouldNotBe item2
            }

            it("CartItems with different id are not equal") {
                val item1 = fakeCartItem(id = "c1", price = 249.0, qty = 1)
                val item2 = fakeCartItem(id = "c2", price = 249.0, qty = 1)

                item1 shouldNotBe item2
            }
        }
    }

    // ── CartPriceBreakdown ────────────────────────────────────

    describe("CartPriceBreakdown") {

        context("default construction") {

            it("all fields stored correctly") {
                val breakdown = CartPriceBreakdown(
                    subtotal    = 249.0,
                    deliveryFee = 30.0,
                    taxes       = 12.45,
                    total       = 291.45,
                )

                breakdown.subtotal    shouldBe 249.0
                breakdown.deliveryFee shouldBe 30.0
                breakdown.taxes       shouldBe 12.45
                breakdown.total       shouldBe 291.45
            }

            it("zero breakdown when cart is empty") {
                val breakdown = CartPriceBreakdown(
                    subtotal    = 0.0,
                    deliveryFee = 0.0,
                    taxes       = 0.0,
                    total       = 0.0,
                )

                breakdown.subtotal    shouldBe 0.0
                breakdown.deliveryFee shouldBe 0.0
                breakdown.taxes       shouldBe 0.0
                breakdown.total       shouldBe 0.0
            }

            it("total consistency — total equals sum of parts") {
                val subtotal    = 698.0
                val deliveryFee = 30.0
                val taxes       = 34.9
                val breakdown   = CartPriceBreakdown(
                    subtotal    = subtotal,
                    deliveryFee = deliveryFee,
                    taxes       = taxes,
                    total       = subtotal + deliveryFee + taxes,
                )

                breakdown.total shouldBe
                        breakdown.subtotal + breakdown.deliveryFee + breakdown.taxes
            }
        }

        context("CartPriceBreakdown equality") {

            it("two identical breakdowns are equal") {
                val b1 = CartPriceBreakdown(249.0, 30.0, 12.45, 291.45)
                val b2 = CartPriceBreakdown(249.0, 30.0, 12.45, 291.45)

                b1 shouldBe b2
            }

            it("breakdowns with different total are not equal") {
                val b1 = CartPriceBreakdown(249.0, 30.0, 12.45, 291.45)
                val b2 = CartPriceBreakdown(249.0, 30.0, 12.45, 300.00)

                b1 shouldNotBe b2
            }
        }
    }
})