package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.OrderEntity
import com.example.model.LatLngCoord
import com.example.model.OrderStatus
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DeliveryOrange
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyMedium
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters
import com.example.util.IntentsHelper
import com.example.util.LocationHelper
import kotlin.math.roundToInt

@Composable
fun MapTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val closestThree by viewModel.closestThreeOrders.collectAsStateWithLifecycle()
    val currentGps by viewModel.currentGpsLocation.collectAsStateWithLifecycle()
    val markerPopupOrder by viewModel.markerPopupOrder.collectAsStateWithLifecycle()

    val pendingOrdersCount = remember(allOrders) {
        allOrders.count { it.status == OrderStatus.DELIVERING || it.status == OrderStatus.NEW_CUSTOMER }
    }

    // Map Pan and Zoom State
    var mapZoom by remember { mutableFloatStateOf(1.0f) }
    var mapPanX by remember { mutableFloatStateOf(0f) }
    var mapPanY by remember { mutableFloatStateOf(0f) }
    var isSatelliteMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP HALF: Interactive Map with Goong Map Styling & STT Badges (56% height)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.56f)
                .background(if (isSatelliteMode) Color(0xFF1B2A1D) else Color(0xFFE8ECEF))
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            // Reference center Lat/Lng
            val centerLat = currentGps.lat
            val centerLng = currentGps.lng

            // Coordinate mapping helper
            fun mapCoordToScreen(lat: Double, lng: Double): Offset {
                val scale = 14000f * mapZoom
                val dx = ((lng - centerLng) * scale).toFloat() + mapPanX
                val dy = -((lat - centerLat) * scale).toFloat() + mapPanY
                return Offset(canvasWidth / 2f + dx, canvasHeight / 2f + dy)
            }

            // Interactive Map Canvas (Roads, River, Route Polyline, GPS pulse)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            mapPanX += dragAmount.x
                            mapPanY += dragAmount.y
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Tap on empty space closes popup
                            viewModel.setMarkerPopupOrder(null)
                        }
                    }
            ) {
                drawMapBackground(isSatellite = isSatelliteMode)

                // Draw route line connecting active delivery orders in sequence (STT)
                val activeOrdersInSeq = allOrders
                    .filter { it.status == OrderStatus.DELIVERING || it.status == OrderStatus.NEW_CUSTOMER }
                    .sortedBy { it.sequenceNumber }

                if (activeOrdersInSeq.isNotEmpty()) {
                    val path = Path()
                    val startScreen = mapCoordToScreen(currentGps.lat, currentGps.lng)
                    path.moveTo(startScreen.x, startScreen.y)

                    activeOrdersInSeq.forEach { order ->
                        val pt = mapCoordToScreen(order.latitude, order.longitude)
                        path.lineTo(pt.x, pt.y)
                    }

                    // Draw route glow
                    drawPath(
                        path = path,
                        color = DeliveryOrange.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 12f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                        )
                    )

                    // Draw solid route core
                    drawPath(
                        path = path,
                        color = DeliveryOrange,
                        style = Stroke(
                            width = 5f,
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Draw current GPS User Location Pulse
                val userScreenPos = mapCoordToScreen(currentGps.lat, currentGps.lng)
                drawCircle(
                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                    radius = 28f,
                    center = userScreenPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = userScreenPos
                )
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 8f,
                    center = userScreenPos
                )
            }

            // STT Marker Badges rendered as interactive Compose overlays
            allOrders.forEach { order ->
                val screenPos = mapCoordToScreen(order.latitude, order.longitude)
                val isSelected = markerPopupOrder?.orderCode == order.orderCode

                // Check if marker is visible in current viewport bounds
                if (screenPos.x in -50f..(canvasWidth + 50f) && screenPos.y in -50f..(canvasHeight + 50f)) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (screenPos.x - 22.dp.toPx()).roundToInt(),
                                    (screenPos.y - 44.dp.toPx()).roundToInt()
                                )
                            }
                            .clickable {
                                viewModel.setMarkerPopupOrder(order)
                            }
                    ) {
                        MapMarkerPin(
                            sequenceNumber = order.sequenceNumber,
                            status = order.status,
                            isSelected = isSelected
                        )
                    }
                }
            }

            // Map Control Buttons (Zoom in/out, Recenter GPS, Satellite Toggle)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Layer toggle (Goong / Satellite)
                FilledIconButton(
                    onClick = { isSatelliteMode = !isSatelliteMode },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSatelliteMode) NavyDark else Color.White,
                        contentColor = if (isSatelliteMode) Color.White else NavyDark
                    ),
                    modifier = Modifier.size(40.dp).shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Đổi giao diện bản đồ",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recenter GPS
                FilledIconButton(
                    onClick = {
                        mapPanX = 0f
                        mapPanY = 0f
                        mapZoom = 1.0f
                        viewModel.refreshCurrentGpsLocation()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0284C7)
                    ),
                    modifier = Modifier.size(40.dp).shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Vị trí của tôi",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom In
                FilledIconButton(
                    onClick = { mapZoom = (mapZoom * 1.3f).coerceAtMost(3.5f) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = NavyDark
                    ),
                    modifier = Modifier.size(40.dp).shadow(4.dp, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Phóng to", modifier = Modifier.size(20.dp))
                }

                // Zoom Out
                FilledIconButton(
                    onClick = { mapZoom = (mapZoom / 1.3f).coerceAtLeast(0.6f) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = NavyDark
                    ),
                    modifier = Modifier.size(40.dp).shadow(4.dp, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Thu nhỏ", modifier = Modifier.size(20.dp))
                }
            }

            // Top Status Overlay (Goong Maps Active Indicator)
            Surface(
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Text(
                        text = "Bản đồ Goong • Lộ trình STT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }
            }

            // POPUP ẢNH NHỎ KHI CLICK MARKER
            // Tương tác: Click Marker ➔ Hiện Popup ảnh nhỏ ➔ Click Popup ➔ Fullscreen View
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = markerPopupOrder != null,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    val order = markerPopupOrder
                    if (order != null) {
                        MarkerThumbnailPopup(
                            order = order,
                            onImageClick = {
                                val imgUri = order.proofImageUri ?: "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=800"
                                viewModel.setFullscreenImageUri(imgUri)
                            },
                            onCallClick = { IntentsHelper.makePhoneCall(context, order.customerPhone) },
                            onDirectionsClick = {
                                IntentsHelper.openGoogleMapsNavigation(context, order.latitude, order.longitude, order.customerName)
                            },
                            onClose = { viewModel.setMarkerPopupOrder(null) }
                        )
                    }
                }
            }
        }

        // BOTTOM HALF: 3 ĐƠN HÀNG GẦN NHẤT (Bottom Sheet section, 44% height)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.44f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Header of Bottom Sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DeliveryOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = DeliveryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "3 ĐƠN HÀNG GẦN NHẤT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (pendingOrdersCount > 0) {
                        Surface(
                            color = DeliveryOrange,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$pendingOrdersCount đơn cần giao",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (closestThree.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có đơn hàng nào cần giao quanh đây",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(closestThree) { (order, distance) ->
                            ClosestOrderItemCard(
                                order = order,
                                distanceMeters = distance,
                                onDetailClick = {
                                    viewModel.setSearchQuery(order.orderCode)
                                    viewModel.setTab(AppTab.ORDERS)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Marker Pin Composable with Number STT badge
@Composable
fun MapMarkerPin(
    sequenceNumber: Int,
    status: OrderStatus,
    isSelected: Boolean
) {
    val markerColor = when (status) {
        OrderStatus.DELIVERED -> AccentGreen
        OrderStatus.FAILED -> AccentRed
        OrderStatus.NEW_CUSTOMER -> Color(0xFF0284C7)
        OrderStatus.RETRY -> Color(0xFFF59E0B)
        OrderStatus.DELIVERING -> DeliveryOrange
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) NavyDark else markerColor,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 3.dp else 2.dp,
                color = Color.White
            ),
            shadowElevation = if (isSelected) 8.dp else 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#$sequenceNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }

        // Small triangle tip
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 6.dp)
                .background(if (isSelected) NavyDark else markerColor, shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
        )
    }
}

// Marker Thumbnail Popup (Click Marker ➔ Hiện Popup ảnh nhỏ ➔ Click Popup ➔ Fullscreen View)
@Composable
fun MarkerThumbnailPopup(
    order: OrderEntity,
    onImageClick: () -> Unit,
    onCallClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    onClose: () -> Unit
) {
    val displayPhoto = order.proofImageUri ?: "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail Image - Click to open Fullscreen
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onImageClick)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayPhoto)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Ảnh nhà/đơn hàng",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Magnifying glass / fullscreen icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Xem to ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Order info details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = DeliveryOrange,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "STT #${order.sequenceNumber}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = order.orderCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng popup",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = order.customerName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = order.address,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // COD Amount highlighted
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "COD: ${Formatters.formatCurrency(order.codAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentRed
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Quick Action Buttons
                    FilledIconButton(
                        onClick = onCallClick,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CallGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Gọi khách",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = onDirectionsClick,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Chỉ đường",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 1 of 3 Closest Order Item Cards in the Bottom Sheet
@Composable
fun ClosestOrderItemCard(
    order: OrderEntity,
    distanceMeters: Double,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetailClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // STT Badge
            Surface(
                color = DeliveryOrange,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${order.sequenceNumber}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = order.customerName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📍 ${LocationHelper.formatDistance(distanceMeters)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = order.address,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "COD: ${Formatters.formatCurrency(order.codAmount)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }

            // Button: Xem Chi tiết đơn
            Button(
                onClick = onDetailClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliveryOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Chi tiết",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Background Road Grid & River simulation for Goong / Streets
private fun DrawScope.drawMapBackground(isSatellite: Boolean) {
    val width = size.width
    val height = size.height

    if (isSatellite) {
        // Satellite dark green/earth tones
        drawRect(Color(0xFF1E2E21))
    } else {
        // Light map style (Goong Maps aesthetic)
        drawRect(Color(0xFFEAEFF2))

        // River / Water body
        val riverPath = Path().apply {
            moveTo(0f, height * 0.75f)
            cubicTo(
                width * 0.3f, height * 0.7f,
                width * 0.6f, height * 0.85f,
                width, height * 0.8f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(riverPath, Color(0xFFBBE2F9))

        // Main Roads & Streets Grid
        val roadColor = Color(0xFFFFFFFF)
        val highwayColor = Color(0xFFFFD54F)

        // Diagonal Highway
        drawLine(
            color = highwayColor,
            start = Offset(0f, height * 0.2f),
            end = Offset(width, height * 0.6f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )

        // Arterial Streets
        drawLine(
            color = roadColor,
            start = Offset(width * 0.25f, 0f),
            end = Offset(width * 0.25f, height),
            strokeWidth = 10f
        )
        drawLine(
            color = roadColor,
            start = Offset(width * 0.7f, 0f),
            end = Offset(width * 0.7f, height),
            strokeWidth = 10f
        )
        drawLine(
            color = roadColor,
            start = Offset(0f, height * 0.45f),
            end = Offset(width, height * 0.45f),
            strokeWidth = 10f
        )

        // Secondary streets
        for (i in 1..6) {
            drawLine(
                color = roadColor.copy(alpha = 0.7f),
                start = Offset(0f, height * (i * 0.15f)),
                end = Offset(width, height * (i * 0.15f)),
                strokeWidth = 5f
            )
            drawLine(
                color = roadColor.copy(alpha = 0.7f),
                start = Offset(width * (i * 0.18f), 0f),
                end = Offset(width * (i * 0.18f), height),
                strokeWidth = 5f
            )
        }
    }
}
