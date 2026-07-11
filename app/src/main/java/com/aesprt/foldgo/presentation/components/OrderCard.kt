package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.aesprt.foldgo.core.util.OrderStatusUtils
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OrderCard(
    modifier: Modifier = Modifier,
    order: Order,
    machine: Machine? = null,
    onClick: () -> Unit,
    onTimerFinished: () -> Unit = {}
) {
    if (order.status == OrderStatus.WASHING || order.status == OrderStatus.DRYING) {
        machine?.endTime?.let { endTime ->
            LaunchedEffect(endTime) {
                val remaining = endTime - System.currentTimeMillis()
                if (remaining > 0) {
                    delay(remaining.milliseconds)
                }
                onTimerFinished()
            }
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = order.customerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = order.customerPhone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Total: ${PriceFormatter.format(order.totalAmount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Items: ${order.items.size}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val containerColor = OrderStatusUtils.getContainerColor(status)
    val text = OrderStatusUtils.getDisplayName(status)
    
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderCardPreview() {
    FoldGoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OrderCard(
                order = Order(
                    orderId = "1",
                    shopId = "shop1",
                    customerId = "cust1",
                    customerName = "John Doe",
                    customerPhone = "1234567890",
                    orderNumber = "FG-1024",
                    items = emptyList(),
                    totalAmount = 25.0,
                    paidAmount = 0.0,
                    status = OrderStatus.INTAKE,
                    intakePhotos = emptyList(),
                    machineId = null,
                    staffId = "staff1",
                    staffName = "Operator 1",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusChipPreview() {
    FoldGoTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(status = OrderStatus.INTAKE)
            StatusChip(status = OrderStatus.WASHING)
            StatusChip(status = OrderStatus.READY)
        }
    }
}
