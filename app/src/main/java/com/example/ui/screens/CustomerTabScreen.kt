package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.CustomerEntity
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel
import com.example.util.IntentsHelper

@Composable
fun CustomerTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val customerToDelete by viewModel.customerToDelete.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter { customer ->
        if (searchQuery.isBlank()) true
        else {
            customer.primaryPhone.contains(searchQuery, ignoreCase = true) ||
            customer.names.any { it.contains(searchQuery, ignoreCase = true) } ||
            customer.phoneNumbers.any { it.contains(searchQuery, ignoreCase = true) } ||
            customer.coordinates.any { it.label.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Dialog xác nhận XÓA Khách Hàng
    if (customerToDelete != null) {
        val target = customerToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.setCustomerToDelete(null) },
            title = {
                Text(
                    text = "Xác nhận xóa khách hàng?",
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa khách hàng: ${target.names.firstOrNull() ?: target.primaryPhone} (${target.primaryPhone}) khỏi Room DB không? Hành động này không thể hoàn tác."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCustomer(target) }
                ) {
                    Text("Xóa vĩnh viễn", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setCustomerToDelete(null) }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Stats Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm theo tên, SĐT hoặc địa chỉ...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = DeliveryOrange)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("customer_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeliveryOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Danh bạ khách hàng Room DB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${filteredCustomers.size} khách",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeliveryOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chưa có khách hàng nào",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.setAddCustomerDialogOpen(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeliveryOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thêm khách hàng đầu tiên")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.primaryPhone }) { customer ->
                        CustomerCardItem(
                            customer = customer,
                            onCall = { IntentsHelper.makePhoneCall(context, customer.primaryPhone) },
                            onEdit = { viewModel.setEditingCustomer(customer) },
                            onDelete = { viewModel.setCustomerToDelete(customer) },
                            onViewHistory = { viewModel.setViewingCustomerHistory(customer) },
                            onViewImage = { uri -> viewModel.setFullscreenImageUri(uri) }
                        )
                    }
                }
            }
        }

        // Floating Action Button: (+) Thêm khách mới
        ExtendedFloatingActionButton(
            onClick = { viewModel.setAddCustomerDialogOpen(true) },
            containerColor = DeliveryOrange,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_customer_fab"),
            icon = { Icon(Icons.Default.Add, contentDescription = "Thêm khách") },
            text = { Text("Thêm Khách Mới", fontWeight = FontWeight.Bold) }
        )
    }
}

@Composable
fun CustomerCardItem(
    customer: CustomerEntity,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit,
    onViewImage: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_card_${customer.primaryPhone}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Primary Name + Actions (Call, History, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = DeliveryOrange.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DeliveryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = customer.names.firstOrNull() ?: "Khách hàng",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "SĐT chính: ${customer.primaryPhone}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeliveryOrange
                        )
                    }
                }

                // Action Icons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Gọi
                    FilledIconButton(
                        onClick = onCall,
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CallGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Gọi khách", modifier = Modifier.size(16.dp))
                    }

                    // Sửa
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa khách", modifier = Modifier.size(16.dp))
                    }

                    // Nút Xóa khách hàng (Dialog xác nhận)
                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp).testTag("delete_customer_${customer.primaryPhone}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AccentRed.copy(alpha = 0.15f),
                            contentColor = AccentRed
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa khách hàng", modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Names List (List<Tên>)
            if (customer.names.size > 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tên phụ: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = customer.names.drop(1).joinToString(", "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Phones List (List<SĐT>)
            if (customer.phoneNumbers.size > 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SĐT phụ: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = customer.phoneNumbers.filter { it != customer.primaryPhone }.joinToString(", "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Coordinates (List<Tọa độ>)
            if (customer.coordinates.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    customer.coordinates.forEach { coord ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${coord.label.ifBlank { "Vị trí" }}: (${coord.lat}, ${coord.lng})",
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }

            // Notes
            if (customer.notes.isNotBlank()) {
                Text(
                    text = "📝 ${customer.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Customer Photos (List<Ảnh>)
            if (customer.imageUris.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    customer.imageUris.take(4).forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onViewImage(uri) }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
                                contentDescription = "Ảnh nhà khách",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // LỊCH SỬ: Xem danh sách đơn giao thành công kèm ảnh xác nhận
            Button(
                onClick = onViewHistory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("view_history_${customer.primaryPhone}"),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = DeliveryOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Xem lịch sử giao thành công & ảnh xác nhận", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
