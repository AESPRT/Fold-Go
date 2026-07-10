package com.aesprt.foldgo.presentation.machines

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.presentation.components.ModernBackground
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMachineScreen(
    onNavigateBack: () -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("WASHER") }
    var capacity by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    
    var showCustomTypeDialog by remember { mutableStateOf(false) }
    var customType by remember { mutableStateOf("") }
    val equipmentTypes = remember { mutableStateListOf("WASHER", "DRYER", "IRON", "STEAMER") }

    val scrollState = rememberScrollState()

    val secondaryColor = when (type) {
        "WASHER" -> Color(0xFF03A9F4)
        "DRYER" -> Color(0xFFFFAB00)
        "IRON" -> Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.primary
    }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Register Equipment", 
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Box(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                        Button(
                            onClick = {
                                val cap = capacity.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && cap > 0) {
                                    viewModel.addMachine(name, type, cap)
                                    onNavigateBack()
                                }
                            },
                            enabled = name.isNotBlank() && capacity.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Confirm Registration", fontWeight = FontWeight.Bold)
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
                                    imageVector = when(type) {
                                        "WASHER" -> Icons.Rounded.LocalLaundryService
                                        "DRYER" -> Icons.Rounded.Air
                                        "IRON" -> Icons.Rounded.Checkroom
                                        else -> Icons.Rounded.Settings
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = type.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = secondaryColor,
                            letterSpacing = 2.sp
                        )
                    }
                }

                // 1. Equipment Type Selection (Modern Chips)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Equipment Category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showCustomTypeDialog = true }) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Custom")
                        }
                    }
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        equipmentTypes.forEach { item ->
                            FilterChip(
                                selected = type == item,
                                onClick = { type = item },
                                label = { Text(item.lowercase().replaceFirstChar { it.uppercase() }) },
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
                    Text("Core Specifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Equipment Name") },
                        placeholder = { Text("e.g. Speed Queen 01") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Settings, null, tint = secondaryColor) },
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            label = { Text("Capacity") },
                            suffix = { Text("kg") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Rounded.MonitorWeight, null, tint = secondaryColor) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            placeholder = { Text("e.g. LG") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Factory, null, tint = secondaryColor) },
                            singleLine = true
                        )
                    }
                }

                // 3. Technical Details (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Technical Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = modelNumber,
                        onValueChange = { modelNumber = it },
                        label = { Text("Model Number") },
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

    if (showCustomTypeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTypeDialog = false },
            title = { Text("Add Custom Equipment Type") },
            text = {
                OutlinedTextField(
                    value = customType,
                    onValueChange = { customType = it },
                    label = { Text("Type Name") },
                    placeholder = { Text("e.g. Iron, Steamer") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customType.isNotBlank()) {
                            val upperType = customType.uppercase().trim()
                            if (!equipmentTypes.contains(upperType)) {
                                equipmentTypes.add(upperType)
                            }
                            type = upperType
                            customType = ""
                            showCustomTypeDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Type")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
