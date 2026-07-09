package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
    val containerColor = when (status) {
        OrderStatus.INTAKE -> Color(0xFFFFAB00)
        OrderStatus.WASHING -> Color(0xFF03A9F4)
        OrderStatus.DRYING -> Color(0xFF03A9F4)
        OrderStatus.FOLDING -> Color(0xFF03A9F4)
        OrderStatus.READY -> Color(0xFF4CAF50)
        OrderStatus.DELIVERED -> Color(0xFF8BC34A)
    }
    
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
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
                    orderNumber = "FG-1024",
                    items = emptyList(),
                    totalAmount = 25.0,
                    paidAmount = 0.0,
                    status = OrderStatus.INTAKE,
                    intakePhotos = emptyList(),
                    machineId = null,
                    staffId = "staff1",
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
