package com.example.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val vietnamLocale = Locale("vi", "VN")

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getInstance(vietnamLocale)
        return "${formatter.format(amount.toLong())} đ"
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", vietnamLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", vietnamLocale)
        return sdf.format(Date(timestamp))
    }

    fun generateVietQrUrl(
        bankShortName: String,
        accountNumber: String,
        amount: Double,
        orderCode: String,
        accountHolder: String
    ): String {
        val cleanAccount = accountNumber.replace(" ", "").trim()
        val cleanBank = bankShortName.trim()
        val encodedAddInfo = URLEncoder.encode("COD $orderCode", StandardCharsets.UTF_8.toString())
        val encodedName = URLEncoder.encode(accountHolder.trim(), StandardCharsets.UTF_8.toString())
        val amountInt = amount.toLong()

        return "https://img.vietqr.io/image/$cleanBank-$cleanAccount-compact2.png?amount=$amountInt&addInfo=$encodedAddInfo&accountName=$encodedName"
    }
}
