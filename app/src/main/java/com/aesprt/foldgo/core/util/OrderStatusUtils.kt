package com.aesprt.foldgo.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Iron
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aesprt.foldgo.domain.model.MachineType
import com.aesprt.foldgo.domain.model.OrderStatus

object OrderStatusUtils {
    fun getContainerColor(status: OrderStatus): Color = when (status) {
        OrderStatus.INTAKE -> Color(0xFFFFAB00)
        OrderStatus.WASHING -> Color(0xFF03A9F4)
        OrderStatus.WASHED -> Color(0xFF4CAF50)
        OrderStatus.DRYING -> Color(0xFF03A9F4)
        OrderStatus.DRIED -> Color(0xFF4CAF50)
        OrderStatus.IRONING -> Color(0xFF03A9F4)
        OrderStatus.IRONED -> Color(0xFF4CAF50)
        OrderStatus.FOLDING -> Color(0xFF03A9F4)
        OrderStatus.READY -> Color(0xFF4CAF50)
        OrderStatus.DELIVERED -> Color(0xFF8BC34A)
    }

    fun getDisplayName(status: OrderStatus): String = when (status) {
        OrderStatus.WASHED -> "READY TO DRY"
        OrderStatus.DRIED -> "READY TO IRON/FOLD"
        OrderStatus.IRONED -> "READY TO FOLD"
        else -> status.name.uppercase()
    }

    fun getStatusIcon(status: OrderStatus): ImageVector = when (status) {
        OrderStatus.WASHING, OrderStatus.WASHED -> Icons.Rounded.LocalLaundryService
        OrderStatus.DRYING, OrderStatus.DRIED -> Icons.Rounded.Air
        OrderStatus.IRONING, OrderStatus.IRONED -> Icons.Rounded.Iron
        else -> Icons.Rounded.Checkroom
    }
}