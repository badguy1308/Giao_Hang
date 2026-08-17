package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.LatLngCoord

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val primaryPhone: String, // SĐT chính (PK)
    val names: List<String>, // List<Tên>
    val phoneNumbers: List<String>, // List<SĐT>
    val imageUris: List<String>, // List<Ảnh>
    val coordinates: List<LatLngCoord>, // List<Tọa (Lat, Lng) độ>
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
