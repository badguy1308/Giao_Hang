package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.OrderEntity
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SmsPurple
import com.example.ui.theme.ZaloBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters
import com.example.util.IntentsHelper

@Composable
fun OrderTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sortedOrders by viewModel.sortedFilteredOrders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val bankSettings by viewModel.bankSettings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP SEARCH & SCAN BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Real-time Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Tìm tên, SĐT, mã đơn, địa chỉ...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = DeliveryOrange
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Xóa tìm kiếm",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeliveryOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    // Quét QR/Barcode bằng CameraX
                    FilledIconButton(
                        onClick = { viewModel.setCameraScannerOpen(true) },
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("scan_barcode_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = DeliveryOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Quét mã QR/Barcode",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Top summary: Only show order counts (Bỏ dòng chữ thứ tự)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val pendingCount = sortedOrders.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.FAILED }
                    val deliveredCount = sortedOrders.count { it.status == OrderStatus.DELIVERED }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = DeliveryOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Cần giao: $pendingCount đơn",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeliveryOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (deliveredCount > 0) {
                            Surface(
                                color = AccentGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Đã giao: $deliveredCount đơn",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Tổng: ${sortedOrders.size} đơn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // ORDER CARDS LIST
        if (sortedOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Không tìm thấy đơn hàng phù hợp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { viewModel.setSearchQuery("") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Hiển thị tất cả đơn hàng")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedOrders, key = { it.orderCode }) { order ->
                    OrderCardItem(
                        order = order,
                        onCall = { IntentsHelper.makePhoneCall(context, order.customerPhone) },
                        onZalo = { IntentsHelper.openZalo(context, order.customerPhone) },
                        onSms = {
                            IntentsHelper.sendOrderSmsWithBankInfo(
                                context = context,
                                customerPhone = order.customerPhone,
                                customerName = order.customerName,
                                orderCode = order.orderCode,
                                codAmount = order.codAmount,
                                bankShortName = bankSettings.bankShortName,
                                accountNumber = bankSettings.accountNumber,
                                accountHolder = bankSettings.accountHolder
                            )
                        },
                        onCapturePhoto = { viewModel.setPhotoCaptureForOrder(order) },
                        onDelivered = { viewModel.setDeliverConfirmOrder(order) },
                        onFailed = { viewModel.setFailConfirmOrder(order) },
                        onRetry = { viewModel.markOrderRetry(order.orderCode) },
                        onViewImage = { uri -> viewModel.setFullscreenImageUri(uri) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCardItem(
    order: OrderEntity,
    onCall: () -> Unit,
    onZalo: () -> Unit,
    onSms: () -> Unit,
    onCapturePhoto: () -> Unit,
    onDelivered: () -> Unit,
    onFailed: () -> Unit,
    onRetry: () -> Unit,
    onViewImage: (String) -> Unit
) {
    val statusColor = when (order.status) {
        OrderStatus.DELIVERED -> AccentGreen
        OrderStatus.FAILED -> AccentRed
        OrderStatus.NEW_CUSTOMER -> Color(0xFF0284C7)
        OrderStatus.RETRY -> AccentAmber
        OrderStatus.DELIVERING -> DeliveryOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderCode}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Badge STT / Trạng thái, Mã vận đơn
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // STT Badge: Only shown for active orders. For delivered/failed, cancel STT badge.
                    if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.FAILED) {
                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "STT #${order.sequenceNumber}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (order.status == OrderStatus.DELIVERED) "✓ ĐÃ GIAO" else "✕ THẤT BẠI",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Mã vận đơn
                    Text(
                        text = order.orderCode,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Trạng thái Badge
                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Row 2: Customer Name, Phone, Address
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "${order.customerName} • ${order.customerPhone}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "📍 ${order.address}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                // Chi tiết từ Excel: Hàng hoá, Dịch vụ đơn, Trạng thái từ file Excel
                if (order.goodsDescription.isNotBlank()) {
                    Text(
                        text = "📦 Hàng hóa: ${order.goodsDescription}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (order.serviceType.isNotBlank()) {
                        Text(
                            text = "🚚 Dịch vụ: ${order.serviceType}",
                            fontSize = 11.sp,
                            color = DeliveryOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (order.excelStatus.isNotBlank()) {
                        Text(
                            text = "📋 File Excel: ${order.excelStatus}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                if (order.deliveryNote.isNotBlank()) {
                    Text(
                        text = "📝 Ghi chú: ${order.deliveryNote}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (order.failureReason.isNotBlank() && order.status == OrderStatus.FAILED) {
                    Surface(
                        color = AccentRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Lý do thất bại đã lưu: ${order.failureReason}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Row 3: KHUNG VIỀN ĐỎ TIỀN COD (Nổi bật)
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, AccentRed),
                color = AccentRed.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TIỀN COD PHẢI THU:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                    Text(
                        text = Formatters.formatCurrency(order.codAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentRed
                    )
                }
            }

            // Attached Photo Preview if any
            if (order.proofImageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onViewImage(order.proofImageUri) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(order.proofImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ảnh đơn hàng",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ảnh xác nhận đã lưu trong Room DB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                        Text(
                            text = "Chạm để xem ảnh toàn màn hình",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Row 4: 3 NÚT (Gọi / Zalo / SMS) + Nút Chụp Ảnh Đơn
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Gọi Button
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = CallGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gọi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 2. Zalo Button
                Button(
                    onClick = onZalo,
                    colors = ButtonDefaults.buttonColors(containerColor = ZaloBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Zalo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 3. SMS Button
                Button(
                    onClick = onSms,
                    colors = ButtonDefaults.buttonColors(containerColor = SmsPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 4. Nút Chụp ảnh đơn đính kèm mã
                FilledTonalIconButton(
                    onClick = onCapturePhoto,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = DeliveryOrange.copy(alpha = 0.15f),
                        contentColor = DeliveryOrange
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Chụp ảnh xác nhận",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 5: NÚT TRẠNG THÁI (ĐÃ GIAO / GIAO THẤT BẠI / GIAO LẠI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status == OrderStatus.FAILED) {
                    // Khi đơn thất bại: Hiện nút GIAO LẠI và nút ĐÃ GIAO
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentAmber,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GIAO LẠI", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    OutlinedButton(
                        onClick = onDelivered,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentGreen
                        ),
                        border = BorderStroke(1.5.dp, AccentGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ĐÃ GIAO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (order.status == OrderStatus.DELIVERED) {
                    // Khi đơn đã giao thành công
                    Surface(
                        color = AccentGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GIAO THÀNH CÔNG", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AccentGreen)
                        }
                    }

                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.weight(0.9f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("GIAO LẠI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Trạng thái đang giao / khách mới: Chỉ hiện ĐÃ GIAO và THẤT BẠI (Nút giao lại chỉ hiện khi bấm vào thất bại)
                    Button(
                        onClick = onDelivered,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ĐÃ GIAO", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = onFailed,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("THẤT BẠI", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
