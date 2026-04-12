package com.swapna.foodapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

// Sealed class for network status
sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
    object Losing : NetworkStatus()
    object Lost : NetworkStatus()
}

@Singleton
class ConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // ─ Observe network status as a Flow ─
    // callbackFlow converts Android callback API into a Flow
    // distinctUntilChanged prevents duplicate emissions
    val networkStatus: Flow<NetworkStatus> = callbackFlow {

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(NetworkStatus.Losing)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Lost)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Send current state immediately on collection
        trySend(currentNetworkStatus())

        // Clean up when Flow is cancelled
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }

    }.distinctUntilChanged()

    // ── Check current status synchronously ────────────────────
    fun currentNetworkStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork
            ?: return NetworkStatus.Unavailable
        val capabilities = connectivityManager
            .getNetworkCapabilities(network)
            ?: return NetworkStatus.Unavailable
        return if (capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        ) {
            NetworkStatus.Available
        } else {
            NetworkStatus.Unavailable
        }
    }

    fun isOnline(): Boolean =
        currentNetworkStatus() == NetworkStatus.Available
}