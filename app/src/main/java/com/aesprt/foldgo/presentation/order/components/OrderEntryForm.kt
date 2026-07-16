package com.aesprt.foldgo.presentation.order.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.order.OrderEntryUiState

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import com.aesprt.foldgo.domain.model.Customer

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryForm(
    uiState: OrderEntryUiState,
    onCustomerNameChange: (String) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onDeliveryFeeChange: (String) -> Unit,
    onToggleService: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleAddOn: (AddOn) -> Unit,
    onAssignMachine: (Machine) -> Unit,
    onCustomerSelect: (Customer) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            "Customer Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded && uiState.suggestedCustomers.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = uiState.customerName,
                onValueChange = {
                    onCustomerNameChange(it)
                    expanded = true
                },
                label = { Text("Customer Name") },
                placeholder = { Text("e.g. Maria Santos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded && uiState.suggestedCustomers.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                uiState.suggestedCustomers.forEach { customer ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(customer.name, fontWeight = FontWeight.Bold)
                                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        onClick = {
                            onCustomerSelect(customer)
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.phoneTextFieldValue,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            placeholder = { Text("e.g. 09171234567") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            )
        )

        OutlinedTextField(
            value = uiState.customerAddress,
            onValueChange = onCustomerAddressChange,
            label = { Text("Customer Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            "Delivery Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DeliveryMethod.entries.forEach { method ->
                val isSelected = uiState.deliveryMethod == method
                FilterChip(
                    selected = isSelected,
                    onClick = { onDeliveryMethodChange(method) },
                    label = {
                        Text(method.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        if (uiState.deliveryMethod == DeliveryMethod.DELIVERY) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Delivery Fee",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableDeliveryFees.forEach { fee ->
                        val isSelected = uiState.deliveryFee == fee.toString()
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDeliveryFeeChange(fee.toString()) },
                            label = { Text("P$fee") },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.deliveryFee,
                    onValueChange = onDeliveryFeeChange,
                    label = { Text("Custom Delivery Fee") },
                    prefix = { Text("₱") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        Text(
            "Service Items",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.availableServices.forEach { service ->
                val isSelected = uiState.selectedServiceIds.contains(service.serviceId)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleService(service.serviceId) },
                    label = { 
                        Column {
                            Text(service.name, fontWeight = FontWeight.Bold)
                            Text("P${service.pricePerUnit} — ${service.defaultQuantity}${service.unit.lowercase()}", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        val hasBundleSelected = uiState.availableServices
            .filter { uiState.selectedServiceIds.contains(it.serviceId) }
            .any { it.type == ServiceType.BUNDLE }

        if (uiState.selectedServiceIds.isNotEmpty() && !hasBundleSelected) {
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = { Text("Weight (KG)") },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Text("kg", modifier = Modifier.padding(end = 12.dp)) }
            )
        }

        Text(
            "Add-Ons",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.availableAddOns.forEach { addOn ->
                val isSelected = uiState.selectedAddOns.contains(addOn)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleAddOn(addOn) },
                    label = { Text("${addOn.name} +P${addOn.price}") },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Assign Machine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Select an idle washer/dryer",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.availableMachines.isEmpty()) {
            Text(
                "No idle machines available. Please wait for a machine to become free.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.availableMachines.forEach { machine ->
                    val isSelected = uiState.assignedMachine?.machineId == machine.machineId
                    Card(
                        onClick = { onAssignMachine(machine) },
                        modifier = Modifier
                            .width(180.dp)
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.LocalLaundryService,
                                    contentDescription = null,
                                    modifier = Modifier.padding(6.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    machine.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "IDLE",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}