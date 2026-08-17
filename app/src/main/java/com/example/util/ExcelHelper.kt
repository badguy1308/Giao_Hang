package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.CustomerEntity
import com.example.data.local.OrderEntity
import com.example.model.ExcelOrderRow
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object ExcelHelper {

    // Standard Columns: STT | Mã Đơn | Khách Hàng | SĐT | Địa Chỉ | Tiền COD | Dịch Vụ | Trạng Thái | Hàng Hóa
    val SAMPLE_EXCEL_CSV = """
STT,Mã Đơn,Khách Hàng,SĐT,Địa Chỉ,Tiền COD,Dịch Vụ,Trạng Thái,Hàng Hóa
1,GHTK-98421,Nguyễn Văn An,0912345678,128 Nguyễn Trãi Thanh Xuân Hà Nội,350000,Giao Nhanh 2H,Chờ giao,Quần áo thời trang (2 bộ)
2,GHTK-98422,Trần Thị Mai,0987654321,45 Lê Văn Lương Cầu Giấy Hà Nội,0,Tiêu Chuẩn,Đang vận chuyển,Mỹ phẩm Skincare cao cấp
3,GHTK-98423,Lê Hoàng Nam,0901234567,78 Cầu Giấy Quan Hoa Hà Nội,520000,Hỏa Tốc,Đã phân phối,Tai nghe Bluetooth không dây
4,GHTK-98424,Phạm Thu Hương,0934567890,12 Chùa Bộc Đống Đa Hà Nội,180000,Giao Tiết Kiệm,Xuất kho giao,Váy đầm dạ hội nữ
5,GHTK-98425,Hoàng Đức Long,0978901234,99 Giải Phóng Hai Bà Trưng Hà Nội,650000,Giao Nhanh,Chờ giao,Giày thể thao Nam size 42
6,GHTK-98426,Vũ Thúy Hằng,0967890123,55 Phố Huế Hoàn Kiếm Hà Nội,420000,Tiêu Chuẩn,Chờ giao,Túi xách da nữ cao cấp
""".trimIndent()

    /**
     * Parse excel file from Uri (Supports .xlsx, .xls, .csv, .tsv, .txt)
     */
    fun parseExcelFileUri(
        context: Context,
        uri: Uri,
        existingCustomers: List<CustomerEntity>
    ): List<ExcelOrderRow> {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        val fileName = getFileName(context, uri).lowercase()

        return try {
            if (fileName.endsWith(".xlsx") || mimeType.contains("spreadsheetml")) {
                contentResolver.openInputStream(uri)?.use { stream ->
                    parseXlsxStream(stream, existingCustomers)
                } ?: emptyList()
            } else {
                // Read as CSV / text
                contentResolver.openInputStream(uri)?.use { stream ->
                    val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                    parseCsvContent(text, existingCustomers)
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback try reading as text
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                    parseCsvContent(text, existingCustomers)
                } ?: emptyList()
            } catch (ex: Exception) {
                emptyList()
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "file.xlsx"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = it.getString(index) ?: "file.xlsx"
                }
            }
        }
        return name
    }

    /**
     * Parses .xlsx file using standard Java ZipInputStream and XmlPullParser
     */
    private fun parseXlsxStream(
        inputStream: InputStream,
        existingCustomers: List<CustomerEntity>
    ): List<ExcelOrderRow> {
        val sharedStrings = mutableListOf<String>()
        var sheetXmlBytes: ByteArray? = null

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml") {
                    sharedStrings.addAll(parseSharedStrings(zip))
                } else if (entry.name == "xl/worksheets/sheet1.xml") {
                    sheetXmlBytes = zip.readBytes()
                }
                entry = zip.nextEntry
            }
        }

        if (sheetXmlBytes != null) {
            return parseSheetData(sheetXmlBytes!!.inputStream(), sharedStrings, existingCustomers)
        }

        return emptyList()
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val list = mutableListOf<String>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "si") {
                        currentText = StringBuilder()
                    } else if (parser.name == "t") {
                        insideT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        insideT = false
                    } else if (parser.name == "si") {
                        list.add(currentText.toString())
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun parseSheetData(
        stream: InputStream,
        sharedStrings: List<String>,
        existingCustomers: List<CustomerEntity>
    ): List<ExcelOrderRow> {
        val rows = mutableListOf<List<String>>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableMapOf<Int, String>()
        var currentCellRef = ""
        var isStringType = false
        var cellValue = StringBuilder()
        var insideV = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "row") {
                        currentRow = mutableMapOf()
                    } else if (parser.name == "c") {
                        currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                        val type = parser.getAttributeValue(null, "t") ?: ""
                        isStringType = (type == "s")
                        cellValue = StringBuilder()
                    } else if (parser.name == "v") {
                        insideV = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideV) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "v") {
                        insideV = false
                    } else if (parser.name == "c") {
                        val colIndex = colRefToIndex(currentCellRef)
                        val rawVal = cellValue.toString().trim()
                        val finalVal = if (isStringType) {
                            val stringIdx = rawVal.toIntOrNull()
                            if (stringIdx != null && stringIdx in sharedStrings.indices) {
                                sharedStrings[stringIdx]
                            } else {
                                rawVal
                            }
                        } else {
                            rawVal
                        }
                        if (colIndex >= 0) {
                            currentRow[colIndex] = finalVal
                        }
                    } else if (parser.name == "row") {
                        if (currentRow.isNotEmpty()) {
                            val maxCol = currentRow.keys.maxOrNull() ?: 0
                            val rowList = (0..maxCol).map { col -> currentRow[col] ?: "" }
                            rows.add(rowList)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        // Convert rows to ExcelOrderRows
        val result = mutableListOf<ExcelOrderRow>()
        var autoStt = 1
        val defaultLat = 21.028511
        val defaultLng = 105.854444

        for ((index, row) in rows.withIndex()) {
            if (row.isEmpty()) continue

            // Skip header if first row has text like STT / Mã / Họ tên
            if (index == 0 && (row[0].contains("STT", ignoreCase = true) || row.any { it.contains("Mã", ignoreCase = true) || it.contains("Khách", ignoreCase = true) })) {
                continue
            }

            if (row.size >= 4) {
                val stt = row.getOrNull(0)?.toIntOrNull() ?: autoStt
                val code = row.getOrNull(1)?.ifBlank { "DON-$autoStt" } ?: "DON-$autoStt"
                val name = row.getOrNull(2) ?: "Khách hàng $autoStt"
                val phone = row.getOrNull(3) ?: "0900000000"
                val address = row.getOrNull(4) ?: "Hà Nội"
                val cod = row.getOrNull(5)?.replace(".", "")?.replace(",", "")?.replace("đ", "")?.replace("VND", "")?.trim()?.toDoubleOrNull() ?: 0.0
                val service = row.getOrNull(6)?.ifBlank { "Tiêu chuẩn" } ?: "Tiêu chuẩn"
                val statusExcel = row.getOrNull(7)?.ifBlank { "Chờ giao" } ?: "Chờ giao"
                val goods = row.getOrNull(8)?.ifBlank { "Hàng hóa bưu phẩm" } ?: "Hàng hóa bưu phẩm"

                val matchedCustomer = existingCustomers.firstOrNull { customer ->
                    customer.primaryPhone == phone || customer.phoneNumbers.contains(phone)
                }

                val isExisting = matchedCustomer != null
                val lat = matchedCustomer?.coordinates?.firstOrNull()?.lat ?: (defaultLat + (autoStt * 0.005))
                val lng = matchedCustomer?.coordinates?.firstOrNull()?.lng ?: (defaultLng + (autoStt * 0.004))

                result.add(
                    ExcelOrderRow(
                        stt = stt,
                        orderCode = code,
                        customerName = name,
                        phone = phone,
                        address = address,
                        codAmount = cod,
                        serviceType = service,
                        excelStatus = statusExcel,
                        goodsDescription = goods,
                        isExistingCustomer = isExisting,
                        detectedLat = lat,
                        detectedLng = lng
                    )
                )
                autoStt++
            }
        }
        return result
    }

    private fun colRefToIndex(cellRef: String): Int {
        val colChars = cellRef.takeWhile { it.isLetter() }.uppercase()
        if (colChars.isEmpty()) return -1
        var result = 0
        for (char in colChars) {
            result = result * 26 + (char - 'A' + 1)
        }
        return result - 1
    }

    fun parseCsvContent(
        content: String,
        existingCustomers: List<CustomerEntity>
    ): List<ExcelOrderRow> {
        val rows = mutableListOf<ExcelOrderRow>()
        val lines = content.lines().filter { it.isNotBlank() }

        var defaultLat = 21.028511
        var defaultLng = 105.854444

        var isHeader = true
        var autoStt = 1

        for (line in lines) {
            val parts = if (line.contains("\t")) {
                line.split("\t")
            } else if (line.contains(";")) {
                line.split(";")
            } else {
                line.split(",")
            }.map { it.trim().removeSurrounding("\"") }

            if (parts.isEmpty()) continue

            // Check if header row
            if (isHeader && (parts[0].contains("STT", ignoreCase = true) || parts.any { it.contains("Mã", ignoreCase = true) || it.contains("Khách", ignoreCase = true) })) {
                isHeader = false
                continue
            }
            isHeader = false

            if (parts.size >= 4) {
                val stt = parts.getOrNull(0)?.toIntOrNull() ?: autoStt
                val code = parts.getOrNull(1)?.ifBlank { "DON-$autoStt" } ?: "DON-$autoStt"
                val name = parts.getOrNull(2) ?: "Khách hàng $autoStt"
                val phone = parts.getOrNull(3) ?: "0900000000"
                val address = parts.getOrNull(4) ?: "Hà Nội"
                val cod = parts.getOrNull(5)?.replace(".", "")?.replace(",", "")?.replace("đ", "")?.replace("VND", "")?.trim()?.toDoubleOrNull() ?: 0.0
                val service = parts.getOrNull(6)?.ifBlank { "Tiêu chuẩn" } ?: "Tiêu chuẩn"
                val statusExcel = parts.getOrNull(7)?.ifBlank { "Chờ giao" } ?: "Chờ giao"
                val goods = parts.getOrNull(8)?.ifBlank { "Hàng hóa bưu phẩm" } ?: "Hàng hóa bưu phẩm"

                // Match with Room DB CustomerEntity
                val matchedCustomer = existingCustomers.firstOrNull { customer ->
                    customer.primaryPhone == phone || customer.phoneNumbers.contains(phone)
                }

                val isExisting = matchedCustomer != null
                val lat = matchedCustomer?.coordinates?.firstOrNull()?.lat ?: (defaultLat + (autoStt * 0.005))
                val lng = matchedCustomer?.coordinates?.firstOrNull()?.lng ?: (defaultLng + (autoStt * 0.004))

                rows.add(
                    ExcelOrderRow(
                        stt = stt,
                        orderCode = code,
                        customerName = name,
                        phone = phone,
                        address = address,
                        codAmount = cod,
                        serviceType = service,
                        excelStatus = statusExcel,
                        goodsDescription = goods,
                        isExistingCustomer = isExisting,
                        detectedLat = lat,
                        detectedLng = lng
                    )
                )
                autoStt++
            }
        }
        return rows
    }

    fun convertRowsToOrderEntities(rows: List<ExcelOrderRow>): List<OrderEntity> {
        return rows.map { row ->
            OrderEntity(
                orderCode = row.orderCode,
                sequenceNumber = row.stt,
                customerPhone = row.phone,
                customerName = row.customerName,
                address = row.address,
                codAmount = row.codAmount,
                serviceType = row.serviceType,
                excelStatus = row.excelStatus,
                goodsDescription = row.goodsDescription,
                status = if (row.isExistingCustomer) OrderStatus.DELIVERING else OrderStatus.NEW_CUSTOMER,
                paymentMethod = PaymentMethod.NONE,
                latitude = row.detectedLat ?: 21.028511,
                longitude = row.detectedLng ?: 105.854444,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}

