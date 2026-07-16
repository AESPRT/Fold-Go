package com.aesprt.foldgo.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.DryCleaning
import androidx.compose.material.icons.rounded.Iron
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.ui.theme.*

object MachineUtils {
    fun getStatusColor(status: MachineStatus): Color = when (status) {
        MachineStatus.IDLE -> MintGreen
        MachineStatus.QUEUED -> SurfaceVariantDark
        MachineStatus.WASHING -> DeepOceanBlue
        MachineStatus.DRYING -> IntakeAmber
        MachineStatus.IRONING, MachineStatus.FOLDING -> Purple40
        MachineStatus.READY -> ReadyEmeraldGreen
        MachineStatus.OUT_OF_ORDER -> ErrorCrimsonRed
    }

    fun getMachineIcon(status: MachineStatus): ImageVector = when (status) {
        MachineStatus.WASHING -> Icons.Rounded.LocalLaundryService
        MachineStatus.DRYING -> Icons.Rounded.Air
        MachineStatus.IRONING -> Icons.Rounded.Iron
        MachineStatus.FOLDING -> Icons.Rounded.DryCleaning
        MachineStatus.READY -> Icons.Rounded.LocalLaundryService
        else -> Icons.Rounded.LocalLaundryService
    }
}
