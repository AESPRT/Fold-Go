package com.aesprt.foldgo.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun MachineCard(
    machine: Machine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val isMaintenanceDue = machine.cyclesCount >= 100

    LaunchedEffect(machine.status, machine.endTime) {
        if (machine.status == "BUSY" && machine.endTime != null) {
            while (currentTime < machine.endTime) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val statusColor = when (machine.status) {
        "IDLE" -> Color(0xFF4CAF50)
        "BUSY" -> MaterialTheme.colorScheme.primary
        "OUT_OF_ORDER" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

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
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.2f))
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
                        imageVector = Icons.Rounded.LocalLaundryService,
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
                    text = "${machine.type.lowercase().replaceFirstChar { it.uppercase() }} • ${machine.capacityKg}kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (machine.status == "BUSY" && machine.endTime != null) {
                    val remaining = machine.endTime - currentTime
                    if (remaining > 0) {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = statusColor
                            )
                            Text(
                                text = String.format("%02d:%02d remaining", minutes, seconds),
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            text = "Cycle Complete",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Status Badge
            Surface(
                color = statusColor,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = machine.status,
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
                    type = "WASHER",
                    capacityKg = 8.0,
                    status = "IDLE",
                    lastMaintenanceDate = 0L
                ),
                onClick = {}
            )
            MachineCard(
                machine = Machine(
                    machineId = "2",
                    shopId = "1",
                    name = "Dryer 02",
                    type = "DRYER",
                    capacityKg = 10.0,
                    status = "BUSY",
                    lastMaintenanceDate = 0L,
                    endTime = System.currentTimeMillis() + 600000,
                    cyclesCount = 105
                ),
                onClick = {}
            )
        }
    }
}
