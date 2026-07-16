package com.aesprt.foldgo.presentation.order.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.OrderStatusUtils
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.ui.theme.MintGreen

@Composable
fun ActiveCycleCard(
    order: Order,
    machine: Machine?
) {
    val totalWeight = order.items.sumOf { it.quantity }.coerceAtLeast(0.1)
    val currentStatus = order.status

    val isEverythingDone = currentStatus == OrderStatus.READY

    val statusColor = if (isEverythingDone || currentStatus == OrderStatus.FOLDING) Color(0xFF4CAF50) else OrderStatusUtils.getContainerColor(
            currentStatus
        )

    val isProcessing = currentStatus == OrderStatus.WASHING ||
            currentStatus == OrderStatus.DRYING ||
            currentStatus == OrderStatus.IRONING ||
            currentStatus == OrderStatus.FOLDING

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
                    imageVector = OrderStatusUtils.getStatusIcon(currentStatus, order.deliveryMethod),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .then(
                            if (isProcessing) Modifier.rotate(rotation) else Modifier
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

            val deliveryStatus = when {
                order.deliveryMethod == DeliveryMethod.DELIVERY -> "Delivered"
                else -> "Pick Up"
            }
            Text(
                text = when (currentStatus) {
                    OrderStatus.FOLDING -> "Folding in Progress"
                    OrderStatus.WASHING -> "Currently Washing (${totalWeight}kg)"
                    OrderStatus.DRYING -> "Currently Drying (${totalWeight}kg)"
                    OrderStatus.IRONING -> "Currently Ironing (${totalWeight}kg)"
                    OrderStatus.READY -> "Ready to $deliveryStatus (${totalWeight}kg)"
                    else -> "Currently Processing (${totalWeight}kg)"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isEverythingDone || currentStatus == OrderStatus.FOLDING) MintGreen else MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (machine != null) {
                Text(
                    text = "assigned to ${machine.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
