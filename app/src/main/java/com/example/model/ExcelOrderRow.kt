package com.example.model

data class ExcelOrderRow(
    val stt: Int,
    val orderCode: String,
    val customerName: String,
    val phone: String,
    val address: String,
    val codAmount: Double,
    val serviceType: String = "Tiêu chuẩn",
    val excelStatus: String = "Chờ giao",
    val goodsDescription: String = "Hàng hóa bưu phẩm",
    val isExistingCustomer: Boolean = false,
    val detectedLat: Double? = null,
    val detectedLng: Double? = null
)
