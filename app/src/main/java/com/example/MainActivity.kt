package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.OrderStatus
import com.example.ui.components.FullscreenImageViewer
import com.example.ui.dialogs.BankSettingsDialog
import com.example.ui.dialogs.CameraScannerDialog
import com.example.ui.dialogs.CodReconciliationDialog
import com.example.ui.dialogs.CustomerFormDialog
import com.example.ui.dialogs.CustomerHistoryDialog
import com.example.ui.dialogs.DeliveryConfirmDialog
import com.example.ui.dialogs.ExcelImportDialog
import com.example.ui.dialogs.FailureConfirmDialog
import com.example.ui.dialogs.PhotoCaptureDialog
import com.example.ui.screens.CustomerTabScreen
import com.example.ui.screens.MapTabScreen
import com.example.ui.screens.OrderTabScreen
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    // Dialog state collections
    val isExcelDialogOpen by viewModel.isExcelDialogOpen.collectAsStateWithLifecycle()
    val isCodReconDialogOpen by viewModel.isCodReconDialogOpen.collectAsStateWithLifecycle()
    val isBankSettingsDialogOpen by viewModel.isBankSettingsDialogOpen.collectAsStateWithLifecycle()
    val isCameraScannerOpen by viewModel.isCameraScannerOpen.collectAsStateWithLifecycle()
    val photoCaptureForOrder by viewModel.photoCaptureForOrder.collectAsStateWithLifecycle()
    val deliverConfirmOrder by viewModel.deliverConfirmOrder.collectAsStateWithLifecycle()
    val failConfirmOrder by viewModel.failConfirmOrder.collectAsStateWithLifecycle()
    val isAddCustomerDialogOpen by viewModel.isAddCustomerDialogOpen.collectAsStateWithLifecycle()
    val editingCustomer by viewModel.editingCustomer.collectAsStateWithLifecycle()
    val viewingCustomerHistory by viewModel.viewingCustomerHistory.collectAsStateWithLifecycle()
    val fullscreenImageUri by viewModel.fullscreenImageUri.collectAsStateWithLifecycle()

    val pendingCount = remember(allOrders) {
        allOrders.count { it.status == OrderStatus.DELIVERING || it.status == OrderStatus.NEW_CUSTOMER }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it.text)
            viewModel.clearMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DeliveryOrange,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Giao Hàng Siêu Tốc",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hệ thống quản lý đơn & COD",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "TIỆN ÍCH & TÍNH NĂNG CHÍNH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    // 1. Nhập Excel (.xlsx)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.TableView, contentDescription = null, tint = AccentGreen) },
                        label = { Text("Nhập danh sách Excel (.xlsx)", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.setExcelDialogOpen(true)
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    // 2. Đối soát COD
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DeliveryOrange) },
                        label = { Text("Đối soát tiền COD", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.setCodReconDialogOpen(true)
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    // 3. Cài đặt STK gửi tin nhắn SMS
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Message, contentDescription = null, tint = com.example.ui.theme.SmsPurple) },
                        label = { Text("Cài đặt STK gửi tin nhắn SMS", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.setBankSettingsDialogOpen(true)
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Re-seed demo batch
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray) },
                        label = { Text("Khôi phục dữ liệu mẫu ban đầu", fontSize = 13.sp) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.resetSampleData()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Phiên bản v2.4 • Offline-First Room DB",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Giao hàng siêu tốc",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Mở Menu ☰",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DeliveryOrange,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // 3 TABS: BẢN ĐỒ | CHI TIẾT ĐƠN HÀNG | KHÁCH HÀNG
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    val tabs = AppTab.values()
                    val selectedIndex = tabs.indexOf(currentTab).coerceAtLeast(0)

                    TabRow(
                        selectedTabIndex = selectedIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = DeliveryOrange,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = DeliveryOrange,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEach { tab ->
                            val isSelected = (currentTab == tab)
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.setTab(tab) },
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .testTag("tab_${tab.name.lowercase()}"),
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) DeliveryOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                },
                                icon = {
                                    val icon = when (tab) {
                                        AppTab.MAP -> Icons.Default.Map
                                        AppTab.ORDERS -> Icons.Default.Assignment
                                        AppTab.CUSTOMERS -> Icons.Default.People
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) DeliveryOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Screen Content switched by Selected Tab
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContentAnimation"
                ) { tab ->
                    when (tab) {
                        AppTab.MAP -> MapTabScreen(viewModel = viewModel)
                        AppTab.ORDERS -> OrderTabScreen(viewModel = viewModel)
                        AppTab.CUSTOMERS -> CustomerTabScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // ALL APP DIALOGS
    if (isExcelDialogOpen) {
        ExcelImportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setExcelDialogOpen(false) }
        )
    }

    if (isCodReconDialogOpen) {
        CodReconciliationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setCodReconDialogOpen(false) }
        )
    }

    if (isBankSettingsDialogOpen) {
        BankSettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setBankSettingsDialogOpen(false) }
        )
    }

    if (isCameraScannerOpen) {
        CameraScannerDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setCameraScannerOpen(false) }
        )
    }

    if (photoCaptureForOrder != null) {
        PhotoCaptureDialog(
            order = photoCaptureForOrder!!,
            viewModel = viewModel,
            onDismiss = { viewModel.setPhotoCaptureForOrder(null) }
        )
    }

    if (deliverConfirmOrder != null) {
        DeliveryConfirmDialog(
            order = deliverConfirmOrder!!,
            viewModel = viewModel,
            onDismiss = { viewModel.setDeliverConfirmOrder(null) }
        )
    }

    if (failConfirmOrder != null) {
        FailureConfirmDialog(
            order = failConfirmOrder!!,
            viewModel = viewModel,
            onDismiss = { viewModel.setFailConfirmOrder(null) }
        )
    }

    if (isAddCustomerDialogOpen) {
        CustomerFormDialog(
            initialCustomer = null,
            viewModel = viewModel,
            onDismiss = { viewModel.setAddCustomerDialogOpen(false) }
        )
    }

    if (editingCustomer != null) {
        CustomerFormDialog(
            initialCustomer = editingCustomer,
            viewModel = viewModel,
            onDismiss = { viewModel.setEditingCustomer(null) }
        )
    }

    if (viewingCustomerHistory != null) {
        CustomerHistoryDialog(
            customer = viewingCustomerHistory!!,
            viewModel = viewModel,
            onDismiss = { viewModel.setViewingCustomerHistory(null) }
        )
    }

    if (fullscreenImageUri != null) {
        FullscreenImageViewer(
            imageUri = fullscreenImageUri!!,
            title = "Ảnh xác nhận đơn hàng",
            onDismiss = { viewModel.setFullscreenImageUri(null) }
        )
    }
}
