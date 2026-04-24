package com.swapna.foodapp.utils

import com.swapna.foodapp.presentation.common.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface IConnectivityObserver {
    val networkStatus: Flow<NetworkStatus>
}