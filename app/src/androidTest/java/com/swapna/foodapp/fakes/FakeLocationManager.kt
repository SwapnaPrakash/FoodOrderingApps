package com.swapna.foodapp.fakes

import com.swapna.foodapp.presentation.common.CurrentLocationResult
import com.swapna.foodapp.presentation.common.LocationManager
import io.mockk.coEvery
import io.mockk.mockk

class FakeLocationManager {

    val mock: LocationManager = mockk(relaxed = true)

    fun setLocation(address: String) {
        coEvery { mock.getCurrentLocation() } returns Result.success(
            CurrentLocationResult(
                displayAddress = address,
                latitude       = 12.93,
                longitude      = 77.62,
            )
        )
    }

    fun setError(message: String) {
        coEvery { mock.getCurrentLocation() } returns
                Result.failure(Exception(message))
    }
}