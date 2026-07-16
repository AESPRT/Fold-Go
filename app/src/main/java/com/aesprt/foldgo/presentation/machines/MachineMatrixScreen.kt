package com.aesprt.foldgo.presentation.machines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.MachineCard
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.SummaryCard
import com.aesprt.foldgo.presentation.machines.components.MachineFilterSection
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.core.util.DevicePreviews
import com.aesprt.foldgo.core.util.MachineUtils
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MachineMatrixScreen(
    onMachineClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: MachineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val isTablet = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    MachineMatrixContent(
        uiState = uiState,
        isTablet = isTablet,
        onMachineClick = { id ->
            if (isTablet) {
                viewModel.selectMachine(id)
            } else {
                onMachineClick(id)
            }
        },
        onFilterStatusChanged = viewModel::onFilterStatusChanged,
        onUpdateStatus = viewModel::updateStatus,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineMatrixContent(
    uiState: MachineUiState,
    isTablet: Boolean = false,
    onMachineClick: (String) -> Unit,
    onFilterStatusChanged: (MachineStatus?) -> Unit,
    onUpdateStatus: (String, MachineStatus) -> Unit = { _, _ -> },
    contentPadding: PaddingValues = PaddingValues()
) {
    ModernBackground {
        if (isTablet) {
            MachineMatrixTabletContent(
                uiState = uiState,
                onMachineClick = onMachineClick,
                onFilterStatusChanged = onFilterStatusChanged,
                onUpdateStatus = onUpdateStatus
            )
        } else {
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
                bottomBar = {
                    // Reserve space for the floating bottom bar so FAB is pushed up correctly
                    Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            vertical = padding.calculateTopPadding()
                        )
                ) {
                    // Analytics Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val washingCount = uiState.machines.count { it.status == MachineStatus.WASHING }
                        val dryingCount = uiState.machines.count { it.status == MachineStatus.DRYING }

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
                        selectedStatus = null, // TODO: Add state to UI state
                        onStatusSelected = onFilterStatusChanged
                    )

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            FoldGoLoading()
                        }
                    } else if (uiState.machines.isEmpty()) {
                        FoldGoEmptyState(
                            message = "No machines found",
                            description = "You haven't added any washing or drying machines yet.",
                            icon = Icons.Rounded.LocalLaundryService
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = contentPadding.calculateBottomPadding() + 16.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.machines) { machine ->
                                MachineCard(
                                    machine = machine,
                                    onClick = { onMachineClick(machine.machineId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MachineMatrixTabletContent(
    uiState: MachineUiState,
    onMachineClick: (String) -> Unit,
    onFilterStatusChanged: (MachineStatus?) -> Unit,
    onUpdateStatus: (String, MachineStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Panel: Summary, Filters, and Grid
        Column(modifier = Modifier.weight(0.6f)) {
            FoldGoLogo(
                iconSize = 32.dp,
                title = "Machines",
                supportingText = "Manage your washing and drying machines",
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val washingCount = uiState.machines.count { it.status == MachineStatus.WASHING }
                val dryingCount = uiState.machines.count { it.status == MachineStatus.DRYING }

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

            Spacer(modifier = Modifier.height(16.dp))

            MachineFilterSection(
                selectedStatus = null,
                onStatusSelected = onFilterStatusChanged,
                isWrap = true
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    FoldGoLoading()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.machines) { machine ->
                        val isSelected = uiState.selectedMachine?.machineId == machine.machineId
                        MachineCard(
                            machine = machine,
                            onClick = { onMachineClick(machine.machineId) },
                            modifier = Modifier.then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(24.dp)
                                ) else Modifier
                            )
                        )
                    }
                }
            }
        }

        // Right Panel: Details and Status Update
        Card(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            val machine = uiState.selectedMachine
            val order = uiState.selectedOrder

            if (machine != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = machine.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Compact Machine Card
                    Surface(
                        color = MachineUtils.getStatusColor(machine.status).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                color = MachineUtils.getStatusColor(machine.status).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        MachineUtils.getMachineIcon(machine.status),
                                        null,
                                        tint = MachineUtils.getStatusColor(machine.status)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    machine.status.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MachineUtils.getStatusColor(machine.status)
                                )
                                Text(
                                    "${machine.capacityKg}kg Capacity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Text(
                        "Update Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (order == null) {
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Warning, null, tint = Color(0xFFFBC02D))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "No order assigned to this machine",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF827717)
                                    )
                                    Text(
                                        "Create a new order and assign it here before updating status.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF827717)
                                    )
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onUpdateStatus(machine.machineId, MachineStatus.WASHING) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Wash") }
                                Button(
                                    onClick = { onUpdateStatus(machine.machineId, MachineStatus.DRYING) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Dry") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onUpdateStatus(machine.machineId, MachineStatus.IRONING) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Iron") }
                                Button(
                                    onClick = { onUpdateStatus(machine.machineId, MachineStatus.FOLDING) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Fold") }
                            }
                            Button(
                                onClick = { onUpdateStatus(machine.machineId, MachineStatus.READY) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) { Text("Mark Ready") }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(
                        onClick = { onUpdateStatus(machine.machineId, MachineStatus.IDLE) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set Idle")
                    }

                    OutlinedButton(
                        onClick = { onUpdateStatus(machine.machineId, MachineStatus.OUT_OF_ORDER) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Mark Out of Order")
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Select a machine to view details",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
fun MachineMatrixContentPreview() {
    FoldGoTheme {
        MachineMatrixContent(
            uiState = MachineUiState(
                machines = listOf(
                    Machine("1", "shop1", "Washer 01", 8.0, MachineStatus.IDLE, 0L),
                    Machine("2", "shop1", "Dryer 01", 10.0, MachineStatus.WASHING, 0L, null)
                )
            ),
            onMachineClick = {},
            onFilterStatusChanged = {}
        )
    }
}

@DevicePreviews
@Composable
fun MachineMatrixContentTabletPreview() {
    FoldGoTheme {
        MachineMatrixTabletContent(
            uiState = MachineUiState(
                machines = listOf(
                    Machine("1", "shop1", "Washer 01", 8.0, MachineStatus.IDLE, 0L),
                    Machine("2", "shop1", "Dryer 01", 10.0, MachineStatus.WASHING, 0L, null)
                )
            ),
            onMachineClick = {},
            onFilterStatusChanged = {},
            onUpdateStatus = { _, _ -> }
        )
    }
}
