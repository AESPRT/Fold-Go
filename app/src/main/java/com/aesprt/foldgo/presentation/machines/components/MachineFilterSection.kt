package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.aesprt.foldgo.domain.model.MachineType

@Composable
fun MachineFilterSection(
    selectedType: MachineType?,
    availableTypes: List<MachineType>,
    onTypeSelected: (MachineType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("All", style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        items(availableTypes) { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() } + "s", style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
