package com.aesprt.foldgo.presentation.machines

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.presentation.components.ModernBackground
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMachineScreen(
    onNavigateBack: () -> Unit,
    viewModel: MachineViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("WASHER") }
    var capacity by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("New Machine", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding() // Avoid keyboard blocking
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Machine Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Machine Name") },
                        placeholder = { Text("e.g. Washer 01") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.Settings, null) },
                        singleLine = true
                    )

                    // Type Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Equipment Type",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SegmentedButton(
                                selected = type == "WASHER",
                                onClick = { type = "WASHER" },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                icon = { SegmentedButtonDefaults.Icon(active = type == "WASHER") }
                            ) {
                                Text("Washer")
                            }
                            SegmentedButton(
                                selected = type == "DRYER",
                                onClick = { type = "DRYER" },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                icon = { SegmentedButtonDefaults.Icon(active = type == "DRYER") }
                            ) {
                                Text("Dryer")
                            }
                        }
                    }

                    // Capacity Input
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        suffix = { Text("kg") },
                        placeholder = { Text("8.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.MonitorWeight, null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Action Area (Bottom)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val cap = capacity.toDoubleOrNull() ?: 0.0
                                    if (name.isNotBlank() && cap > 0) {
                                        viewModel.addMachine(name, type, cap)
                                        onNavigateBack()
                                    }
                                },
                                enabled = name.isNotBlank() && capacity.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Register Machine", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
