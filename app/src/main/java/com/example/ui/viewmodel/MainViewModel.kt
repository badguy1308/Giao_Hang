package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.BankSettings
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.CustomerEntity
import com.example.data.local.OrderEntity
import com.example.data.repository.CodReconciliation
import com.example.data.repository.DeliveryRepository
import com.example.model.ExcelOrderRow
import com.example.model.LatLngCoord
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.util.ExcelHelper
import com.example.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    MAP("BẢN ĐỒ"),
    ORDERS("CHI TIẾT ĐƠN HÀNG"),
    CUSTOMERS("KHÁCH HÀNG")
}

data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val settingsDataStore = SettingsDataStore(application)
    private val locationHelper = LocationHelper(application)
    val repository = DeliveryRepository(db, settingsDataStore, application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.MAP)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Search Query for real-time search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Current User GPS Location
    private val _currentGpsLocation = MutableStateFlow(locationHelper.defaultLocation)
    val currentGpsLocation: StateFlow<LatLngCoord> = _currentGpsLocation.asStateFlow()

    // Selected order on Map Marker click (Popup ảnh nhỏ)
    private val _markerPopupOrder = MutableStateFlow<OrderEntity?>(null)
    val markerPopupOrder: StateFlow<OrderEntity?> = _markerPopupOrder.asStateFlow()

    // Fullscreen Image Viewer
    private val _fullscreenImageUri = MutableStateFlow<String?>(null)
    val fullscreenImageUri: StateFlow<String?> = _fullscreenImageUri.asStateFlow()

    // Dialogs & Sheets
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _isExcelDialogOpen = MutableStateFlow(false)
    val isExcelDialogOpen: StateFlow<Boolean> = _isExcelDialogOpen.asStateFlow()

    private val _isCodReconDialogOpen = MutableStateFlow(false)
    val isCodReconDialogOpen: StateFlow<Boolean> = _isCodReconDialogOpen.asStateFlow()

    private val _isBankSettingsDialogOpen = MutableStateFlow(false)
    val isBankSettingsDialogOpen: StateFlow<Boolean> = _isBankSettingsDialogOpen.asStateFlow()

    private val _isAddCustomerDialogOpen = MutableStateFlow(false)
    val isAddCustomerDialogOpen: StateFlow<Boolean> = _isAddCustomerDialogOpen.asStateFlow()

    private val _editingCustomer = MutableStateFlow<CustomerEntity?>(null)
    val editingCustomer: StateFlow<CustomerEntity?> = _editingCustomer.asStateFlow()

    private val _viewingCustomerHistory = MutableStateFlow<CustomerEntity?>(null)
    val viewingCustomerHistory: StateFlow<CustomerEntity?> = _viewingCustomerHistory.asStateFlow()

    private val _customerToDelete = MutableStateFlow<CustomerEntity?>(null)
    val customerToDelete: StateFlow<CustomerEntity?> = _customerToDelete.asStateFlow()

    private val _isCameraScannerOpen = MutableStateFlow(false)
    val isCameraScannerOpen: StateFlow<Boolean> = _isCameraScannerOpen.asStateFlow()

    private val _photoCaptureForOrder = MutableStateFlow<OrderEntity?>(null)
    val photoCaptureForOrder: StateFlow<OrderEntity?> = _photoCaptureForOrder.asStateFlow()

    private val _deliverConfirmOrder = MutableStateFlow<OrderEntity?>(null)
    val deliverConfirmOrder: StateFlow<OrderEntity?> = _deliverConfirmOrder.asStateFlow()

    private val _failConfirmOrder = MutableStateFlow<OrderEntity?>(null)
    val failConfirmOrder: StateFlow<OrderEntity?> = _failConfirmOrder.asStateFlow()

    // GPS Conflict Dialog: [Lưu đè] hay [Tạo mới]
    private val _gpsConflictPendingCoord = MutableStateFlow<LatLngCoord?>(null)
    val gpsConflictPendingCoord: StateFlow<LatLngCoord?> = _gpsConflictPendingCoord.asStateFlow()

    // Excel preview list
    private val _excelPreviewRows = MutableStateFlow<List<ExcelOrderRow>>(emptyList())
    val excelPreviewRows: StateFlow<List<ExcelOrderRow>> = _excelPreviewRows.asStateFlow()

    // Toast / snackbar messages
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    // Bank settings
    val bankSettings: StateFlow<BankSettings> = repository.bankSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BankSettings()
    )

    // All raw orders
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrdersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All raw customers
    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Orders sorted according to specification:
    // Khách mới (1) ➔ Đang giao theo STT (2) ➔ Đã giao/Thất bại (3,4,5)
    val sortedFilteredOrders: StateFlow<List<OrderEntity>> = combine(
        allOrders,
        searchQuery
    ) { orders, query ->
        val filtered = if (query.isBlank()) {
            orders
        } else {
            orders.filter {
                it.orderCode.contains(query, ignoreCase = true) ||
                it.customerName.contains(query, ignoreCase = true) ||
                it.customerPhone.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
            }
        }

        filtered.sortedWith(
            compareBy<OrderEntity> { it.status.sortPriority }
                .thenBy { it.sequenceNumber }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 3 Closest Orders for Bottom Sheet in Map Tab
    val closestThreeOrders: StateFlow<List<Pair<OrderEntity, Double>>> = combine(
        allOrders,
        _currentGpsLocation
    ) { orders, gps ->
        val activeOrders = orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.FAILED }
        val targetOrders = if (activeOrders.isNotEmpty()) activeOrders else orders

        targetOrders.map { order ->
            val dist = LocationHelper.calculateDistanceMeters(
                gps.lat, gps.lng, order.latitude, order.longitude
            )
            Pair(order, dist)
        }.sortedBy { it.second }.take(3)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // COD Reconciliation summary
    val codReconciliation: StateFlow<CodReconciliation> = allOrders.map { orders ->
        val total = orders.size
        val delivered = orders.filter { it.status == OrderStatus.DELIVERED }
        val pending = orders.filter { it.status == OrderStatus.DELIVERING || it.status == OrderStatus.NEW_CUSTOMER || it.status == OrderStatus.RETRY }
        val failed = orders.filter { it.status == OrderStatus.FAILED }

        val totalCod = orders.sumOf { it.codAmount }
        val cash = delivered.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.codAmount }
        val bank = delivered.filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER }.sumOf { it.codAmount }
        val totalCol = cash + bank
        val pendingCod = pending.sumOf { it.codAmount }

        CodReconciliation(
            totalOrders = total,
            deliveredCount = delivered.size,
            pendingCount = pending.size,
            failedCount = failed.size,
            totalCodAmount = totalCod,
            cashCollected = cash,
            bankCollected = bank,
            totalCollected = totalCol,
            pendingCodAmount = pendingCod
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CodReconciliation(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    init {
        viewModelScope.launch {
            repository.initializeSampleDataIfNeeded()
            refreshCurrentGpsLocation()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDrawerOpen(open: Boolean) {
        _isDrawerOpen.value = open
    }

    fun setMarkerPopupOrder(order: OrderEntity?) {
        _markerPopupOrder.value = order
    }

    fun setFullscreenImageUri(uri: String?) {
        _fullscreenImageUri.value = uri
    }

    fun setExcelDialogOpen(open: Boolean) {
        _isExcelDialogOpen.value = open
        if (open && _excelPreviewRows.value.isEmpty()) {
            loadSampleExcelRows()
        }
    }

    fun setCodReconDialogOpen(open: Boolean) {
        _isCodReconDialogOpen.value = open
    }

    fun setBankSettingsDialogOpen(open: Boolean) {
        _isBankSettingsDialogOpen.value = open
    }

    fun setAddCustomerDialogOpen(open: Boolean) {
        _isAddCustomerDialogOpen.value = open
    }

    fun setEditingCustomer(customer: CustomerEntity?) {
        _editingCustomer.value = customer
    }

    fun setViewingCustomerHistory(customer: CustomerEntity?) {
        _viewingCustomerHistory.value = customer
    }

    fun setCustomerToDelete(customer: CustomerEntity?) {
        _customerToDelete.value = customer
    }

    fun setCameraScannerOpen(open: Boolean) {
        _isCameraScannerOpen.value = open
    }

    fun setPhotoCaptureForOrder(order: OrderEntity?) {
        _photoCaptureForOrder.value = order
    }

    fun setDeliverConfirmOrder(order: OrderEntity?) {
        _deliverConfirmOrder.value = order
    }

    fun setFailConfirmOrder(order: OrderEntity?) {
        _failConfirmOrder.value = order
    }

    fun setGpsConflictPendingCoord(coord: LatLngCoord?) {
        _gpsConflictPendingCoord.value = coord
    }

    fun showMessage(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(text = msg, isError = isError)
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun refreshCurrentGpsLocation() {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            _currentGpsLocation.value = loc
        }
    }

    // Order status actions
    fun markOrderDelivered(orderCode: String, paymentMethod: PaymentMethod, proofImage: String? = null, note: String = "") {
        viewModelScope.launch {
            repository.updateOrderStatus(
                orderCode = orderCode,
                status = OrderStatus.DELIVERED,
                paymentMethod = paymentMethod,
                proofImageUri = proofImage,
                deliveryNote = note
            )
            _deliverConfirmOrder.value = null
            showMessage("Đã cập nhật: GIAO THÀNH CÔNG ($orderCode)")
        }
    }

    fun markOrderFailed(orderCode: String, reason: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(
                orderCode = orderCode,
                status = OrderStatus.FAILED,
                failureReason = reason
            )
            _failConfirmOrder.value = null
            showMessage("Đã cập nhật: GIAO THẤT BẠI ($orderCode)", isError = true)
        }
    }

    fun markOrderRetry(orderCode: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(
                orderCode = orderCode,
                status = OrderStatus.RETRY
            )
            showMessage("Đã chuyển sang trạng thái: GIAO LẠI ($orderCode)")
        }
    }

    fun saveProofPhotoForOrder(orderCode: String, photoUri: String) {
        viewModelScope.launch {
            val order = allOrders.value.firstOrNull { it.orderCode == orderCode }
            if (order != null) {
                repository.updateOrder(order.copy(proofImageUri = photoUri))
                showMessage("Đã lưu ảnh đơn hàng $orderCode")
            }
            _photoCaptureForOrder.value = null
        }
    }

    // Customer CRUD
    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
            _isAddCustomerDialogOpen.value = false
            _editingCustomer.value = null
            showMessage("Đã lưu thông tin khách hàng: ${customer.names.firstOrNull() ?: customer.primaryPhone}")
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _customerToDelete.value = null
            _editingCustomer.value = null
            showMessage("Đã xóa khách hàng: ${customer.primaryPhone}")
        }
    }

    // GPS Fetch for Customer
    fun fetchGpsForCustomer(
        existingCoords: List<LatLngCoord>,
        onDirectSave: (LatLngCoord) -> Unit
    ) {
        viewModelScope.launch {
            val gps = locationHelper.getCurrentLocation()
            if (existingCoords.isEmpty()) {
                onDirectSave(gps)
                showMessage("Đã lưu tọa độ GPS hiện tại!")
            } else {
                // Show dialog [Lưu đè] hay [Tạo mới]
                _gpsConflictPendingCoord.value = gps
            }
        }
    }

    // Excel Preview & Import
    fun loadExcelFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val existing = repository.getAllCustomersList()
            val rows = ExcelHelper.parseExcelFileUri(getApplication(), uri, existing)
            _excelPreviewRows.value = rows
            if (rows.isNotEmpty()) {
                showMessage("Đã phân tích thành công ${rows.size} đơn hàng từ tệp Excel!")
            } else {
                showMessage("Không tìm thấy dòng dữ liệu hợp lệ trong tệp Excel", isError = true)
            }
        }
    }

    fun loadSampleExcelRows() {
        viewModelScope.launch {
            val existing = repository.getAllCustomersList()
            val rows = ExcelHelper.parseCsvContent(ExcelHelper.SAMPLE_EXCEL_CSV, existing)
            _excelPreviewRows.value = rows
        }
    }

    fun parseCustomExcelText(content: String) {
        viewModelScope.launch {
            val existing = repository.getAllCustomersList()
            val rows = ExcelHelper.parseCsvContent(content, existing)
            _excelPreviewRows.value = rows
            showMessage("Đã tải ${rows.size} đơn hàng từ dữ liệu Excel!")
        }
    }

    fun importExcelRowsToOrders() {
        viewModelScope.launch {
            val rows = _excelPreviewRows.value
            if (rows.isEmpty()) {
                showMessage("Không có dữ liệu đơn hàng để nhập", isError = true)
                return@launch
            }
            val orders = ExcelHelper.convertRowsToOrderEntities(rows)
            repository.replaceOrders(orders)
            _isExcelDialogOpen.value = false
            _excelPreviewRows.value = emptyList()
            showMessage("Đã nhập thành công ${orders.size} đơn hàng mới và xóa danh sách đơn cũ!")
        }
    }

    fun saveBankSettings(
        bankName: String,
        bankBin: String,
        bankShortName: String,
        accountNumber: String,
        accountHolder: String
    ) {
        viewModelScope.launch {
            repository.saveBankSettings(bankName, bankBin, bankShortName, accountNumber, accountHolder)
            _isBankSettingsDialogOpen.value = false
            showMessage("Đã lưu cài đặt số tài khoản ngân hàng!")
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetDataWithSample()
            showMessage("Đã khôi phục dữ liệu mẫu!")
        }
    }
}

private fun <T, R> StateFlow<T>.map(transform: (T) -> R): kotlinx.coroutines.flow.Flow<R> {
    val upstream = this
    return kotlinx.coroutines.flow.flow {
        upstream.collect { value ->
            emit(transform(value))
        }
    }
}
