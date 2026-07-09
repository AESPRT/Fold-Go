package com.aesprt.foldgo.presentation.machines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.MachineCard
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.machines.components.AddMachineDialog
import com.aesprt.foldgo.presentation.machines.components.MachineFilterSection
import com.aesprt.foldgo.presentation.machines.components.MachineStatusDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineMatrixScreen(
    onAddNewMachine: () -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMachine by remember { mutableStateOf<Machine?>(null) }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Machine Matrix",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddNewMachine,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Machine")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                MachineFilterSection(
                    selectedType = uiState.filteredType,
                    onTypeSelected = viewModel::onFilterTypeChanged
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FoldGoLoading()
                    }
                } else if (uiState.machines.isEmpty()) {
                    FoldGoEmptyState(
                        message = "No machines found",
                        description = if (uiState.filteredType != null) 
                            "No ${uiState.filteredType?.lowercase()}s are currently configured."
                            else "You haven't added any washing or drying machines yet.",
                        icon = Icons.Rounded.LocalLaundryService
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.machines) { machine ->
                            MachineCard(
                                machine = machine,
                                onClick = { selectedMachine = machine }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedMachine?.let { machine ->
        MachineStatusDialog(
            machine = machine,
            activeOrders = uiState.activeOrders,
            onDismiss = { selectedMachine = null },
            onStatusChange = { newStatus ->
                viewModel.updateStatus(machine.machineId, newStatus)
                selectedMachine = null
            },
            onStartCycle = { duration, orderId ->
                viewModel.startCycle(machine.machineId, duration, orderId)
                selectedMachine = null
            },
            onFinishCycle = {
                viewModel.finishCycle(machine.machineId)
                selectedMachine = null
            }
        )
    }
}
