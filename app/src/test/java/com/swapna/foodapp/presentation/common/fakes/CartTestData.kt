package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_3
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_PLAIN_NAAN
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_349
import com.swapna.foodapp.utils.TestConstants.PRICE_50

val chickenBiryani = fakeMenuItem(
    id = MENU_ID_1,
    name = MENU_ITEM_CHICK_BIR,
    price = PRICE_249,
)

val muttonBiryani = fakeMenuItem(
    id = MENU_ID_2,
    name = MENU_ITEM_MUTTON_BIR,
    price = PRICE_349,
)

val plainNaan = fakeMenuItem(
    id = MENU_ID_3,
    name = MENU_ITEM_PLAIN_NAAN,
    price = PRICE_50,
    isVeg = true,
)

