package com.aesprt.foldgo.presentation.machines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.MachineCard
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.SummaryCard
import com.aesprt.foldgo.presentation.machines.components.MachineFilterSection
import com.aesprt.foldgo.domain.model.MachineStatus
import com.aesprt.foldgo.domain.model.MachineType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineMatrixScreen(
    onAddNewMachine: () -> Unit,
    onMachineClick: (String) -> Unit, // Added this parameter
    viewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                // Analytics Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val washingCount = uiState.machines.count { it.status == MachineStatus.BUSY && it.type == MachineType.WASHER }
                    val dryingCount = uiState.machines.count { it.status == MachineStatus.BUSY && it.type == MachineType.DRYER }
                    
                    SummaryCard(
                        title = "Washing",
                        value = washingCount.toString(),
                        icon = Icons.Rounded.LocalLaundryService,
                        iconColor = Color(0xFF03A9F4),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Drying",
                        value = dryingCount.toString(),
                        icon = Icons.Rounded.Air,
                        iconColor = Color(0xFFFFAB00),
                        modifier = Modifier.weight(1f)
                    )
                }

                MachineFilterSection(
                    selectedType = uiState.filteredType,
                    availableTypes = uiState.availableTypes,
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
                            "No ${uiState.filteredType?.name?.lowercase()}s are currently configured."
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
                                onClick = { onMachineClick(machine.machineId) },
                                onTimerFinished = { viewModel.finishCycle(machine.machineId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
