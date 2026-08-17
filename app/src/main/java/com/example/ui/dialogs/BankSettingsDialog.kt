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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SmsPurple
import com.example.ui.viewmodel.MainViewModel

@Composable
fun BankSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentSettings by viewModel.bankSettings.collectAsStateWithLifecycle()

    val popularBanks = listOf(
        "MBBank", "Vietcombank", "Techcombank", "VPBank",
        "ACB", "BIDV", "Agribank", "TPBank", "VietinBank", "Sacombank"
    )

    var selectedBank by remember { mutableStateOf(currentSettings.bankShortName) }
    var accountNumber by remember { mutableStateOf(currentSettings.accountNumber) }
    var accountHolder by remember { mutableStateOf(currentSettings.accountHolder) }

    // Live preview message for SMS
    val sampleSmsMessage = remember(selectedBank, accountNumber, accountHolder) {
        if (accountNumber.isNotBlank()) {
            "Chào anh/chị Nguyễn Văn An, em giao hàng đơn GHTK-98421. Tiền COD: 350.000 đ. Anh/chị chuyển khoản vào STK: $accountNumber - $selectedBank - Chủ TK: ${accountHolder.ifBlank { "NGUYEN VAN A" }}. Nội dung CK: GHTK-98421. Em cảm ơn ạ!"
        } else {
            "Chào anh/chị Nguyễn Văn An, em giao hàng đơn GHTK-98421. Tiền COD: 350.000 đ. Em đang đến giao hàng, anh/chị để ý điện thoại nhận hàng giúp em nhé!"
        }
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
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            tint = SmsPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Cài đặt STK gửi tin nhắn SMS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tự động điền STK & tiền COD khi bấm nút SMS",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bank Selection
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Ngân hàng:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularBanks) { bank ->
                                val isSelected = selectedBank.equals(bank, ignoreCase = true)
                                Surface(
                                    color = if (isSelected) SmsPurple else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { selectedBank = bank }
                                ) {
                                    Text(
                                        text = bank,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = { selectedBank = it },
                            label = { Text("Tên viết tắt ngân hàng") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SmsPurple)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Account Number Input
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it.replace(" ", "") },
                        label = { Text("Số tài khoản nhận tiền") },
                        placeholder = { Text("Ví dụ: 0988123456 hoặc 1903...") },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = SmsPurple)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Account Holder Name Input
                    OutlinedTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it.uppercase() },
                        label = { Text("Tên chủ tài khoản (In hoa không dấu)") },
                        placeholder = { Text("NGUYEN VAN A") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SmsPurple)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Live SMS Message Preview
                    Surface(
                        color = SmsPurple.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SmsPurple.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, tint = SmsPurple, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Xem trước nội dung SMS tự động tạo:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SmsPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = sampleSmsMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
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
                            viewModel.saveBankSettings(
                                bankName = selectedBank,
                                bankBin = "",
                                bankShortName = selectedBank,
                                accountNumber = accountNumber,
                                accountHolder = accountHolder
                            )
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SmsPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lưu cài đặt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
