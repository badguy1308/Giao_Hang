package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object IntentsHelper {

    fun makePhoneCall(context: Context, phoneNumber: String) {
        val cleanPhone = phoneNumber.replace(" ", "").trim()
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở ứng dụng gọi điện: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String = "") {
        val cleanPhone = phoneNumber.replace(" ", "").trim()
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message.ifBlank { "Chào bạn, Shipper đang giao đơn hàng đến bạn. Vui lòng để ý điện thoại nhé!" })
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở ứng dụng tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendOrderSmsWithBankInfo(
        context: Context,
        customerPhone: String,
        customerName: String,
        orderCode: String,
        codAmount: Double,
        bankShortName: String,
        accountNumber: String,
        accountHolder: String
    ) {
        val formattedMoney = Formatters.formatCurrency(codAmount)
        val message = if (accountNumber.isNotBlank()) {
            "Chào anh/chị $customerName, em giao hàng đơn $orderCode. Tiền COD: $formattedMoney. Anh/chị chuyển khoản vào STK: $accountNumber - $bankShortName - Chủ TK: $accountHolder. Nội dung CK: $orderCode. Em cảm ơn ạ!"
        } else {
            "Chào anh/chị $customerName, em giao hàng đơn $orderCode. Tiền COD: $formattedMoney. Em đang đến giao hàng, anh/chị để ý điện thoại nhận hàng giúp em nhé!"
        }
        sendSms(context, customerPhone, message)
    }

    fun openZalo(context: Context, phoneNumber: String) {
        val cleanPhone = phoneNumber.replace("+84", "0").replace(" ", "").trim()
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            // Try direct web/app link to Zalo chat
            val uri = Uri.parse("https://zalo.me/$cleanPhone")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở Zalo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openGoogleMapsNavigation(context: Context, lat: Double, lng: Double, label: String = "Điểm giao") {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}
