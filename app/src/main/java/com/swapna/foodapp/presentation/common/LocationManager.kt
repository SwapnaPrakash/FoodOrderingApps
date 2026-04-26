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
    val displayAddress: String,
    val latitude: Double,
    val longitude: Double,
)

sealed class LocationError {
    object PermissionDenied : LocationError()
    object GpsDisabled : LocationError()
    object LocationUnavailable : LocationError()
    object GeocodeFailed : LocationError()
}

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

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
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }

            continuation.invokeOnCancellation {
                fusedClient.flushLocations()
            }
        }
    }

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

    private fun formatAddress(address: android.location.Address?): String? {
        if (address == null) return null

        val parts = listOfNotNull(
            address.subLocality,
            address.locality,
        ).filter { it.isNotEmpty() }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            listOfNotNull(
                address.thoroughfare,
                address.locality,
            ).joinToString(", ").ifEmpty { null }
        }
    }
}