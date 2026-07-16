package com.aesprt.foldgo.presentation.order.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, qty: Double, unit: String, price: Double, type: ServiceType, saveAsPreset: Boolean) -> Unit,
    title: String = "Add Service",
    confirmText: String = "Add to Order",
    showSaveAsPreset: Boolean = true
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1.0") }
    var unit by remember { mutableStateOf("KG") }
    var price by remember { mutableStateOf("") }
    var isBundle by remember { mutableStateOf(false) }
    var saveAsPreset by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name", style = MaterialTheme.typography.bodyMedium) },
                    placeholder = { Text("e.g. Special Wash", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Service Type Selection (Per Unit vs Bundle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isBundle,
                        onClick = { 
                            isBundle = false 
                            unit = "KG"
                        },
                        label = { Text("Per Unit (KG/PCS)") },
                        leadingIcon = if (!isBundle) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = isBundle,
                        onClick = { 
                            isBundle = true 
                            unit = "KG"
                        },
                        label = { Text("Per Bundle") },
                        leadingIcon = if (isBundle) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Qty", style = MaterialTheme.typography.bodyMedium) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(if (isBundle) "Bundle Price" else "Price per Unit", style = MaterialTheme.typography.bodyMedium) },
                    prefix = { Text("₱", style = MaterialTheme.typography.bodyMedium) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (showSaveAsPreset) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Checkbox(
                            checked = saveAsPreset,
                            onCheckedChange = { saveAsPreset = it }
                        )
                        Text(
                            "Save as predefined service",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qty.toDoubleOrNull() ?: 1.0
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onConfirm(name, q, unit, p, if (isBundle) ServiceType.BUNDLE else ServiceType.PER_KG, saveAsPreset)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ServiceAddDialogPreview() {
    FoldGoTheme {
        ServiceAddDialog(
            onDismiss = {},
            onConfirm = { _, _, _, _, _, _ -> }
        )
    }
}
