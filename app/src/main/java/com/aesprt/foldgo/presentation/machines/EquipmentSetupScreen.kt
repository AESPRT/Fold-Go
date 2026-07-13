package com.aesprt.foldgo.presentation.machines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.MachineUtils
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun EquipmentSetupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddMachine: () -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    EquipmentSetupContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToAddMachine = onNavigateToAddMachine
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSetupContent(
    uiState: MachineUiState,
    onNavigateBack: () -> Unit,
    onNavigateToAddMachine: () -> Unit
) {
    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Equipment Setup", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddMachine,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Add Equipment", fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    FoldGoLoading()
                }
            } else if (uiState.machines.isEmpty()) {
                FoldGoEmptyState(
                    message = "No equipment registered",
                    description = "Add your first washing machine or dryer to start managing your shop's operations.",
                    icon = Icons.Rounded.LocalLaundryService
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Registered Equipment",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                    }

                    items(uiState.machines) { machine ->
                        EquipmentListItem(
                            machine = machine
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentListItem(machine: Machine) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Machine Type Icon
            val typeColor = MachineUtils.getMachineTypeColor(machine.type)
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = typeColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MachineUtils.getMachineIcon(machine.type),
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${machine.capacityKg} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${machine.type.name.lowercase().replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Indicator
            Surface(
                color = MachineUtils.getStatusColor(machine.status).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = machine.status.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MachineUtils.getStatusColor(machine.status),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EquipmentSetupPreview() {
    FoldGoTheme {
        EquipmentSetupContent(
            uiState = MachineUiState(
                machines = listOf(
                    Machine("1", "shop1", "Washer 01", MachineType.WASHER, 8.0, MachineStatus.IDLE, 0L),
                    Machine("2", "shop1", "Dryer 02", MachineType.DRYER, 10.0, MachineStatus.BUSY, 0L)
                )
            ),
            onNavigateBack = {},
            onNavigateToAddMachine = {}
        )
    }
}
