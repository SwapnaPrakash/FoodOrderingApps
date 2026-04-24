package com.swapna.foodapp.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class CurrentLocationResult(
    val displayAddress: String,  // "Work, RMZ Ecospace, Bellandur"
    val latitude:       Double,
    val longitude:      Double,
)

sealed class LocationError {
    object PermissionDenied    : LocationError()
    object GpsDisabled         : LocationError()
    object LocationUnavailable : LocationError()
    object GeocodeFailed       : LocationError()
}

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    // ── Get current GPS location + reverse geocode ────────────────────
    // WHY suspendCancellableCoroutine?
    // FusedLocationProviderClient uses callbacks
    // suspendCancellableCoroutine bridges callback → suspend fun
    // Cancels the location request if coroutine is cancelled
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<CurrentLocationResult> {
        return suspendCancellableCoroutine { continuation ->

            val locationRequest = com.google.android.gms.location.CurrentLocationRequest
                .Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedClient.getCurrentLocation(locationRequest, null)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        continuation.resume(
                            Result.failure(
                                Exception(LocationError.LocationUnavailable.toString())
                            )
                        )
                        return@addOnSuccessListener
                    }

                    // Reverse geocode lat/lng → human address
                    val address = reverseGeocode(
                        lat = location.latitude,
                        lng = location.longitude,
                    )

                    if (address == null) {
                        continuation.resume(
                            Result.failure(
                                Exception(LocationError.GeocodeFailed.toString())
                            )
                        )
                    } else {
                        continuation.resume(
                            Result.success(
                                CurrentLocationResult(
                                    displayAddress = address,
                                    latitude       = location.latitude,
                                    longitude      = location.longitude,
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }

            // Cancel location request if coroutine cancelled
            continuation.invokeOnCancellation {
                fusedClient.flushLocations()
            }
        }
    }

    // ── Reverse geocode lat/lng → display string ──────────────────────
    // Returns "Koramangala 5th Block, Bengaluru" style string
    // WHY nullable return?
    // Geocoder can fail if no internet or location out of range
    // Caller handles null = graceful degradation
    private fun reverseGeocode(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())

            // API 33+ has async callback; below uses synchronous
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Synchronous wrapper using blocking variable
                var result: String? = null
                val latch = java.util.concurrent.CountDownLatch(1)

                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    result = formatAddress(addresses.firstOrNull())
                    latch.countDown()
                }

                // Wait max 3 seconds for geocoder callback
                latch.await(3, java.util.concurrent.TimeUnit.SECONDS)
                result
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(addresses?.firstOrNull())
            }
        } catch (e: Exception) {
            null
        }
    }

    // ── Format android.location.Address → display string ─────────────
    // Input:  android.location.Address object
    // Output: "Koramangala 5th Block, Bengaluru" or "HSR Layout, Bengaluru"
    //
    // WHY this specific format?
    // subLocality  = neighbourhood e.g. "Koramangala 5th Block"
    // locality     = city           e.g. "Bengaluru"
    // Together they match your screenshot style
    private fun formatAddress(address: android.location.Address?): String? {
        if (address == null) return null

        // Build parts in priority order
        val parts = listOfNotNull(
            address.subLocality,   // "Koramangala 5th Block"
            address.locality,      // "Bengaluru"
        ).filter { it.isNotEmpty() }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")   // "Koramangala 5th Block, Bengaluru"
        } else {
            // Fallback: use thoroughfare + locality
            listOfNotNull(
                address.thoroughfare,  // "80 Feet Road"
                address.locality,
            ).joinToString(", ").ifEmpty { null }
        }
    }
}