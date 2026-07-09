package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddMachineDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, capacity: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("WASHER") } // Default
    var capacity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Machine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Machine Name (e.g. Washer 01)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Machine Type", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == "WASHER",
                            onClick = { type = "WASHER" },
                            label = { Text("Washer") }
                        )
                        FilterChip(
                            selected = type == "DRYER",
                            onClick = { type = "DRYER" },
                            label = { Text("Dryer") }
                        )
                    }
                }

                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacity.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && cap > 0) {
                        onConfirm(name, type, cap)
                    }
                },
                enabled = name.isNotBlank() && capacity.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
