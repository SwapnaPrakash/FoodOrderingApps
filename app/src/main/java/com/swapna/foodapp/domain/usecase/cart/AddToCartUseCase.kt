package com.swapna.foodapp.domain.usecase.cart

import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem

interface AddToCartUseCase {
    suspend operator fun invoke(
        menuItem: MenuItem,
        quantity: Int,
        customisations: List<CustomisationOption> = emptyList(),
    )
}