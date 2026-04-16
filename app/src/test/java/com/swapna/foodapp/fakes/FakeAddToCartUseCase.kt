package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase

// WHY FakeAddToCartUseCase?
// AddToCartUseCase is an interface (fixed Day 37)
// Fake implements interface → no real CartRepository needed
// Tests verify: was it called? with what item? did it throw?

class FakeAddToCartUseCase : AddToCartUseCase {

    var shouldThrow  = false
    var errorMessage = "Failed to add to cart"
    var callCount    = 0
    var lastItem:    MenuItem?                  = null
    var lastQty:     Int                        = 0

    // ✅ Added: track customisations passed to use case
    // Tests verify correct options were passed
    var lastCustomisations: List<CustomisationOption> =
        emptyList()

    override suspend operator fun invoke(
        menuItem:       MenuItem,
        quantity:       Int,
        customisations: List<CustomisationOption>,
    ) {
        callCount++
        lastItem           = menuItem
        lastQty            = quantity
        lastCustomisations = customisations   // ← track these

        if (shouldThrow) {
            throw Exception(errorMessage)
        }
    }

    fun reset() {
        shouldThrow        = false
        errorMessage       = "Failed to add to cart"
        callCount          = 0
        lastItem           = null
        lastQty            = 0
        lastCustomisations = emptyList()
    }
}