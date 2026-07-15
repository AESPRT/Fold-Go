package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import java.util.Locale

@Composable
fun MachineFilterSection(
    selectedStatus: MachineStatus?,
    onStatusSelected: (MachineStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text("All", style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(12.dp)
            )
        }
        items(MachineStatus.entries) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { 
                    Text(
                        status.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, 
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MachineFilterSectionPreview() {
    FoldGoTheme {
        MachineFilterSection(
            selectedStatus = null,
            onStatusSelected = {}
        )
    }
}
