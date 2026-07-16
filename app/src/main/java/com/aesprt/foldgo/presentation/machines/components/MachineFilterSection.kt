package com.aesprt.foldgo.presentation.machines.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachineFilterSection(
    selectedStatus: MachineStatus?,
    onStatusSelected: (MachineStatus?) -> Unit,
    modifier: Modifier = Modifier,
    isWrap: Boolean = false
) {
    if (isWrap) {
        FlowRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatusFilterChip(
                selected = selectedStatus == null,
                label = "All",
                onClick = { onStatusSelected(null) }
            )
            MachineStatus.entries.forEach { status ->
                StatusFilterChip(
                    selected = selectedStatus == status,
                    label = status.name.lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    onClick = { onStatusSelected(status) }
                )
            }
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                StatusFilterChip(
                    selected = selectedStatus == null,
                    label = "All",
                    onClick = { onStatusSelected(null) }
                )
            }
            items(MachineStatus.entries.toList()) { status ->
                StatusFilterChip(
                    selected = selectedStatus == status,
                    label = status.name.lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    onClick = { onStatusSelected(status) }
                )
            }
        }
    }
}

@Composable
private fun StatusFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = chipColors
    )
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
