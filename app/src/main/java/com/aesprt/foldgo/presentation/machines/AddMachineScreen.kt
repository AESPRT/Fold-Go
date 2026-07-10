package com.aesprt.foldgo.presentation.machines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.core.util.MachineUtils
import com.aesprt.foldgo.domain.model.MachineType
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddMachineScreen(
    onNavigateBack: () -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    AddMachineContent(
        onNavigateBack = onNavigateBack,
        onConfirmRegistration = { name, type, capacity ->
            viewModel.addMachine(name, type, capacity)
            onNavigateBack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMachineContent(
    onNavigateBack: () -> Unit,
    onConfirmRegistration: (String, MachineType, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(MachineType.WASHER) }
    var capacity by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    
    val equipmentTypes = remember { MachineType.entries }

    val scrollState = rememberScrollState()

    val secondaryColor = MachineUtils.getMachineTypeColor(type)

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Register Equipment",
                            style = MaterialTheme.typography.titleLarge,
                            letterSpacing = (-0.5).sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Unspecified,
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                        Button(
                            onClick = {
                                val cap = capacity.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && cap > 0) {
                                    onConfirmRegistration(name, type, cap)
                                }
                            },
                            enabled = name.isNotBlank() && capacity.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "Confirm Registration",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Dynamic Hero Visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(secondaryColor.copy(alpha = 0.15f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            color = secondaryColor,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = MachineUtils.getMachineIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = type.name,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = secondaryColor,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }

                // 1. Equipment Type Selection (Modern Chips)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Equipment Category",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        equipmentTypes.forEach { item ->
                            FilterChip(
                                selected = type == item,
                                onClick = { type = item },
                                label = { 
                                    Text(
                                        item.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelLarge
                                    ) 
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = secondaryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = secondaryColor,
                                    selectedLeadingIconColor = secondaryColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = type == item,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    selectedBorderColor = secondaryColor
                                )
                            )
                        }
                    }
                }

                // 2. Core Specifications
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Core Specifications", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Equipment Name", style = MaterialTheme.typography.titleSmall) },
                        placeholder = { Text("e.g. Speed Queen 01", style = MaterialTheme.typography.titleSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Settings, null, tint = secondaryColor) },
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            label = { Text("Capacity", style = MaterialTheme.typography.titleSmall) },
                            suffix = { Text("kg", style = MaterialTheme.typography.titleSmall) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Rounded.MonitorWeight, null, tint = secondaryColor) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand", style = MaterialTheme.typography.titleSmall) },
                            placeholder = { Text("e.g. LG", style = MaterialTheme.typography.titleSmall) },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Factory, null, tint = secondaryColor) },
                            singleLine = true
                        )
                    }
                }

                // 3. Technical Details (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Technical Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    
                    OutlinedTextField(
                        value = modelNumber,
                        onValueChange = { modelNumber = it },
                        label = { Text("Model Number", style = MaterialTheme.typography.titleSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Fingerprint, null, tint = secondaryColor) },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMachineScreenPreview() {
    FoldGoTheme {
        AddMachineContent(
            onNavigateBack = {},
            onConfirmRegistration = { _, _, _ -> }
        )
    }
}
