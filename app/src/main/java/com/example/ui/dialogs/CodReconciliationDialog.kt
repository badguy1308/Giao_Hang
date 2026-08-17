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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters

@Composable
fun CodReconciliationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

    // Calculated total COD from orders in app (for default prefill)
    val calculatedCodFromOrders = remember(allOrders) {
        allOrders.sumOf { it.codAmount }
    }

    // State for denomination bill counts (500k, 200k, 100k, 50k, 20k, 10k)
    val billCounts = remember {
        mutableStateMapOf(
            500_000 to 0,
            200_000 to 0,
            100_000 to 0,
            50_000 to 0,
            20_000 to 0,
            10_000 to 0
        )
    }

    // State for Bank amount input (user manually enters)
    var bankAmountInput by remember { mutableStateOf("") }

    // State for Total COD input (user manually enters, default initialized to calculated COD from orders if present)
    var codAmountInput by remember(calculatedCodFromOrders) {
        mutableStateOf(if (calculatedCodFromOrders > 0) calculatedCodFromOrders.toLong().toString() else "")
    }

    val bankAmount = remember(bankAmountInput) {
        bankAmountInput.replace(".", "").replace(",", "").replace(" ", "").toDoubleOrNull() ?: 0.0
    }

    val totalCod = remember(codAmountInput) {
        codAmountInput.replace(".", "").replace(",", "").replace(" ", "").toDoubleOrNull() ?: 0.0
    }

    // Calculate Total Cash
    val totalCash = remember(billCounts.values.toList()) {
        billCounts.entries.sumOf { (denomination, count) ->
            (denomination * count).toDouble()
        }
    }

    // Tiền thực tế = Tổng tiền mặt + Tiền trong bank
    val totalActualMoney = totalCash + bankAmount

    // Đối soát COD: Chênh lệch = Tiền thực tế - Tổng COD nhập tay
    val diff = totalActualMoney - totalCod

    val isShortage = diff < -0.01 // Tiền thực tế ít hơn COD -> Thiếu
    val isSurplus = diff > 0.01   // Tiền thực tế nhiều hơn COD -> Dư

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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = DeliveryOrange,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                text = "Bảng Kê Tiền & Đối Soát COD",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kiểm đếm mệnh giá • Tiền mặt • Tiền Bank • COD",
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

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // SECTION 1: KẾT QUẢ ĐỐI SOÁT COD (HIGHLIGHT CARD)
                    Surface(
                        color = when {
                            isShortage -> AccentRed // Nền Đỏ ghi chữ 'thiếu'
                            isSurplus -> AccentGreen // Nền Xanh ghi chữ 'dư'
                            else -> AccentGreen // Nền Xanh khớp
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isShortage) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = when {
                                        isShortage -> "KẾT QUẢ: THIẾU TIỀN"
                                        isSurplus -> "KẾT QUẢ: DƯ TIỀN"
                                        else -> "KẾT QUẢ: ĐỐI SOÁT KHỚP"
                                    },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = when {
                                    isShortage -> "Thiếu: ${Formatters.formatCurrency(kotlin.math.abs(diff))}"
                                    isSurplus -> "Dư: +${Formatters.formatCurrency(diff)}"
                                    else -> "ĐỐI SOÁT KHỚP (0 đ)"
                                },
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Thực tế (${Formatters.formatCurrency(totalActualMoney)}) - Tổng COD (${Formatters.formatCurrency(totalCod)})",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 2: BẢNG KÊ TIỀN CÁC MỆNH GIÁ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BẢNG KÊ TIỀN MẶT THEO MỆNH GIÁ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    billCounts.keys.forEach { billCounts[it] = 0 }
                                }
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đặt lại về 0", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Denomination Rows (500k, 200k, 100k, 50k, 20k, 10k)
                    val denominations = listOf(500_000, 200_000, 100_000, 50_000, 20_000, 10_000)
                    denominations.forEach { denom ->
                        val count = billCounts[denom] ?: 0
                        val rowTotal = (denom * count).toDouble()

                        DenominationInputRow(
                            denomination = denom,
                            count = count,
                            total = rowTotal,
                            onCountChange = { newCount ->
                                billCounts[denom] = newCount.coerceAtLeast(0)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 3: TỔNG KẾT TÀI CHÍNH
                    Text(
                        text = "TỔNG HỢP TIỀN THỰC TẾ & COD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Tổng tiền mặt
                    SummaryInfoRow(
                        icon = Icons.Default.Payments,
                        iconTint = DeliveryOrange,
                        label = "Tổng tiền mặt:",
                        subLabel = "Tổng từ bảng kê các mệnh giá",
                        valueText = Formatters.formatCurrency(totalCash),
                        valueColor = DeliveryOrange
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Tiền trong bank (Ô nhập sạch đẹp, không bị lệch/cắt chữ)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text("Tiền trong Bank:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Nhập số tiền tài khoản bank", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Clean Custom Text Box for Bank Amount
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (bankAmountInput.isBlank()) {
                                    Text(
                                        text = "0 đ",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                BasicTextField(
                                    value = bankAmountInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.length <= 12) {
                                            bankAmountInput = input
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        color = Color(0xFF0284C7),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.End
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF0284C7)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Tiền thực tế = Tiền mặt + Bank
                    SummaryInfoRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF16A34A),
                        label = "Tổng tiền thực tế:",
                        subLabel = "Tiền mặt + Tiền trong Bank",
                        valueText = Formatters.formatCurrency(totalActualMoney),
                        valueColor = Color(0xFF16A34A),
                        isHighlight = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Tổng COD (Người dùng tự nhập tay hoặc dùng số từ đơn hàng)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.PriceCheck,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text("Tổng tiền COD:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Tự nhập số tiền COD cần thu", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Custom Text Box for Manual COD Input
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, AccentRed, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (codAmountInput.isBlank()) {
                                    Text(
                                        text = "0 đ",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                BasicTextField(
                                    value = codAmountInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.length <= 12) {
                                            codAmountInput = input
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        color = AccentRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.End
                                    ),
                                    cursorBrush = SolidColor(AccentRed),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeliveryOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đóng bảng đối soát", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DenominationInputRow(
    denomination: Int,
    count: Int,
    total: Double,
    onCountChange: (Int) -> Unit
) {
    val denomLabel = when (denomination) {
        500_000 -> "500k"
        200_000 -> "200k"
        100_000 -> "100k"
        50_000 -> "50k"
        20_000 -> "20k"
        10_000 -> "10k"
        else -> "${denomination / 1000}k"
    }

    var countText by remember(count) { mutableStateOf(if (count == 0) "" else count.toString()) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Mệnh giá Badge (Rõ ràng, không bị chèn ép)
            Surface(
                color = when (denomination) {
                    500_000 -> Color(0xFF0284C7)
                    200_000 -> Color(0xFFEA580C)
                    100_000 -> Color(0xFF16A34A)
                    50_000 -> Color(0xFF9333EA)
                    20_000 -> Color(0xFF475569)
                    10_000 -> Color(0xFF64748B)
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = denomLabel,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 2. Stepper Buttons + Text Box không bị lệch/cắt chữ
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Nút Trừ (-)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            val newCount = (count - 1).coerceAtLeast(0)
                            countText = if (newCount == 0) "" else newCount.toString()
                            onCountChange(newCount)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Ô nhập số tờ (Dùng BasicTextField để canh giữa tuyệt đối, không bị lệch font hay cắt chữ)
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            if (count > 0) DeliveryOrange else Color.LightGray,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (countText.isBlank()) {
                        Text(
                            text = "0",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    BasicTextField(
                        value = countText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                countText = input
                                val num = input.toIntOrNull() ?: 0
                                onCountChange(num)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(DeliveryOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Nút Cộng (+)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            val newCount = count + 1
                            countText = newCount.toString()
                            onCountChange(newCount)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 3. Thành tiền
            Text(
                text = Formatters.formatCurrency(total),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (total > 0) DeliveryOrange else Color.Gray,
                textAlign = TextAlign.End,
                modifier = Modifier.width(95.dp)
            )
        }
    }
}

@Composable
private fun SummaryInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    subLabel: String,
    valueText: String,
    valueColor: Color,
    isHighlight: Boolean = false
) {
    Surface(
        color = if (isHighlight) valueColor.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                Column {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subLabel, fontSize = 10.sp, color = Color.Gray)
                }
            }

            Text(
                text = valueText,
                fontSize = if (isHighlight) 15.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
    }
}
