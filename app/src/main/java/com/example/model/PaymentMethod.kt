package com.example.model

enum class PaymentMethod(val displayName: String) {
    NONE("Chưa thu"),
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản / VietQR");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: NONE
        }
    }
}
