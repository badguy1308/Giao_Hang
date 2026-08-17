package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.model.LatLngCoord
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Default reference coordinates (Hanoi center: 21.028511, 105.854444)
    val defaultLocation = LatLngCoord(21.028511, 105.854444, "Vị trí mặc định")

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLngCoord {
        return try {
            val cts = CancellationTokenSource()
            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { loc ->
                    if (continuation.isActive) continuation.resume(loc)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
            }

            if (location != null) {
                LatLngCoord(
                    lat = location.latitude,
                    lng = location.longitude,
                    label = "Vị trí GPS hiện tại"
                )
            } else {
                // Fallback to last known location
                val lastLoc: Location? = suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        if (continuation.isActive) continuation.resume(loc)
                    }.addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
                if (lastLoc != null) {
                    LatLngCoord(lastLoc.latitude, lastLoc.longitude, "Vị trí gần nhất")
                } else {
                    defaultLocation
                }
            }
        } catch (e: Exception) {
            defaultLocation
        }
    }

    companion object {
        fun calculateDistanceMeters(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {
            val results = FloatArray(1)
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return results[0].toDouble()
        }

        fun formatDistance(meters: Double): String {
            return if (meters < 1000) {
                "${meters.toInt()} m"
            } else {
                val km = meters / 1000.0
                String.format(java.util.Locale.US, "%.1f km", km)
            }
        }
    }
}
