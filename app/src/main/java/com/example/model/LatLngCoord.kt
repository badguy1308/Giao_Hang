package com.example.model

import androidx.annotation.Keep

@Keep
data class LatLngCoord(
    val lat: Double,
    val lng: Double,
    val label: String = ""
)
