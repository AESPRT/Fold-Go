package com.aesprt.foldgo.presentation.machines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.core.util.MachineUtils
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineStatus
import com.aesprt.foldgo.domain.model.MachineType
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.machines.components.MachineStatusDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailScreen(
    machineId: String,
    onNavigateBack: () -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val machine = uiState.machines.find { it.machineId == machineId }
    var showStatusDialog by remember { mutableStateOf(false) }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(machine?.name ?: "Machine Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (machine == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.isLoading) FoldGoLoading() else Text("Machine not found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Status Hero Card
                    val statusColor = MachineUtils.getStatusColor(machine.status)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                color = statusColor,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = MachineUtils.getMachineIcon(machine.type),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(machine.status.name, fontWeight = FontWeight.Black, color = statusColor, letterSpacing = 1.sp)
                                Text("${machine.type.name} • ${machine.capacityKg}kg", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Analytics & Info
                    Text("Performance & Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailStatCard(
                            label = "Total Cycles",
                            value = machine.cyclesCount.toString(),
                            icon = Icons.Rounded.Loop,
                            modifier = Modifier.weight(1f)
                        )
                        DetailStatCard(
                            label = "Maintenance",
                            value = "Good", // Placeholder
                            icon = Icons.Rounded.Build,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Actions Section
                    Text("Management Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Button(
                        onClick = { showStatusDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Settings, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Update Status or Start Cycle", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.updateStatus(machineId, MachineStatus.OUT_OF_ORDER) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Rounded.Warning, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Mark Out of Order", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showStatusDialog && machine != null) {
        MachineStatusDialog(
            machine = machine,
            activeOrders = uiState.activeOrders,
            onDismiss = { showStatusDialog = false },
            onStatusChange = { status ->
                viewModel.updateStatus(machineId, MachineStatus.valueOf(status))
                showStatusDialog = false 
            },
            onStartCycle = { duration, orderId -> viewModel.startCycle(machineId, duration, orderId); showStatusDialog = false },
            onFinishCycle = { viewModel.finishCycle(machineId); showStatusDialog = false }
        )
    }
}

@Composable
fun DetailStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
