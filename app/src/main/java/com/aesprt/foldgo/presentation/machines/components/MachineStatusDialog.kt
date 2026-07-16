package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.IntakeAmber

@Composable
fun MachineStatusDialog(
    machine: Machine,
    onDismiss: () -> Unit,
    onStatusChange: (MachineStatus) -> Unit
) {
    val hasAssignedOrder = machine.assignedOrderId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Status: ${machine.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!hasAssignedOrder) {
                    Surface(
                        color = IntakeAmber.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = IntakeAmber)
                            Text(
                                "No order assigned to this machine. Go to Dashboard and select a pending order to assign.",
                                style = MaterialTheme.typography.bodySmall,
                                color = IntakeAmber
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusOption(
                            title = "Washing",
                            subtitle = "Machine is currently washing",
                            icon = Icons.Rounded.LocalLaundryService,
                            color = Color(0xFF03A9F4),
                            onClick = { onStatusChange(MachineStatus.WASHING) }
                        )
                        StatusOption(
                            title = "Drying",
                            subtitle = "Machine is currently drying",
                            icon = Icons.Rounded.Air,
                            color = Color(0xFFFFAB00),
                            onClick = { onStatusChange(MachineStatus.DRYING) }
                        )
                        StatusOption(
                            title = "Ironing",
                            subtitle = "Staff is currently ironing",
                            icon = Icons.Rounded.Iron,
                            color = Color(0xFFFF5722),
                            onClick = { onStatusChange(MachineStatus.IRONING) }
                        )
                        StatusOption(
                            title = "Folding",
                            subtitle = "Staff is currently folding",
                            icon = Icons.Rounded.DryCleaning,
                            color = Color(0xFF9C27B0),
                            onClick = { onStatusChange(MachineStatus.FOLDING) }
                        )
                        StatusOption(
                            title = "Ready",
                            subtitle = "Order is processed and ready",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF4CAF50),
                            onClick = { onStatusChange(MachineStatus.READY) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onStatusChange(MachineStatus.IDLE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Set Idle", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onStatusChange(MachineStatus.OUT_OF_ORDER) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        border = BorderStroke(width = 1.dp, brush = SolidColor(Color(0xFFF44336)))
                    ) {
                        Text("Out of Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun StatusOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = color,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MachineStatusDialogPreview() {
    FoldGoTheme {
        MachineStatusDialog(
            machine = Machine("1", "shop1", "Washer 01", 8.0, MachineStatus.IDLE, 0L),
            onDismiss = {},
            onStatusChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusOptionPreview() {
    FoldGoTheme {
        StatusOption(
            title = "Set to Idle",
            subtitle = "Available for new orders",
            icon = Icons.Rounded.CheckCircle,
            color = Color(0xFF4CAF50),
            onClick = {}
        )
    }
}
