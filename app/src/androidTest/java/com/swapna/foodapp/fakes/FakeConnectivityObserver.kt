package com.swapna.foodapp.fakes

import com.swapna.foodapp.presentation.common.ConnectivityObserver
import com.swapna.foodapp.presentation.common.NetworkStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

// WHY mockk not subclass?
// ConnectivityObserver is final — cannot be subclassed
// mockk-android mocks final classes on device
// every { networkStatus } returns our controllable flow
class FakeConnectivityObserver {

    private val _status = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    // Create mock and wire our flow to it
    val mock: ConnectivityObserver = mockk(relaxed = true) {
        every { networkStatus } returns _status
    }

    fun setOffline() { _status.value = NetworkStatus.Unavailable }
    fun setOnline()  { _status.value = NetworkStatus.Available   }
}