package com.swapna.foodapp.utils

import com.swapna.foodapp.presentation.common.CurrentLocationResult

interface ILocationManager {
    suspend fun getCurrentLocation(): Result<CurrentLocationResult>
}