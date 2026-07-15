package com.aesprt.foldgo.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Iron
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aesprt.foldgo.domain.model.enums.BatchStatus
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.ui.theme.DeepOceanBlue
import com.aesprt.foldgo.ui.theme.IntakeAmber
import com.aesprt.foldgo.ui.theme.MintGreen
import com.aesprt.foldgo.ui.theme.MintGreenDark
import com.aesprt.foldgo.ui.theme.ProcessingSkyBlue
import com.aesprt.foldgo.ui.theme.ReadyEmeraldGreen

object OrderStatusUtils {
    fun getContainerColor(status: Any): Color = when (status) {
        is BatchStatus -> when (status) {
            BatchStatus.QUEUED -> IntakeAmber
            BatchStatus.WASHING -> MintGreen
            BatchStatus.DRYING -> ProcessingSkyBlue
            BatchStatus.FOLDING -> DeepOceanBlue
            BatchStatus.READY -> ReadyEmeraldGreen
        }
        is OrderStatus -> when (status) {
            OrderStatus.INTAKE -> IntakeAmber
            OrderStatus.WASHING, OrderStatus.WASHING_AND_DRYING -> MintGreen
            OrderStatus.DRYING -> ProcessingSkyBlue
            OrderStatus.FOLDING, OrderStatus.IRONING -> DeepOceanBlue
            OrderStatus.READY, OrderStatus.WASHED_AND_DRIED, OrderStatus.WASHED, OrderStatus.DRIED, OrderStatus.IRONED -> ReadyEmeraldGreen
            OrderStatus.DELIVERED -> MintGreenDark
        }
        else -> Color.Gray
    }

    fun getDisplayName(status: Any): String = when (status) {
        is BatchStatus -> status.name.uppercase()
        is OrderStatus -> when (status) {
            OrderStatus.WASHING_AND_DRYING -> "WASHING AND DRYING"
            OrderStatus.WASHED_AND_DRIED -> "WASHED AND DRIED"
            OrderStatus.WASHING -> "WASHING"
            OrderStatus.WASHED -> "WASHED"
            OrderStatus.DRYING -> "DRYING"
            OrderStatus.DRIED -> "DRIED"
            OrderStatus.IRONING -> "IRONING"
            OrderStatus.IRONED -> "IRONED"
            else -> status.name.uppercase()
        }
        else -> "UNKNOWN"
    }

    fun getOrderStatusLabel(status: OrderStatus): String = when (status) {
        OrderStatus.WASHED -> "Washed"
        OrderStatus.WASHING_AND_DRYING -> "Washing and Drying"
        OrderStatus.WASHED_AND_DRIED -> "Washed and Dried"
        OrderStatus.DRIED -> "Dried"
        OrderStatus.IRONED -> "Ironed"
        OrderStatus.READY -> "Ready"
        OrderStatus.DELIVERED -> "Delivered"
        OrderStatus.FOLDING -> "Folding"
        OrderStatus.IRONING -> "Ironing"
        OrderStatus.WASHING -> "Washing"
        OrderStatus.DRYING -> "Drying"
        OrderStatus.INTAKE -> "Intake"
    }

    fun getStatusIcon(status: Any): ImageVector = when (status) {
        is BatchStatus -> when (status) {
            BatchStatus.WASHING -> Icons.Rounded.LocalLaundryService
            BatchStatus.DRYING -> Icons.Rounded.Air
            else -> Icons.Rounded.Checkroom
        }
        is OrderStatus -> when (status) {
            OrderStatus.WASHING, OrderStatus.WASHED, OrderStatus.WASHING_AND_DRYING, OrderStatus.WASHED_AND_DRIED -> Icons.Rounded.LocalLaundryService
            OrderStatus.DRYING, OrderStatus.DRIED -> Icons.Rounded.Air
            OrderStatus.IRONING, OrderStatus.IRONED -> Icons.Rounded.Iron
            else -> Icons.Rounded.Checkroom
        }
        else -> Icons.Rounded.Checkroom
    }
}