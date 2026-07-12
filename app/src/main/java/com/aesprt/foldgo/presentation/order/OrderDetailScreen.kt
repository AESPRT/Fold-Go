package com.aesprt.foldgo.presentation.order

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.core.util.DateFormatterUtils.formatDate
import com.aesprt.foldgo.core.util.KgFormatterUtils
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.core.util.OrderStatusUtils
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderBatch
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.StatusChip
import com.aesprt.foldgo.presentation.machines.MachineViewModel
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.MintGreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = koinViewModel { parametersOf(orderId) },
    machineViewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Order Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
            ) {
                if (uiState.isLoading) {
                    FoldGoLoading(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.order != null) {
                    OrderDetailContent(
                        order = uiState.order!!,
                        machine = uiState.machine,
                        allMachines = uiState.allMachines,
                        batches = uiState.batches,
                        onReady = viewModel::updateOrderPaymentAndDelivery,
                        onOrderStatusClick = viewModel::updateOrderStatus,
                        onDelivered = viewModel::markAsDelivered,
                        onFinishCycle = machineViewModel::finishCycle
                    )
                } else {
                    Text(
                        "Order not found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (uiState.showSmsPrompt && uiState.order != null) {
                    val order = uiState.order!!
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissSmsPrompt() },
                        title = { Text("Notify Customer?") },
                        text = { Text("Would you like to send an SMS to ${order.customerName} notifying them that their order is ready?") },
                        confirmButton = {
                            Button(onClick = {
                                val message =
                                    "Hi ${order.customerName}, your Fold-Go order ${order.orderNumber} is now ready for ${order.deliveryMethod.name.lowercase()}. Total amount: ${
                                        PriceFormatter.format(order.totalAmount)
                                    }."
                                viewModel.sendSmsAndComplete(message)
                            }) {
                                Text("Send SMS")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.dismissSmsPrompt() }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDetailContent(
    order: Order,
    machine: Machine?,
    allMachines: List<Machine>,
    batches: List<OrderBatch>,
    onReady: (DeliveryMethod, Double) -> Unit,
    onOrderStatusClick: (OrderStatus) -> Unit,
    onDelivered: () -> Unit,
    onFinishCycle: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            OrderHeader(order)
        }

        if (batches.isNotEmpty()) {
            item {
                Text(
                    "Order Batches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            items(batches) { batch ->
                val batchMachine = allMachines.find { it.machineId == batch.machineId }
                BatchCard(batch, batchMachine)
            }
        }

        if (order.status == OrderStatus.WASHING || order.status == OrderStatus.DRYING ||
            order.status == OrderStatus.WASHING_AND_DRYING || order.status == OrderStatus.WASHED_AND_DRIED ||
            order.status == OrderStatus.WASHED || order.status == OrderStatus.DRIED ||
            order.status == OrderStatus.IRONING || order.status == OrderStatus.IRONED ||
            order.status == OrderStatus.FOLDING
        ) {
            item {
                ActiveCycleCard(order, machine, allMachines, batches, onOrderStatusClick, onFinishCycle)
            }
        }

        item {
            OrderInfoCard(order)
        }

        val hasDryItems =
            order.items.any { it.type == ServiceType.DRY || it.type == ServiceType.WASH_DRY }
        val hasIronItems = order.items.any { it.type == ServiceType.IRON }

        val isServiceProcessingComplete = when (order.status) {
            OrderStatus.WASHED -> !hasDryItems
            OrderStatus.DRIED -> !hasIronItems
            OrderStatus.IRONED -> true
            OrderStatus.FOLDING -> true
            OrderStatus.READY -> true
            else -> false
        }

        if (isServiceProcessingComplete) {
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
fun BatchCard(batch: OrderBatch, machine: Machine?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (batch.status == OrderStatus.WASHING) Icons.Rounded.LocalLaundryService else Icons.Rounded.Air,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Batch: ${batch.weightKg}kg",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = machine?.name ?: "No Machine",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(status = batch.status)
        }
    }
}

@Composable
fun OrderHeader(order: Order) {
    val statusColor = OrderStatusUtils.getContainerColor(order.status)

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
                    text = OrderStatusUtils.getDisplayName(order.status),
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
    allMachines: List<Machine>,
    batches: List<OrderBatch>,
    onOrderStatusClick: (OrderStatus) -> Unit,
    onFinishCycle: (String) -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val totalWeight = order.items.sumOf { it.quantity }.coerceAtLeast(0.1)

    val isCycleComplete = (machine?.endTime != null && currentTime >= machine.endTime) ||
            order.status == OrderStatus.WASHED_AND_DRIED ||
            order.status == OrderStatus.WASHED || order.status == OrderStatus.DRIED ||
            order.status == OrderStatus.IRONED

    // Weight finished for the CURRENT MAJOR PHASE (WASHED, DRIED, IRONED)
    val weightFinished = if (batches.isEmpty()) {
        when (order.status) {
            OrderStatus.WASHED_AND_DRIED,
            OrderStatus.WASHED,
            OrderStatus.DRIED,
            OrderStatus.IRONED,
            OrderStatus.FOLDING,
            OrderStatus.READY -> totalWeight
            else -> 0.0
        }
    } else {
        batches.filter {
            when (order.status) {
                OrderStatus.WASHING_AND_DRYING,
                OrderStatus.WASHED_AND_DRIED -> it.status in listOf(
                    OrderStatus.WASHED_AND_DRIED,
                    OrderStatus.IRONING,
                    OrderStatus.IRONED,
                    OrderStatus.FOLDING,
                    OrderStatus.READY
                )

                OrderStatus.WASHING,
                OrderStatus.WASHED -> it.status in listOf(
                    OrderStatus.WASHED,
                    OrderStatus.DRYING,
                    OrderStatus.DRIED,
                    OrderStatus.IRONING,
                    OrderStatus.IRONED,
                    OrderStatus.FOLDING,
                    OrderStatus.READY
                )

                OrderStatus.DRYING,
                OrderStatus.DRIED -> it.status in listOf(
                    OrderStatus.DRIED,
                    OrderStatus.IRONING,
                    OrderStatus.IRONED,
                    OrderStatus.FOLDING,
                    OrderStatus.READY
                )

                OrderStatus.IRONING, OrderStatus.IRONED -> it.status in listOf(
                    OrderStatus.IRONED,
                    OrderStatus.FOLDING,
                    OrderStatus.READY
                )

                else -> it.status in listOf(OrderStatus.FOLDING, OrderStatus.READY)
            }
        }.sumOf { it.weightKg }
    }

    // Weight currently in machines for the CURRENT PHASE
    val weightInAction = if (batches.isEmpty()) {
        if (order.machineId != null && !isCycleComplete) totalWeight else 0.0
    } else {
        batches.filter {
            when (order.status) {
                OrderStatus.WASHING_AND_DRYING -> it.status == OrderStatus.WASHING_AND_DRYING
                OrderStatus.WASHING -> it.status == OrderStatus.WASHING
                OrderStatus.DRYING -> it.status == OrderStatus.DRYING
                OrderStatus.IRONING -> it.status == OrderStatus.IRONING
                else -> false
            }
        }.sumOf { it.weightKg }
    }

    val isEverythingDone = weightFinished >= totalWeight

    val hasDryItems =
        order.items.any { it.type == ServiceType.DRY || it.type == ServiceType.WASH_DRY }
    val ironMachines = allMachines.filter { it.type == MachineType.IRON }
    val isIronAvailable = ironMachines.isNotEmpty()

    val statusColor =
        if (isEverythingDone || order.status == OrderStatus.FOLDING) Color(0xFF4CAF50) else OrderStatusUtils.getContainerColor(
            order.status
        )

    LaunchedEffect(machine?.endTime, order.status) {
        if (machine?.endTime != null && (order.status == OrderStatus.WASHING || order.status == OrderStatus.DRYING || order.status == OrderStatus.WASHING_AND_DRYING)) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.1f)
                ) {}

                Icon(
                    imageVector = OrderStatusUtils.getStatusIcon(order.status),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .then(
                            if (!isEverythingDone && order.status != OrderStatus.FOLDING) Modifier.rotate(
                                rotation
                            ) else Modifier
                        ),
                    tint = statusColor
                )

                if (isEverythingDone) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    order.status == OrderStatus.FOLDING -> "Folding in Progress"
                    order.status == OrderStatus.IRONING -> "Ironing in Progress"
                    isEverythingDone && (
                            order.status == OrderStatus.WASHING ||
                                    order.status == OrderStatus.WASHED ||
                                    order.status == OrderStatus.WASHING_AND_DRYING ||
                                    order.status == OrderStatus.WASHED_AND_DRIED
                            ) -> {
                        if (hasDryItems && machine?.type != MachineType.WASHER_DRYER) "Ready to Dry" else "Ready to Fold"
                    }

                    isEverythingDone && (order.status == OrderStatus.DRYING || order.status == OrderStatus.DRIED) -> "Ready to Fold or Iron"
                    !isEverythingDone && isCycleComplete -> "Cycle Finished • More Pending"
                    isEverythingDone && isCycleComplete -> "Cycle Finished • No More Pending"
                    order.status == OrderStatus.WASHING -> "Currently Washing (${
                        KgFormatterUtils.formatDouble(
                            weightInAction,
                            weightFinished
                        )
                    }/${totalWeight}kg)"

                    order.status == OrderStatus.WASHING_AND_DRYING -> "Currently Washing and Drying (${
                        KgFormatterUtils.formatDouble(
                            weightInAction,
                            weightFinished
                        )
                    }/${totalWeight}kg)"

                    order.status == OrderStatus.DRIED
                            || order.status == OrderStatus.WASHED_AND_DRIED
                            || order.status == OrderStatus.WASHED -> "Ready to Fold"

                    else -> "Currently Drying (${
                        KgFormatterUtils.formatDouble(
                            weightInAction,
                            weightFinished
                        )
                    }/${totalWeight}kg)"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isEverythingDone || order.status == OrderStatus.FOLDING || order.status == OrderStatus.IRONING) MintGreen else MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (isEverythingDone && (order.status == OrderStatus.WASHING
                        || order.status == OrderStatus.WASHED)
            ) {
                if (hasDryItems && machine?.type != MachineType.WASHER_DRYER) {
                    Text(
                        text = "Please select a dryer in the Machine Matrix",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onOrderStatusClick(OrderStatus.FOLDING) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Folding Timer", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else if (isEverythingDone && (
                        order.status == OrderStatus.DRYING
                                || order.status == OrderStatus.DRIED
                                || order.status == OrderStatus.WASHED_AND_DRIED
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isIronAvailable) {
                        Text(
                            text = "Does this order need ironing?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { onOrderStatusClick(OrderStatus.IRONED) }, // Skip ironing, go to ready to fold
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Skip to Fold", style = MaterialTheme.typography.labelLarge)
                            }
                            Button(
                                onClick = { onOrderStatusClick(OrderStatus.DRIED) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Use Iron", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        val availableIrons = ironMachines.filter { it.status == MachineStatus.IDLE }
                        if (availableIrons.isNotEmpty()) {
                            Text(
                                text = "Available Irons: ${availableIrons.joinToString { it.name }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(top = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Text(
                            text = "To start ironing, please select an Iron in the Machine Matrix",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Ready to be folded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onOrderStatusClick(OrderStatus.FOLDING) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Folding Timer", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else if (!isEverythingDone && isCycleComplete) {
                Text(
                    text = "Weight processed: ${
                        KgFormatterUtils.formatDouble(
                            weightFinished
                        )
                    }kg of ${totalWeight}kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Please assign the remaining ${
                        KgFormatterUtils.formatDouble(
                            totalWeight - weightFinished
                        )
                    }kg in the Machine Matrix",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (order.status == OrderStatus.IRONED) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ready to be folded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onOrderStatusClick(OrderStatus.FOLDING) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Folding Timer", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else if (order.status == OrderStatus.FOLDING || order.status == OrderStatus.IRONING) {
                Text(
                    text = if (order.status == OrderStatus.FOLDING) "Tracking folding duration..." else "Tracking ironing duration...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (machine != null) {
                Text(
                    text = "assigned to ${machine.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isCycleComplete && machine.endTime != null) {
                    RemainingTimer(
                        endTime = machine.endTime,
                        color = statusColor,
                        currentTime = currentTime,
                        machineId = machine.machineId,
                        onFinishCycle = onFinishCycle
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RemainingTimer(endTime: Long, color: Color, currentTime: Long, machineId: String, onFinishCycle: (String) -> Unit) {
    val remaining = endTime - currentTime
    if (remaining > 0) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = color,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = "REMAINING",
            style = MaterialTheme.typography.labelLarge,
            color = color.copy(alpha = 0.6f),
            letterSpacing = 2.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    } else {
        onFinishCycle(machineId)
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
            InfoRow(Icons.Rounded.Person, "Customer", order.customerName)
            InfoRow(Icons.Rounded.Phone, "Contact Number", order.customerPhone)
            if (order.customerAddress.isNotBlank()) {
                InfoRow(Icons.Rounded.LocationOn, "Address", order.customerAddress)
            }

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
            InfoRow(
                Icons.Rounded.Payments,
                "Total Amount",
                PriceFormatter.format(order.totalAmount)
            )
            InfoRow(
                Icons.Rounded.AccountBalanceWallet,
                "Paid Amount",
                PriceFormatter.format(order.paidAmount)
            )
            if (order.changeDue > 0) {
                InfoRow(
                    Icons.Rounded.Payments,
                    "Change Due",
                    PriceFormatter.format(order.changeDue)
                )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                            Text(
                                item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${item.quantity} ${item.unit} x ${PriceFormatter.format(item.pricePerUnit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            PriceFormatter.format(item.totalPrice),
                            style = MaterialTheme.typography.bodyLarge,
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
    var amountPaid by remember { mutableStateOf("") }
    val deliveryFee = 50.0 // Hardcoded for now, could be in settings

    val nextStatus = when (status) {
        OrderStatus.WASHED, OrderStatus.DRIED, OrderStatus.IRONED, OrderStatus.FOLDING -> OrderStatus.READY
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
                            text = if (nextStatus == OrderStatus.READY) "Collect payment" else "Mark order as completed",
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
                        Text("Proceed", style = MaterialTheme.typography.labelLarge)
                    }
                }

                if (showPaymentDialog) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Payment Breakdown",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Delivery Method: ${order.deliveryMethod.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price Breakdown
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BreakdownRow("Order Items", order.totalAmount)
                            if (order.deliveryMethod == DeliveryMethod.DELIVERY) {
                                BreakdownRow("Delivery Fee", deliveryFee)
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 0.5.dp
                            )
                            val finalTotal =
                                if (order.deliveryMethod == DeliveryMethod.DELIVERY) order.totalAmount + deliveryFee else order.totalAmount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Final Amount",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    PriceFormatter.format(finalTotal),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = { amountPaid = it },
                        label = {
                            Text(
                                "Tendered Amount",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val tendered = amountPaid.toDoubleOrNull() ?: 0.0
                    val finalTotal =
                        if (order.deliveryMethod == DeliveryMethod.DELIVERY) order.totalAmount + deliveryFee else order.totalAmount
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
                                    Text(
                                        "Change Due",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        PriceFormatter.format(change),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Icon(
                                    Icons.Rounded.Payments,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onReady(order.deliveryMethod, tendered)
                            showPaymentDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = tendered >= finalTotal
                    ) {
                        Text("Confirm Ready", style = MaterialTheme.typography.labelLarge)
                    }

                    if (tendered < finalTotal && amountPaid.isNotEmpty()) {
                        Text(
                            text = "Insufficient amount. Required: ${
                                PriceFormatter.format(
                                    finalTotal
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .align(Alignment.CenterHorizontally)
                        )
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
        Text(
            PriceFormatter.format(amount),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

@Preview(showBackground = true)
@Composable
fun OrderDetailContentPreview() {
    FoldGoTheme {
        OrderDetailContent(
            order = Order(
                orderId = "1",
                shopId = "shop1",
                customerId = "cust1",
                customerName = "Juan Dela Cruz",
                customerPhone = "09123456789",
                orderNumber = "FG-1001",
                items = listOf(
                    com.aesprt.foldgo.domain.model.ServiceItem(
                        "Wash & Dry",
                        5.0,
                        "KG",
                        65.0,
                        325.0,
                        ServiceType.WASH_DRY
                    )
                ),
                totalAmount = 325.0,
                paidAmount = 0.0,
                status = OrderStatus.WASHING,
                intakePhotos = emptyList(),
                machineId = "M1",
                staffId = "staff1",
                staffName = "Operator 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            machine = Machine(
                "M1",
                "shop1",
                "Washer 01",
                MachineType.WASHER,
                8.0,
                MachineStatus.IDLE,
                0L,
                System.currentTimeMillis() + 600000
            ),
            allMachines = emptyList(),
            batches = emptyList(),
            onReady = { _, _ -> },
            onOrderStatusClick = {},
            onDelivered = {},
            onFinishCycle = {}
        )
    }
}
