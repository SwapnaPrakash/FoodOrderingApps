package com.swapna.foodapp.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class DetectedLocation(
    val locality:  String,
    val address:   String,
    val latitude:  Double,
    val longitude: Double,
)

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @SuppressLint("NewApi")
    suspend fun getAddressFromCoordinates(
        latitude:  Double,
        longitude: Double,
    ): DetectedLocation? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ async API
                suspendCancellableCoroutine<DetectedLocation?> { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val addr   = addresses.firstOrNull()
                        val result = addr?.let {
                            DetectedLocation(
                                locality  = it.subLocality ?: it.locality ?: "Current Location",
                                address   = buildAddressString(it),
                                latitude  = latitude,
                                longitude = longitude,
                            )
                        }
                        // ✅ FIX: use resume(value) not resume(value) {}
                        // WHY deprecated?
                        // Old overload took onCancellation lambda — now replaced
                        // by CancellableContinuation.resume(value) from kotlin.coroutines
                        // New overload handles cancellation internally → safer
                        continuation.resume(result)
                    }
                }
            } else {
                // Below Android 13 sync API
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr      = addresses?.firstOrNull() ?: return null

                DetectedLocation(
                    locality  = addr.subLocality ?: addr.locality ?: "Current Location",
                    address   = buildAddressString(addr),
                    latitude  = latitude,
                    longitude = longitude,
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildAddressString(addr: android.location.Address): String =
        listOfNotNull(
            addr.subLocality,
            addr.locality,
            addr.adminArea,
        ).distinct()
            .joinToString(", ")
}