package com.swapna.foodapp.utils.extensions


fun String.parsePriceString(): Double =
    this.replace("[^0-9.]".toRegex(), "")
        .toDoubleOrNull() ?: 0.0