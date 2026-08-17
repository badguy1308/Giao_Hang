package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.OrderStatus
import com.example.model.PaymentMethod

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderCode: String, // Mã vận đơn (PK)
    val sequenceNumber: Int, // STT
    val customerPhone: String,
    val customerName: String,
    val address: String,
    val codAmount: Double,
    val serviceType: String = "Tiêu chuẩn", // Dịch vụ đơn (vd: Nhanh, Tiết kiệm, Bay...)
    val excelStatus: String = "Chờ giao", // Trạng thái đơn từ file Excel đầu vào
    val goodsDescription: String = "Hàng hóa bưu phẩm", // Hàng hóa / Sản phẩm
    val status: OrderStatus = OrderStatus.DELIVERING,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val proofImageUri: String? = null,
    val deliveryNote: String = "",
    val failureReason: String = "",
    val latitude: Double = 21.028511,
    val longitude: Double = 105.854444,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null
)
