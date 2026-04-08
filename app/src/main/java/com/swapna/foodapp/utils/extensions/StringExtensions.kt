package com.swapna.foodapp.utils.extensions

fun String.parsePriceString(): Double {
    return replace(",", "")
        .split(" ")
        .firstOrNull()
        ?.toDoubleOrNull()
        ?: 0.0
}