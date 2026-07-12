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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.MachineCard
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.SummaryCard
import com.aesprt.foldgo.presentation.machines.components.MachineFilterSection
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MachineMatrixScreen(
    onAddNewMachine: () -> Unit,
    onMachineClick: (String) -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MachineMatrixContent(
        uiState = uiState,
        onAddNewMachine = onAddNewMachine,
        onMachineClick = onMachineClick,
        onFilterTypeChanged = viewModel::onFilterTypeChanged,
        onFinishCycle = viewModel::finishCycle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineMatrixContent(
    uiState: MachineUiState,
    onAddNewMachine: () -> Unit,
    onMachineClick: (String) -> Unit,
    onFilterTypeChanged: (MachineType?) -> Unit,
    onFinishCycle: (String) -> Unit
) {
    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        FoldGoLogo(
                            iconSize = 32.dp,
                            title = "Machines",
                            supportingText = "Manage your washing and drying machines",
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
                    onTypeSelected = onFilterTypeChanged
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FoldGoLoading()
                    }
                } else if (uiState.machines.isEmpty()) {
                    FoldGoEmptyState(
                        message = "No machines found",
                        description = if (uiState.filteredType != null)
                            "No ${uiState.filteredType.name.lowercase()}s are currently configured."
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
                                onTimerFinished = { onFinishCycle(machine.machineId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MachineMatrixContentPreview() {
    FoldGoTheme {
        MachineMatrixContent(
            uiState = MachineUiState(
                machines = listOf(
                    Machine("1", "shop1", "Washer 01", MachineType.WASHER, 8.0, MachineStatus.IDLE, 0L),
                    Machine("2", "shop1", "Dryer 01", MachineType.DRYER, 10.0, MachineStatus.BUSY, 0L, System.currentTimeMillis() + 600000)
                ),
                availableTypes = listOf(MachineType.WASHER, MachineType.DRYER)
            ),
            onAddNewMachine = {},
            onMachineClick = {},
            onFilterTypeChanged = {},
            onFinishCycle = {}
        )
    }
}
