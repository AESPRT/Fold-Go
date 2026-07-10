package com.aesprt.foldgo.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Iron
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aesprt.foldgo.domain.model.MachineStatus
import com.aesprt.foldgo.domain.model.MachineType

object MachineUtils {
    fun getStatusColor(status: MachineStatus): Color = when (status) {
        MachineStatus.IDLE -> Color(0xFF4CAF50)
        MachineStatus.BUSY -> Color(0xFF03A9F4) // Should ideally use MaterialTheme.colorScheme.primary, but using hardcoded for simplicity in util or pass context
        MachineStatus.OUT_OF_ORDER -> Color(0xFFF44336)
    }

    fun getMachineIcon(type: MachineType): ImageVector = when (type) {
        MachineType.WASHER -> Icons.Rounded.LocalLaundryService
        MachineType.DRYER -> Icons.Rounded.Air
        MachineType.IRON -> Icons.Rounded.Iron
        MachineType.STEAMER -> Icons.Rounded.Settings
    }

    fun getMachineTypeColor(type: MachineType): Color = when (type) {
        MachineType.WASHER -> Color(0xFF03A9F4)
        MachineType.DRYER -> Color(0xFFFFAB00)
        MachineType.IRON -> Color(0xFFE91E63)
        MachineType.STEAMER -> Color(0xFF9C27B0)
    }

    fun getMachineSelectionMenuMessage(type: MachineType): String = when (type) {
        MachineType.WASHER -> "Select a Washer"
        MachineType.DRYER -> "Select a Dryer"
        MachineType.IRON -> "Select an Iron"
        MachineType.STEAMER -> "Select a Steamer"
    }
}