package com.aesprt.foldgo.presentation.order.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityPromptDialog(
    service: Service,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantity by remember { mutableStateOf(service.defaultQuantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Enter Quantity", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold 
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (${service.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toDoubleOrNull() ?: service.defaultQuantity
                    onConfirm(q)
                },
                shape = RoundedCornerShape(12.dp)
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
