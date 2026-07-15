package com.aesprt.foldgo.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Iron
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aesprt.foldgo.domain.model.enums.MachineStatus

object MachineUtils {
    fun getStatusColor(status: MachineStatus): Color = when (status) {
        MachineStatus.IDLE -> Color(0xFF4CAF50)
        MachineStatus.QUEUED -> Color(0xFF607D8B)
        MachineStatus.WASHING -> Color(0xFF03A9F4)
        MachineStatus.DRYING -> Color(0xFFFFAB00)
        MachineStatus.IRONING -> Color(0xFFFF5722)
        MachineStatus.FOLDING -> Color(0xFF9C27B0)
        MachineStatus.READY -> Color(0xFF4CAF50)
        MachineStatus.OUT_OF_ORDER -> Color(0xFFF44336)
    }

    fun getMachineIcon(status: MachineStatus): ImageVector = when (status) {
        MachineStatus.WASHING -> Icons.Rounded.LocalLaundryService
        MachineStatus.DRYING -> Icons.Rounded.Air
        MachineStatus.IRONING -> Icons.Rounded.Iron
        MachineStatus.FOLDING -> Icons.Rounded.Iron
        MachineStatus.READY -> Icons.Rounded.LocalLaundryService
        else -> Icons.Rounded.LocalLaundryService
    }
}
