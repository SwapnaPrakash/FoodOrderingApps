package com.swapna.foodapp.fakes

import com.swapna.foodapp.presentation.common.CurrentLocationResult
import com.swapna.foodapp.presentation.common.LocationManager
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LAT
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LNG
import io.mockk.coEvery
import io.mockk.mockk

class FakeLocationManager {

    val mock: LocationManager = mockk(relaxed = true)

    fun setLocation(address: String) {
        coEvery { mock.getCurrentLocation() } returns Result.success(
            CurrentLocationResult(
                displayAddress = address,
                latitude = HOME_ADDRESS_LAT,
                longitude = HOME_ADDRESS_LNG,
            )
        )
    }

    fun setError(message: String) {
        coEvery { mock.getCurrentLocation() } returns
                Result.failure(Exception(message))
    }
}