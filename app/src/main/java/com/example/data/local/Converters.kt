package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.LatLngCoord
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.optString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromLatLngList(value: List<LatLngCoord>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach {
            val obj = JSONObject()
            obj.put("lat", it.lat)
            obj.put("lng", it.lng)
            obj.put("label", it.label)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toLatLngList(value: String?): List<LatLngCoord> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<LatLngCoord>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i)
                if (obj != null) {
                    list.add(
                        LatLngCoord(
                            lat = obj.optDouble("lat", 0.0),
                            lng = obj.optDouble("lng", 0.0),
                            label = obj.optString("label", "")
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus?): String {
        return status?.name ?: OrderStatus.DELIVERING.name
    }

    @TypeConverter
    fun toOrderStatus(value: String?): OrderStatus {
        return OrderStatus.fromString(value ?: OrderStatus.DELIVERING.name)
    }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String {
        return method?.name ?: PaymentMethod.NONE.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod {
        return PaymentMethod.fromString(value ?: PaymentMethod.NONE.name)
    }
}
