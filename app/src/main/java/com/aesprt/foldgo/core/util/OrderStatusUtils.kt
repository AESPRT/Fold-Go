package com.aesprt.foldgo.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DryCleaning
import androidx.compose.material.icons.rounded.Iron
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aesprt.foldgo.domain.model.enums.BatchStatus
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.ui.theme.*

object OrderStatusUtils {
    fun getContainerColor(status: OrderStatus): Color {
        return when(status) {
            OrderStatus.PENDING -> ErrorCrimsonRed
            OrderStatus.QUEUED -> SurfaceVariantDark
            OrderStatus.WASHING -> DeepOceanBlue
            OrderStatus.DRYING -> IntakeAmber
            OrderStatus.FOLDING, OrderStatus.IRONING -> Purple40
            OrderStatus.READY -> ReadyEmeraldGreen
            OrderStatus.DELIVERED -> MintGreen
            else -> ErrorCrimsonRed
        }
    }

    fun getDisplayName(status: Any): String = when (status) {
        is BatchStatus -> status.name.uppercase()
        is OrderStatus -> when (status) {
            OrderStatus.WASHING -> "WASHING"
            OrderStatus.DRYING -> "DRYING"
            OrderStatus.IRONING -> "IRONING"
            OrderStatus.FOLDING -> "FOLDING"
            else -> status.name.uppercase()
        }
        else -> "UNKNOWN"
    }

    fun getStatusIcon(status: OrderStatus, deliveryMethod: DeliveryMethod): ImageVector = when (status) {
        OrderStatus.WASHING -> Icons.Rounded.LocalLaundryService
        OrderStatus.DRYING -> Icons.Rounded.Air
        OrderStatus.FOLDING -> Icons.Rounded.DryCleaning
        OrderStatus.IRONING -> Icons.Rounded.Iron
        OrderStatus.READY -> if(deliveryMethod == DeliveryMethod.DELIVERY) Icons.Rounded.DeliveryDining else Icons.Rounded.Storefront
        else -> Icons.Rounded.Start
    }
}