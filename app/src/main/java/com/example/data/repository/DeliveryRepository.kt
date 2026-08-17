package com.example.data.repository

import android.content.Context
import com.example.data.datastore.BankSettings
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.CustomerEntity
import com.example.data.local.OrderEntity
import com.example.model.LatLngCoord
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class CodReconciliation(
    val totalOrders: Int,
    val deliveredCount: Int,
    val pendingCount: Int,
    val failedCount: Int,
    val totalCodAmount: Double,
    val cashCollected: Double,
    val bankCollected: Double,
    val totalCollected: Double,
    val pendingCodAmount: Double
)

class DeliveryRepository(
    private val database: AppDatabase,
    private val settingsDataStore: SettingsDataStore,
    private val context: Context
) {
    private val customerDao = database.customerDao()
    private val orderDao = database.orderDao()

    val allOrdersFlow: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allCustomersFlow: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val bankSettingsFlow: Flow<BankSettings> = settingsDataStore.bankSettingsFlow

    fun getDeliveredHistoryForCustomer(phone: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByCustomerPhone(phone)
    }

    suspend fun initializeSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        if (customerDao.getCustomerCount() == 0) {
            val sampleCustomers = listOf(
                CustomerEntity(
                    primaryPhone = "0912345678",
                    names = listOf("Nguyễn Văn An", "Anh An Kế Toán", "Cửa hàng An Phát"),
                    phoneNumbers = listOf("0912345678", "0981112233"),
                    imageUris = listOf(
                        "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600",
                        "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600"
                    ),
                    coordinates = listOf(
                        LatLngCoord(21.002235, 105.819876, "Nhà riêng - Ngõ 128 Nguyễn Trãi"),
                        LatLngCoord(21.006500, 105.823400, "Cơ quan - Royal City")
                    ),
                    notes = "Khách quen, hay gửi hàng tại quầy lễ tân toà R2",
                    updatedAt = System.currentTimeMillis() - 86400000
                ),
                CustomerEntity(
                    primaryPhone = "0987654321",
                    names = listOf("Trần Thị Mai", "Chị Mai Dược Phẩm"),
                    phoneNumbers = listOf("0987654321", "0977223344"),
                    imageUris = listOf(
                        "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=600"
                    ),
                    coordinates = listOf(
                        LatLngCoord(21.011800, 105.803500, "45 Lê Văn Lương, Cầu Giấy")
                    ),
                    notes = "Chỉ nhận hàng giờ hành chính (8h30 - 17h)",
                    updatedAt = System.currentTimeMillis() - 72000000
                ),
                CustomerEntity(
                    primaryPhone = "0901234567",
                    names = listOf("Lê Hoàng Nam", "Nam Mobile"),
                    phoneNumbers = listOf("0901234567"),
                    imageUris = listOf(
                        "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600"
                    ),
                    coordinates = listOf(
                        LatLngCoord(21.034500, 105.795000, "78 Cầu Giấy, Quan Hoa")
                    ),
                    notes = "Nhà mặt đường cạnh cây xăng Quan Hoa",
                    updatedAt = System.currentTimeMillis() - 43200000
                ),
                CustomerEntity(
                    primaryPhone = "0934567890",
                    names = listOf("Phạm Thu Hương"),
                    phoneNumbers = listOf("0934567890"),
                    imageUris = emptyList(),
                    coordinates = listOf(
                        LatLngCoord(21.008000, 105.829000, "12 Chùa Bộc, Đống Đa")
                    ),
                    notes = "Shop quần áo tầng 1",
                    updatedAt = System.currentTimeMillis() - 21600000
                ),
                CustomerEntity(
                    primaryPhone = "0978901234",
                    names = listOf("Hoàng Đức Long", "Anh Long Tech"),
                    phoneNumbers = listOf("0978901234", "0966554433"),
                    imageUris = listOf(
                        "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600"
                    ),
                    coordinates = listOf(
                        LatLngCoord(20.999500, 105.842000, "99 Giải Phóng, Hai Bà Trưng")
                    ),
                    notes = "Giao lên phòng 602 toà nhà văn phòng",
                    updatedAt = System.currentTimeMillis() - 10800000
                )
            )
            customerDao.insertCustomers(sampleCustomers)
        }

        if (orderDao.getOrderCount() == 0) {
            val sampleOrders = listOf(
                OrderEntity(
                    orderCode = "GHTK-98421",
                    sequenceNumber = 1,
                    customerPhone = "0912345678",
                    customerName = "Nguyễn Văn An",
                    address = "128 Nguyễn Trãi, Thanh Xuân, Hà Nội",
                    codAmount = 350000.0,
                    serviceType = "Giao Nhanh 2H",
                    excelStatus = "Chờ giao",
                    goodsDescription = "Quần áo thời trang (2 bộ)",
                    status = OrderStatus.DELIVERING,
                    paymentMethod = PaymentMethod.NONE,
                    proofImageUri = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600",
                    deliveryNote = "Giao cổng sau ngõ 128",
                    latitude = 21.002235,
                    longitude = 105.819876,
                    createdAt = System.currentTimeMillis() - 7200000
                ),
                OrderEntity(
                    orderCode = "GHTK-98422",
                    sequenceNumber = 2,
                    customerPhone = "0987654321",
                    customerName = "Trần Thị Mai",
                    address = "45 Lê Văn Lương, Cầu Giấy, Hà Nội",
                    codAmount = 0.0,
                    serviceType = "Tiêu Chuẩn",
                    excelStatus = "Đang vận chuyển",
                    goodsDescription = "Mỹ phẩm Skincare cao cấp",
                    status = OrderStatus.DELIVERING,
                    paymentMethod = PaymentMethod.NONE,
                    proofImageUri = "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=600",
                    deliveryNote = "Đã thanh toán trước 0đ COD",
                    latitude = 21.011800,
                    longitude = 105.803500,
                    createdAt = System.currentTimeMillis() - 6500000
                ),
                OrderEntity(
                    orderCode = "GHTK-98423",
                    sequenceNumber = 3,
                    customerPhone = "0901234567",
                    customerName = "Lê Hoàng Nam",
                    address = "78 Cầu Giấy, Quan Hoa, Cầu Giấy, Hà Nội",
                    codAmount = 520000.0,
                    serviceType = "Hỏa Tốc",
                    excelStatus = "Đã phân phối",
                    goodsDescription = "Tai nghe Bluetooth không dây",
                    status = OrderStatus.DELIVERING,
                    paymentMethod = PaymentMethod.NONE,
                    proofImageUri = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600",
                    deliveryNote = "Gọi trước 5 phút khi đến",
                    latitude = 21.034500,
                    longitude = 105.795000,
                    createdAt = System.currentTimeMillis() - 5000000
                ),
                OrderEntity(
                    orderCode = "GHTK-98424",
                    sequenceNumber = 4,
                    customerPhone = "0934567890",
                    customerName = "Phạm Thu Hương",
                    address = "12 Chùa Bộc, Đống Đa, Hà Nội",
                    codAmount = 180000.0,
                    serviceType = "Giao Tiết Kiệm",
                    excelStatus = "Xuất kho giao",
                    goodsDescription = "Váy đầm dạ hội nữ",
                    status = OrderStatus.NEW_CUSTOMER,
                    paymentMethod = PaymentMethod.NONE,
                    deliveryNote = "Khách mới chưa có toạ độ định vị",
                    latitude = 21.008000,
                    longitude = 105.829000,
                    createdAt = System.currentTimeMillis() - 3600000
                ),
                OrderEntity(
                    orderCode = "GHTK-98425",
                    sequenceNumber = 5,
                    customerPhone = "0978901234",
                    customerName = "Hoàng Đức Long",
                    address = "99 Giải Phóng, Hai Bà Trưng, Hà Nội",
                    codAmount = 650000.0,
                    serviceType = "Giao Nhanh",
                    excelStatus = "Chờ giao",
                    goodsDescription = "Giày thể thao Nam size 42",
                    status = OrderStatus.DELIVERED,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    proofImageUri = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600",
                    deliveryNote = "Đã nhận qua VietQR Vietcombank",
                    latitude = 20.999500,
                    longitude = 105.842000,
                    createdAt = System.currentTimeMillis() - 10800000,
                    deliveredAt = System.currentTimeMillis() - 1800000
                ),
                OrderEntity(
                    orderCode = "GHTK-98426",
                    sequenceNumber = 6,
                    customerPhone = "0967890123",
                    customerName = "Vũ Thúy Hằng",
                    address = "55 Phố Huế, Hoàn Kiếm, Hà Nội",
                    codAmount = 420000.0,
                    serviceType = "Tiêu Chuẩn",
                    excelStatus = "Chờ giao",
                    goodsDescription = "Túi xách da nữ cao cấp",
                    status = OrderStatus.DELIVERED,
                    paymentMethod = PaymentMethod.CASH,
                    proofImageUri = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600",
                    deliveryNote = "Đã thu tiền mặt đủ",
                    latitude = 21.018500,
                    longitude = 105.852000,
                    createdAt = System.currentTimeMillis() - 12000000,
                    deliveredAt = System.currentTimeMillis() - 3600000
                )
            )
            orderDao.insertOrders(sampleOrders)
        }
    }

    suspend fun insertCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun getCustomerByPhone(phone: String): CustomerEntity? = withContext(Dispatchers.IO) {
        customerDao.getCustomerByPrimaryPhone(phone)
    }

    suspend fun getAllCustomersList(): List<CustomerEntity> = withContext(Dispatchers.IO) {
        customerDao.getAllCustomersList()
    }

    suspend fun insertOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun insertOrders(orders: List<OrderEntity>) = withContext(Dispatchers.IO) {
        orderDao.insertOrders(orders)
    }

    suspend fun replaceOrders(newOrders: List<OrderEntity>) = withContext(Dispatchers.IO) {
        orderDao.clearAllOrders()
        orderDao.insertOrders(newOrders)
    }

    suspend fun updateOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.deleteOrder(order)
    }

    suspend fun updateOrderStatus(
        orderCode: String,
        status: OrderStatus,
        paymentMethod: PaymentMethod = PaymentMethod.NONE,
        proofImageUri: String? = null,
        failureReason: String = "",
        deliveryNote: String = ""
    ) = withContext(Dispatchers.IO) {
        val existing = orderDao.getOrderByCode(orderCode)
        if (existing != null) {
            val updated = existing.copy(
                status = status,
                paymentMethod = if (status == OrderStatus.DELIVERED) paymentMethod else PaymentMethod.NONE,
                proofImageUri = proofImageUri ?: existing.proofImageUri,
                failureReason = failureReason,
                deliveryNote = if (deliveryNote.isNotBlank()) deliveryNote else existing.deliveryNote,
                deliveredAt = if (status == OrderStatus.DELIVERED) System.currentTimeMillis() else null
            )
            orderDao.updateOrder(updated)

            // If proof image was taken and it's delivered, also associate photo with CustomerEntity in Room DB
            if (proofImageUri != null && status == OrderStatus.DELIVERED) {
                val customer = customerDao.getCustomerByPrimaryPhone(existing.customerPhone)
                if (customer != null) {
                    val updatedImages = customer.imageUris.toMutableList()
                    if (!updatedImages.contains(proofImageUri)) {
                        updatedImages.add(0, proofImageUri)
                        customerDao.updateCustomer(customer.copy(imageUris = updatedImages, updatedAt = System.currentTimeMillis()))
                    }
                }
            }
        }
    }

    suspend fun saveBankSettings(
        bankName: String,
        bankBin: String,
        bankShortName: String,
        accountNumber: String,
        accountHolder: String,
        goongApiKey: String = ""
    ) = withContext(Dispatchers.IO) {
        settingsDataStore.saveBankSettings(bankName, bankBin, bankShortName, accountNumber, accountHolder, goongApiKey)
    }

    suspend fun resetDataWithSample() = withContext(Dispatchers.IO) {
        orderDao.clearAllOrders()
        initializeSampleDataIfNeeded()
    }
}
