package com.example.model

enum class OrderStatus(val displayName: String, val sortPriority: Int) {
    NEW_CUSTOMER("Khách mới", 1),
    DELIVERING("Đang giao", 2),
    RETRY("Giao lại", 3),
    DELIVERED("Đã giao", 4),
    FAILED("Giao thất bại", 5);

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: DELIVERING
        }
    }
}
