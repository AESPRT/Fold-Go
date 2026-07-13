package com.aesprt.foldgo.presentation.order

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.aesprt.foldgo.core.util.TimeUtils
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
    var selectedBatchId by remember { mutableStateOf<String?>(null) }

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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Order Batches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedBatchId != null) {
                        TextButton(onClick = { selectedBatchId = null }) {
                            Text("Show All Progress", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            itemsIndexed(batches) { index, batch ->
                val batchMachine = allMachines.find { it.machineId == batch.machineId }
                BatchCard(
                    batch = batch,
                    machine = batchMachine,
                    index = index,
                    total = batches.size,
                    isSelected = selectedBatchId == batch.batchId,
                    onClick = {
                        selectedBatchId = if (selectedBatchId == batch.batchId) null else batch.batchId
                    }
                )
            }
        }

        if (order.status == OrderStatus.WASHING || order.status == OrderStatus.DRYING ||
            order.status == OrderStatus.WASHING_AND_DRYING || order.status == OrderStatus.WASHED_AND_DRIED ||
            order.status == OrderStatus.WASHED || order.status == OrderStatus.DRIED ||
            order.status == OrderStatus.IRONING || order.status == OrderStatus.IRONED ||
            order.status == OrderStatus.FOLDING
        ) {
            item {
                val selectedBatch = batches.find { it.batchId == selectedBatchId }
                val activeMachine = if (selectedBatch != null) {
                    allMachines.find { it.machineId == selectedBatch.machineId }
                } else machine

                ActiveCycleCard(
                    order = order,
                    machine = activeMachine,
                    allMachines = allMachines,
                    batches = batches,
                    selectedBatch = selectedBatch,
                    onOrderStatusClick = onOrderStatusClick,
                    onFinishCycle = onFinishCycle
                )
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
fun BatchCard(
    batch: OrderBatch,
    machine: Machine?,
    index: Int,
    total: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val isProcessing = batch.status == OrderStatus.WASHING ||
            batch.status == OrderStatus.DRYING ||
            batch.status == OrderStatus.WASHING_AND_DRYING ||
            batch.status == OrderStatus.IRONING

    LaunchedEffect(isProcessing, machine?.endTime) {
        if (isProcessing && machine?.endTime != null) {
            while (currentTime < machine.endTime) {
                delay(1000.milliseconds)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "batchRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val batchIcon = when (batch.serviceType) {
        ServiceType.WASH -> Icons.Rounded.LocalLaundryService
        ServiceType.DRY -> Icons.Rounded.Air
        ServiceType.WASH_DRY -> Icons.Rounded.AllInclusive
        ServiceType.IRON -> Icons.Rounded.Iron
        ServiceType.OTHER -> Icons.Rounded.Category
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onClick
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
                        imageVector = batchIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .then(if (isProcessing) Modifier.rotate(rotation) else Modifier),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Batch ${index + 1} of $total: ${batch.weightKg}kg",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = machine?.name ?: "No Machine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isProcessing && machine?.endTime != null) {
                        val remaining = machine.endTime - currentTime
                        if (remaining > 0) {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
                            val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
                            Text(
                                text = " • ${TimeUtils.formatTime(minutes, seconds)} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
    selectedBatch: OrderBatch? = null,
    onOrderStatusClick: (OrderStatus) -> Unit,
    onFinishCycle: (String) -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // If a batch is selected, we focus on its weight. Otherwise, use total order weight.
    val totalWeight = selectedBatch?.weightKg ?: order.items.sumOf { it.quantity }.coerceAtLeast(0.1)

    // Current status to display (Batch status or Order status)
    val currentStatus = selectedBatch?.status ?: order.status

    val isCycleComplete = (machine?.endTime != null && currentTime >= machine.endTime) ||
            currentStatus == OrderStatus.WASHED_AND_DRIED ||
            currentStatus == OrderStatus.WASHED || currentStatus == OrderStatus.DRIED ||
            currentStatus == OrderStatus.IRONED

    // Weight finished calculation
    val weightFinished = if (selectedBatch != null) {
        if (isCycleComplete) selectedBatch.weightKg else 0.0
    } else if (batches.isEmpty()) {
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

    // Weight currently in action
    val weightInAction = if (selectedBatch != null) {
        if (machine != null && !isCycleComplete) selectedBatch.weightKg else 0.0
    } else if (batches.isEmpty()) {
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

    val isEverythingDone = if (selectedBatch != null) isCycleComplete else weightFinished >= totalWeight

    val hasDryItems = if (selectedBatch != null) {
        selectedBatch.serviceType == ServiceType.WASH_DRY || selectedBatch.serviceType == ServiceType.DRY
    } else {
        order.items.any { it.type == ServiceType.DRY || it.type == ServiceType.WASH_DRY }
    }

    val ironMachines = allMachines.filter { it.type == MachineType.IRON }
    val isIronAvailable = ironMachines.isNotEmpty()

    // Determine the relevant machine end time (specific batch or max of all batches)
    val relevantEndTime = if (selectedBatch != null) {
        machine?.endTime
    } else {
        // In summary mode, find the latest end time among all active batches for this order
        val activeBatchMachineIds = batches.filter { 
            it.status == OrderStatus.WASHING || it.status == OrderStatus.DRYING || 
            it.status == OrderStatus.WASHING_AND_DRYING || it.status == OrderStatus.IRONING 
        }.map { it.machineId }
        
        allMachines.filter { it.machineId in activeBatchMachineIds }
            .mapNotNull { it.endTime }
            .maxOrNull()
    }

    val statusColor =
        if (isEverythingDone || currentStatus == OrderStatus.FOLDING) Color(0xFF4CAF50) else OrderStatusUtils.getContainerColor(
            currentStatus
        )

    LaunchedEffect(relevantEndTime, currentStatus) {
        if (relevantEndTime != null && (currentStatus == OrderStatus.WASHING || currentStatus == OrderStatus.DRYING || currentStatus == OrderStatus.WASHING_AND_DRYING)) {
            while (currentTime < relevantEndTime) {
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
            if (selectedBatch != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Viewing Selected Batch",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.1f)
                ) {}

                Icon(
                    imageVector = if (selectedBatch != null) {
                        when (selectedBatch.serviceType) {
                            ServiceType.WASH -> Icons.Rounded.LocalLaundryService
                            ServiceType.DRY -> Icons.Rounded.Air
                            ServiceType.WASH_DRY -> Icons.Rounded.AllInclusive
                            ServiceType.IRON -> Icons.Rounded.Iron
                            else -> OrderStatusUtils.getStatusIcon(currentStatus)
                        }
                    } else OrderStatusUtils.getStatusIcon(currentStatus),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .then(
                            if (!isEverythingDone && currentStatus != OrderStatus.FOLDING) Modifier.rotate(
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
                    currentStatus == OrderStatus.FOLDING -> "Folding in Progress"
                    currentStatus == OrderStatus.IRONING -> "Ironing in Progress"
                    isEverythingDone && (
                            currentStatus == OrderStatus.WASHING ||
                                    currentStatus == OrderStatus.WASHED ||
                                    currentStatus == OrderStatus.WASHING_AND_DRYING ||
                                    currentStatus == OrderStatus.WASHED_AND_DRIED
                            ) -> {
                        if (hasDryItems && machine?.type != MachineType.WASHER_DRYER) "Ready to Dry" else "Ready to Fold"
                    }

                    isEverythingDone && (currentStatus == OrderStatus.DRYING || currentStatus == OrderStatus.DRIED) -> "Ready to Fold or Iron"
                    !isEverythingDone && isCycleComplete -> "Cycle Finished • More Pending"
                    isEverythingDone && isCycleComplete -> "Cycle Finished"
                    currentStatus == OrderStatus.WASHING -> "Currently Washing (${
                        KgFormatterUtils.formatDouble(
                            weightInAction,
                            weightFinished
                        )
                    }/${totalWeight}kg)"

                    currentStatus == OrderStatus.WASHING_AND_DRYING -> "Currently Washing and Drying (${
                        KgFormatterUtils.formatDouble(
                            weightInAction,
                            weightFinished
                        )
                    }/${totalWeight}kg)"

                    currentStatus == OrderStatus.DRIED
                            || currentStatus == OrderStatus.WASHED_AND_DRIED
                            || currentStatus == OrderStatus.WASHED -> "Ready to Fold"

                    else -> "Currently Drying (${
                        KgFormatterUtils.formatDouble(
                            weightInAction,
                            weightFinished
                        )
                    }/${totalWeight}kg)"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isEverythingDone || currentStatus == OrderStatus.FOLDING || currentStatus == OrderStatus.IRONING) MintGreen else MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (isEverythingDone && (currentStatus == OrderStatus.WASHING
                        || currentStatus == OrderStatus.WASHED)
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
                        currentStatus == OrderStatus.DRYING
                                || currentStatus == OrderStatus.DRIED
                                || currentStatus == OrderStatus.WASHED_AND_DRIED
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
                    text = if (selectedBatch != null) "Batch processing complete" 
                          else "Weight processed: ${KgFormatterUtils.formatDouble(weightFinished)}kg of ${totalWeight}kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (selectedBatch == null) {
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
                }
            } else if (currentStatus == OrderStatus.IRONED) {
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
            } else if (currentStatus == OrderStatus.FOLDING || currentStatus == OrderStatus.IRONING) {
                Text(
                    text = if (currentStatus == OrderStatus.FOLDING) "Tracking folding duration..." else "Tracking ironing duration...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (machine != null || (selectedBatch == null && relevantEndTime != null)) {
                if (selectedBatch == null && relevantEndTime != null) {
                    Text(
                        text = "Calculating completion for all active batches...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (machine != null) {
                    Text(
                        text = "assigned to ${machine.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!isCycleComplete && relevantEndTime != null) {
                    RemainingTimer(
                        endTime = relevantEndTime,
                        color = statusColor,
                        currentTime = currentTime,
                        machineId = machine?.machineId ?: "",
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
