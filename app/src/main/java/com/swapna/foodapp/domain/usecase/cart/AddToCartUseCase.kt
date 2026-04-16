package com.swapna.foodapp.domain.usecase.cart

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.ERROR_MAX_QUANTITY
import com.swapna.foodapp.utils.AppConstants.ERROR_MIN_QUANTITY
import java.util.UUID
import javax.inject.Inject


interface AddToCartUseCase {
    suspend operator fun invoke(
        menuItem:       MenuItem,
        quantity:       Int,
        customisations: List<CustomisationOption> = emptyList(),
    )
}