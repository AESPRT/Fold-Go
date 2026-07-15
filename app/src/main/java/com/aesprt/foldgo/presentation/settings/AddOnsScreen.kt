package com.aesprt.foldgo.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.enums.ServiceScope
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOnsScreen(
    onNavigateBack: () -> Unit
) {
    var addOns by remember {
        mutableStateOf(
            listOf(
                AddOn("ao1", "Fabric Softener", "Adds softener to wash cycle", 30.0, ServiceScope.ALL, true),
                AddOn("ao2", "Extra Rinse", "One additional rinse cycle", 25.0, ServiceScope.WASH_ONLY, true),
                AddOn("ao3", "Express Service", "Ready in under 2 hours", 100.0, ServiceScope.ALL, true),
                AddOn("ao4", "Stain Treatment", "Pre-treat visible stains", 50.0, ServiceScope.ALL, true)
            )
        )
    }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Add-Ons Management", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel: List
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Available Add-Ons",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(addOns) { addOn ->
                                AddOnListItem(
                                    addOn = addOn,
                                    onToggle = { active ->
                                        addOns = addOns.map { if (it.id == addOn.id) it.copy(isActive = active) else it }
                                    }
                                )
                            }
                        }
                    }
                }

                // Right Panel: Create Form (Tablet View)
                Card(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    AddOnForm()
                }
            }
        }
    }
}

@Composable
fun AddOnListItem(
    addOn: AddOn,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(addOn.name, fontWeight = FontWeight.Bold)
                    Text(
                        "P${addOn.price}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    addOn.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = addOn.isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddOnForm() {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf(ServiceScope.ALL) }
    var isActive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Create New Add-On", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Add-On Name") },
            placeholder = { Text("e.g. Hypoallergenic Detergent") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Short description shown to staff/customer") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price (PHP)") },
            placeholder = { Text("e.g. 30.00") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = { Text("P") }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Applies To", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServiceScope.entries.forEach { s ->
                    FilterChip(
                        selected = scope == s,
                        onClick = { scope = s },
                        label = { Text(s.name.replace("_", " ").lowercase().capitalize()) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Active", fontWeight = FontWeight.Bold)
            Switch(checked = isActive, onCheckedChange = { isActive = it })
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Save logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Add-On", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun AddOnsScreenPreview() {
    FoldGoTheme {
        AddOnsScreen(onNavigateBack = {})
    }
}
