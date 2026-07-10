package com.aesprt.foldgo.presentation.order

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.DeliveryMethod
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = koinViewModel { parametersOf(orderId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().imePadding()) {
                if (uiState.isLoading) {
                    FoldGoLoading(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.order != null) {
                    OrderDetailContent(
                        order = uiState.order!!,
                        machine = uiState.machine,
                        onReady = viewModel::updateOrderPaymentAndDelivery,
                        onOrderStatusClick = viewModel::updateOrderStatus,
                        onDelivered = viewModel::markAsDelivered
                    )
                } else {
                    Text("Order not found", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun OrderDetailContent(
    order: Order,
    machine: Machine?,
    onReady: (DeliveryMethod, Double) -> Unit,
    onOrderStatusClick: (OrderStatus) -> Unit,
    onDelivered: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            OrderHeader(order)
        }

        if (order.status == OrderStatus.WASHING || order.status == OrderStatus.DRYING || 
            order.status == OrderStatus.WASHED || order.status == OrderStatus.DRIED ||
            order.status == OrderStatus.FOLDING) {
            item {
                ActiveCycleCard(order, machine, onOrderStatusClick)
            }
        }

        item {
            OrderInfoCard(order)
        }

        if (order.status == OrderStatus.FOLDING || order.status == OrderStatus.READY) {
            item {
                StatusActionCard(
                    status = order.status,
                    order = order,
                    onReady = onReady,
                    onDelivered = onDelivered
                )
            }
        }

        item {
            ItemsListCard(order)
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun OrderHeader(order: Order) {
    val statusColor = getStatusColor(order.status)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
                Text(
                    text = "Placed on ${formatDate(order.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Surface(
                color = statusColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = order.status.name,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveCycleCard(
    order: Order,
    machine: Machine?,
    onOrderStatusClick: (OrderStatus) -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    val isCycleComplete = (machine?.endTime != null && currentTime >= machine.endTime) || 
                          order.status == OrderStatus.WASHED || order.status == OrderStatus.DRIED
    
    val statusColor = if (isCycleComplete || order.status == OrderStatus.FOLDING) Color(0xFF4CAF50) else getStatusColor(order.status)
    
    LaunchedEffect(machine?.endTime, order.status) {
        if (machine?.endTime != null && (order.status == OrderStatus.WASHING || order.status == OrderStatus.DRYING)) {
            while (currentTime < machine.endTime) {
                delay(1000.milliseconds)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.1f)
                ) {}
                
                Icon(
                    imageVector = when(order.status) {
                        OrderStatus.WASHING, OrderStatus.WASHED -> Icons.Rounded.LocalLaundryService
                        OrderStatus.DRYING, OrderStatus.DRIED -> Icons.Rounded.Air
                        else -> Icons.Rounded.Checkroom
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .then(if (!isCycleComplete && order.status != OrderStatus.FOLDING) Modifier.rotate(rotation) else Modifier),
                    tint = statusColor
                )
                
                if (isCycleComplete) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).align(Alignment.BottomEnd),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    order.status == OrderStatus.FOLDING -> "Folding in Progress"
                    isCycleComplete && (order.status == OrderStatus.WASHING || order.status == OrderStatus.WASHED) -> "Ready to Dry"
                    isCycleComplete && (order.status == OrderStatus.DRYING || order.status == OrderStatus.DRIED) -> "Ready to Fold"
                    order.status == OrderStatus.WASHING -> "Currently Washing"
                    else -> "Currently Drying"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCycleComplete || order.status == OrderStatus.FOLDING) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
            )
            
            if (isCycleComplete && (order.status == OrderStatus.WASHING || order.status == OrderStatus.WASHED)) {
                Text(
                    text = "Please select a dryer in the Machine Matrix",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (isCycleComplete && (order.status == OrderStatus.DRYING || order.status == OrderStatus.DRIED)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Please proceed to folding area",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onOrderStatusClick(OrderStatus.FOLDING) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Folding Timer")
                    }
                }
            } else if (order.status == OrderStatus.FOLDING) {
                 Text(
                    text = "Tracking folding duration...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (machine != null) {
                Text(
                    text = "assigned to ${machine.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!isCycleComplete && machine.endTime != null) {
                    RemainingTimer(endTime = machine.endTime, color = statusColor, currentTime = currentTime)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RemainingTimer(endTime: Long, color: Color, currentTime: Long) {
    val remaining = endTime - currentTime
    if (remaining > 0) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
        
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = color,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "REMAINING",
            style = MaterialTheme.typography.labelLarge,
            color = color.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun OrderInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Customer Details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(Icons.Rounded.Person, "Customer ID", order.customerId)
            InfoRow(Icons.Rounded.Phone, "Staff ID", order.staffId)
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            Text(
                "Payment Summary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(Icons.Rounded.Payments, "Total Amount", PriceFormatter.format(order.totalAmount))
            InfoRow(Icons.Rounded.AccountBalanceWallet, "Paid Amount", PriceFormatter.format(order.paidAmount))
            if (order.changeDue > 0) {
                InfoRow(Icons.Rounded.Payments, "Change Due", PriceFormatter.format(order.changeDue))
            }
        }
    }
}

@Composable
fun ItemsListCard(order: Order) {
    Column {
        Text(
            "Service Items",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                order.items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${item.quantity} ${item.unit} x ${PriceFormatter.format(item.pricePerUnit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            PriceFormatter.format(item.totalPrice),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (index < order.items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusActionCard(
    status: OrderStatus,
    order: Order, // Added order parameter
    onReady: (DeliveryMethod, Double) -> Unit,
    onDelivered: () -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    var deliveryMethod by remember { mutableStateOf(order.deliveryMethod) }
    var amountPaid by remember { mutableStateOf("") }
    val deliveryFee = 50.0 // Hardcoded for now, could be in settings

    val nextStatus = when (status) {
        OrderStatus.FOLDING -> OrderStatus.READY
        OrderStatus.READY -> OrderStatus.DELIVERED
        else -> null
    }

    if (nextStatus != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (nextStatus == OrderStatus.READY) Icons.Rounded.CheckCircle else Icons.Rounded.DoneAll,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nextStatus == OrderStatus.READY) "Finish Folding?" else "Mark as Delivered?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (nextStatus == OrderStatus.READY) "Collect payment and set delivery" else "Mark order as completed",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = { 
                            if (nextStatus == OrderStatus.READY) showPaymentDialog = true
                            else onDelivered()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed")
                    }
                }

                if (showPaymentDialog) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Delivery Method", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = deliveryMethod == DeliveryMethod.PICKUP,
                            onClick = { deliveryMethod = DeliveryMethod.PICKUP },
                            label = { Text("Pickup") }
                        )
                        FilterChip(
                            selected = deliveryMethod == DeliveryMethod.DELIVERY,
                            onClick = { deliveryMethod = DeliveryMethod.DELIVERY },
                            label = { Text("Delivery") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Price Breakdown
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BreakdownRow("Order Items", order.totalAmount)
                            if (deliveryMethod == DeliveryMethod.DELIVERY) {
                                BreakdownRow("Delivery Fee", deliveryFee)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                            val finalTotal = if (deliveryMethod == DeliveryMethod.DELIVERY) order.totalAmount + deliveryFee else order.totalAmount
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Final Amount", fontWeight = FontWeight.Bold)
                                Text(PriceFormatter.format(finalTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = { amountPaid = it },
                        label = { Text("Tendered Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val tendered = amountPaid.toDoubleOrNull() ?: 0.0
                    val finalTotal = if (deliveryMethod == DeliveryMethod.DELIVERY) order.totalAmount + deliveryFee else order.totalAmount
                    val change = tendered - finalTotal

                    if (change > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Change Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text(
                                        PriceFormatter.format(change),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Icon(Icons.Rounded.Payments, null, tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onReady(deliveryMethod, tendered)
                            showPaymentDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Ready")
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(PriceFormatter.format(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun getStatusColor(status: OrderStatus): Color = when (status) {
    OrderStatus.INTAKE -> Color(0xFFFFAB00)
    OrderStatus.WASHING -> Color(0xFF03A9F4)
    OrderStatus.WASHED -> Color(0xFF4CAF50)
    OrderStatus.DRYING -> Color(0xFF03A9F4)
    OrderStatus.DRIED -> Color(0xFF4CAF50)
    OrderStatus.FOLDING -> Color(0xFF03A9F4)
    OrderStatus.READY -> Color(0xFF4CAF50)
    OrderStatus.DELIVERED -> Color(0xFF8BC34A)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
