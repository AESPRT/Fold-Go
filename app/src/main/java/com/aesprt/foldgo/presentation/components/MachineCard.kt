package com.aesprt.foldgo.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.core.util.MachineUtils
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@Composable
fun MachineCard(
    modifier: Modifier = Modifier,
    machine: Machine,
    onClick: () -> Unit
) {
    val isMaintenanceDue = machine.cyclesCount >= 100

    val isWorking = machine.status == MachineStatus.WASHING || machine.status == MachineStatus.DRYING || 
                   machine.status == MachineStatus.IRONING || machine.status == MachineStatus.FOLDING

    val statusColor = MachineUtils.getStatusColor(machine.status)
    val icon = MachineUtils.getMachineIcon(machine.status)

    val containerColor by animateColorAsState(
        targetValue = statusColor.copy(alpha = 0.04f),
        animationSpec = tween(500),
        label = "containerColor"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(statusColor.copy(alpha = 0.2f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Surface(
                modifier = Modifier.size(64.dp),
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = machine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isMaintenanceDue) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Maintenance Due",
                            tint = Color(0xFFFFAB00),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = "${machine.capacityKg}kg Capacity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )

                if (isWorking) {
                    Text(
                        text = "In Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Status Badge
            Surface(
                color = statusColor,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = machine.status.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MachineCardPreview() {
    FoldGoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MachineCard(
                machine = Machine(
                    machineId = "1",
                    shopId = "1",
                    name = "Washer 01",
                    capacityKg = 8.0,
                    status = MachineStatus.IDLE,
                    lastMaintenanceDate = 0L
                ),
                onClick = {}
            )
            MachineCard(
                machine = Machine(
                    machineId = "2",
                    shopId = "1",
                    name = "Dryer 02",
                    capacityKg = 10.0,
                    status = MachineStatus.WASHING,
                    lastMaintenanceDate = 0L,
                    endTime = System.currentTimeMillis() + 600000,
                    cyclesCount = 105
                ),
                onClick = {}
            )
        }
    }
}
