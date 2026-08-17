package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.CustomerEntity
import com.example.model.LatLngCoord
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CustomerFormDialog(
    initialCustomer: CustomerEntity? = null,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = initialCustomer != null

    var primaryPhone by remember { mutableStateOf(initialCustomer?.primaryPhone ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }

    // Dynamic List 1: Names (List<Tên>)
    val namesList = remember {
        mutableStateListOf<String>().apply {
            if (initialCustomer != null && initialCustomer.names.isNotEmpty()) {
                addAll(initialCustomer.names)
            } else {
                add("")
            }
        }
    }

    // Dynamic List 2: Phone Numbers (List<SĐT>)
    val phonesList = remember {
        mutableStateListOf<String>().apply {
            if (initialCustomer != null && initialCustomer.phoneNumbers.isNotEmpty()) {
                addAll(initialCustomer.phoneNumbers)
            } else {
                add("")
            }
        }
    }

    // Dynamic List 3: Coordinates (List<Tọa độ>)
    val coordsList = remember {
        mutableStateListOf<LatLngCoord>().apply {
            if (initialCustomer != null && initialCustomer.coordinates.isNotEmpty()) {
                addAll(initialCustomer.coordinates)
            } else {
                add(LatLngCoord(21.028511, 105.854444, "Địa chỉ chính"))
            }
        }
    }

    // List 4: Image URIs (List<Ảnh>)
    val imagesList = remember {
        mutableStateListOf<String>().apply {
            if (initialCustomer != null) {
                addAll(initialCustomer.imageUris)
            }
        }
    }

    // Dialog state for GPS conflict: [Lưu đè] hay [Tạo mới]
    val gpsConflictPendingCoord by viewModel.gpsConflictPendingCoord.collectAsStateWithLifecycle()

    if (gpsConflictPendingCoord != null) {
        val pending = gpsConflictPendingCoord!!
        AlertDialog(
            onDismissRequest = { viewModel.setGpsConflictPendingCoord(null) },
            title = {
                Text("Đã có tọa độ vị trí", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Khách hàng đã có ${coordsList.size} tọa độ lưu trong hệ thống.\nBạn muốn [Lưu đè] vào vị trí đầu tiên hay [Tạo mới] thêm 1 tọa độ nữa?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Tạo mới
                        coordsList.add(pending.copy(label = "Tọa độ GPS mới"))
                        viewModel.setGpsConflictPendingCoord(null)
                        viewModel.showMessage("Đã thêm mới tọa độ vào danh sách!")
                    }
                ) {
                    Text("Tạo mới (+)", fontWeight = FontWeight.Bold, color = DeliveryOrange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Lưu đè
                        if (coordsList.isNotEmpty()) {
                            coordsList[0] = pending.copy(label = "Tọa độ cập nhật")
                        } else {
                            coordsList.add(pending)
                        }
                        viewModel.setGpsConflictPendingCoord(null)
                        viewModel.showMessage("Đã lưu đè tọa độ GPS hiện tại!")
                    }
                ) {
                    Text("Lưu đè", fontWeight = FontWeight.Bold, color = AccentRed)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Person else Icons.Default.Add,
                            contentDescription = null,
                            tint = DeliveryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isEditMode) "Chỉnh sửa khách hàng" else "Thêm khách hàng mới",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SĐT chính (PK)
                    OutlinedTextField(
                        value = primaryPhone,
                        onValueChange = {
                            primaryPhone = it
                            if (phonesList.isNotEmpty() && phonesList[0].isBlank()) {
                                phonesList[0] = it
                            }
                        },
                        label = { Text("SĐT chính (Primary Key) *") },
                        placeholder = { Text("0912345678") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_primary_phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        enabled = !isEditMode, // Primary Key immutable on edit
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DeliveryOrange) }
                    )

                    // 1. DYNAMIC LIST: List<Tên> với nút (+) và (-)
                    // RÀNG BUỘC UI: Nút (-) bị vô hiệu hóa khi danh sách chỉ còn 1 item (giữ tối thiểu 1 Tên)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Danh sách Tên (List<Tên>)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                FilledTonalButton(
                                    onClick = { namesList.add("") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("add_name_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thêm tên", fontSize = 11.sp)
                                }
                            }

                            namesList.forEachIndexed { index, name ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { namesList[index] = it },
                                        placeholder = { Text("Tên gọi / Biệt danh ${index + 1}") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    // Nút (-) xóa dòng - Bị vô hiệu hóa khi chỉ còn 1 item!
                                    FilledTonalIconButton(
                                        onClick = {
                                            if (namesList.size > 1) {
                                                namesList.removeAt(index)
                                            }
                                        },
                                        enabled = namesList.size > 1, // RÀNG BUỘC UI
                                        modifier = Modifier.size(38.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = AccentRed.copy(alpha = 0.15f),
                                            contentColor = AccentRed,
                                            disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                                            disabledContentColor = Color.Gray
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Xóa dòng tên")
                                    }
                                }
                            }
                        }
                    }

                    // 2. DYNAMIC LIST: List<SĐT> với nút (+) và (-)
                    // RÀNG BUỘC UI: Nút (-) bị vô hiệu hóa khi danh sách chỉ còn 1 item (giữ tối thiểu 1 SĐT)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Danh sách SĐT phụ (List<SĐT>)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                FilledTonalButton(
                                    onClick = { phonesList.add("") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("add_phone_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thêm SĐT", fontSize = 11.sp)
                                }
                            }

                            phonesList.forEachIndexed { index, phone ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phonesList[index] = it },
                                        placeholder = { Text("Số điện thoại ${index + 1}") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    // Nút (-) xóa dòng - Bị vô hiệu hóa khi chỉ còn 1 item!
                                    FilledTonalIconButton(
                                        onClick = {
                                            if (phonesList.size > 1) {
                                                phonesList.removeAt(index)
                                            }
                                        },
                                        enabled = phonesList.size > 1, // RÀNG BUỘC UI
                                        modifier = Modifier.size(38.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = AccentRed.copy(alpha = 0.15f),
                                            contentColor = AccentRed,
                                            disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                                            disabledContentColor = Color.Gray
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Xóa dòng SĐT")
                                    }
                                }
                            }
                        }
                    }

                    // 3. DYNAMIC LIST: List<Tọa độ> + NÚT "Lấy tọa độ hiện tại (📍)"
                    // RÀNG BUỘC UI: Nút (-) bị vô hiệu hóa khi danh sách chỉ còn 1 item (giữ tối thiểu 1 Tọa độ)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tọa độ giao hàng (List<Tọa độ>)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // NÚT "Lấy tọa độ hiện tại (📍)" Dùng FusedLocationProviderClient
                                Button(
                                    onClick = {
                                        viewModel.fetchGpsForCustomer(coordsList) { newCoord ->
                                            if (coordsList.isEmpty()) {
                                                coordsList.add(newCoord)
                                            } else {
                                                coordsList[0] = newCoord
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0284C7),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("get_gps_button")
                                ) {
                                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lấy tọa độ (📍)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            coordsList.forEachIndexed { index, coord ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedTextField(
                                                value = coord.label,
                                                onValueChange = { newLabel ->
                                                    coordsList[index] = coord.copy(label = newLabel)
                                                },
                                                placeholder = { Text("Tên điểm/Nhãn (VD: Cổng chính)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            Text(
                                                text = "Lat: ${coord.lat} | Lng: ${coord.lng}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0284C7)
                                            )
                                        }

                                        // Nút (-) xóa dòng - Bị vô hiệu hóa khi chỉ còn 1 item!
                                        FilledTonalIconButton(
                                            onClick = {
                                                if (coordsList.size > 1) {
                                                    coordsList.removeAt(index)
                                                }
                                            },
                                            enabled = coordsList.size > 1, // RÀNG BUỘC UI
                                            modifier = Modifier.size(36.dp),
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = AccentRed.copy(alpha = 0.15f),
                                                contentColor = AccentRed,
                                                disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                                                disabledContentColor = Color.Gray
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Xóa tọa độ")
                                        }
                                    }
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    coordsList.add(LatLngCoord(21.028511, 105.854444, "Điểm phụ ${coordsList.size + 1}"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm dòng tọa độ", fontSize = 11.sp)
                            }
                        }
                    }

                    // 4. List<Ảnh>
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Hình ảnh nhà / cổng (List<Ảnh>)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                FilledTonalButton(
                                    onClick = {
                                        val samplePool = listOf(
                                            "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600",
                                            "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=600",
                                            "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600"
                                        )
                                        imagesList.add(samplePool.random())
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thêm ảnh", fontSize = 11.sp)
                                }
                            }

                            if (imagesList.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(imagesList) { uri ->
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            IconButton(
                                                onClick = { imagesList.remove(uri) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(22.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Xóa ảnh", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Ghi chú
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Ghi chú đặc biệt") },
                        placeholder = { Text("Ví dụ: Gửi lễ tân tầng 1, gọi trước khi đến...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Save Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            if (primaryPhone.isBlank()) {
                                viewModel.showMessage("Vui lòng nhập Số điện thoại chính", isError = true)
                                return@Button
                            }

                            val finalNames = namesList.filter { it.isNotBlank() }.ifEmpty { listOf("Khách hàng $primaryPhone") }
                            val finalPhones = phonesList.filter { it.isNotBlank() }.ifEmpty { listOf(primaryPhone) }
                            val finalCoords = coordsList.ifEmpty { listOf(LatLngCoord(21.028511, 105.854444, "Địa chỉ mặc định")) }

                            val entity = CustomerEntity(
                                primaryPhone = primaryPhone.trim(),
                                names = finalNames,
                                phoneNumbers = finalPhones,
                                imageUris = imagesList.toList(),
                                coordinates = finalCoords,
                                notes = notes.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.saveCustomer(entity)
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeliveryOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lưu Khách Hàng", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
