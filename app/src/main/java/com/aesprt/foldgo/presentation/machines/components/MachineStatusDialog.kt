package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus

@Composable
fun MachineStatusDialog(
    machine: Machine,
    activeOrders: List<Order>,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
    onStartCycle: (Int, String?) -> Unit,
    onFinishCycle: () -> Unit
) {
    var showStartCycleConfig by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (showStartCycleConfig) "Start Cycle: ${machine.name}" else "Update Status: ${machine.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            if (showStartCycleConfig) {
                StartCycleConfig(
                    machineType = machine.type,
                    activeOrders = activeOrders,
                    selectedOrderId = selectedOrderId,
                    onOrderSelected = { selectedOrderId = it },
                    duration = duration,
                    onDurationChange = { duration = it }
                )
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (machine.status == "BUSY") {
                        StatusOption(
                            title = "Finish Cycle",
                            subtitle = "Complete current operation",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF4CAF50),
                            onClick = onFinishCycle
                        )
                    } else {
                        StatusOption(
                            title = "Set to Idle",
                            subtitle = "Available for new orders",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF4CAF50),
                            onClick = { onStatusChange("IDLE") }
                        )
                        StatusOption(
                            title = "Start Cycle",
                            subtitle = "Begin washing or drying",
                            icon = Icons.Rounded.PlayArrow,
                            color = Color(0xFF03A9F4),
                            onClick = { showStartCycleConfig = true }
                        )
                    }
                    StatusOption(
                        title = "Out of Order",
                        subtitle = "Requires maintenance",
                        icon = Icons.Rounded.Block,
                        color = Color(0xFFF44336),
                        onClick = { onStatusChange("OUT_OF_ORDER") }
                    )
                }
            }
        },
        confirmButton = {
            if (showStartCycleConfig) {
                Button(
                    onClick = { onStartCycle(duration.toIntOrNull() ?: 30, selectedOrderId) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = if (showStartCycleConfig) { { showStartCycleConfig = false } } else onDismiss) { 
                Text(if (showStartCycleConfig) "Back" else "Cancel") 
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartCycleConfig(
    machineType: String,
    activeOrders: List<Order>,
    selectedOrderId: String?,
    onOrderSelected: (String?) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = duration,
            onValueChange = onDurationChange,
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        var expanded by remember { mutableStateOf(false) }
        val selectedOrder = activeOrders.find { it.orderId == selectedOrderId }

        // Filter orders based on machine type and current status
        val validOrders = activeOrders.filter { order ->
            when (machineType) {
                "WASHER" -> order.status == OrderStatus.INTAKE
                "DRYER" -> order.status == OrderStatus.WASHED
                else -> false
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedOrder?.orderNumber ?: "Select Order (Optional)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Associated Order") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onOrderSelected(null)
                        expanded = false
                    }
                )
                validOrders.forEach { order ->
                    DropdownMenuItem(
                        text = { Text("${order.orderNumber} - ${order.customerName}") },
                        onClick = {
                            onOrderSelected(order.orderId)
                            expanded = false
                        }
                    )
                }
                
                if (validOrders.isEmpty()) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                if (machineType == "WASHER") "No Intake Orders" else "No Washed Orders",
                                color = MaterialTheme.colorScheme.outline
                            ) 
                        },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
            }
        }
        
        // Validation Warning
        val ordersInProgress = activeOrders.filter { it.status == OrderStatus.WASHING || it.status == OrderStatus.DRYING }
        if (ordersInProgress.isNotEmpty()) {
            Text(
                text = "Note: Active orders in cycles are hidden to prevent duplicates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = color,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                }
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
