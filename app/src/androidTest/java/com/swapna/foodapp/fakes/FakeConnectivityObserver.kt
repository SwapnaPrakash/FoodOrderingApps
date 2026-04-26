package com.swapna.foodapp.fakes

import com.swapna.foodapp.presentation.common.ConnectivityObserver
import com.swapna.foodapp.presentation.common.NetworkStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityObserver {

    private val _status = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    val mock: ConnectivityObserver = mockk(relaxed = true) {
        every { networkStatus } returns _status
    }

    fun setOffline() {
        _status.value = NetworkStatus.Unavailable
    }

    fun setOnline() {
        _status.value = NetworkStatus.Available
    }
}