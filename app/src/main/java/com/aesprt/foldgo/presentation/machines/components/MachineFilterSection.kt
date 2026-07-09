package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MachineFilterSection(
    selectedType: String?,
    onTypeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selectedType == "WASHER",
            onClick = { onTypeSelected("WASHER") },
            label = { Text("Washers") }
        )
        FilterChip(
            selected = selectedType == "DRYER",
            onClick = { onTypeSelected("DRYER") },
            label = { Text("Dryers") }
        )
    }
}
