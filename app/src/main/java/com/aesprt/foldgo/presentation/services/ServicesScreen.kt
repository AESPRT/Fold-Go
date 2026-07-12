package com.aesprt.foldgo.presentation.services

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.Service
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.order.components.ServiceAddDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServicesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Laundry Services", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Add Service", fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    FoldGoLoading()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Available Services",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                    }

                    items(uiState.services) { service ->
                        ServiceItem(
                            service = service,
                            onDelete = { viewModel.deleteService(service) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ServiceAddDialog(
            title = "Add New Service",
            confirmText = "Add Service",
            showSaveAsPreset = false,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, unit, price, type, _ ->
                viewModel.addService(
                    Service(
                        serviceId = IdGeneratorUtils.generateUniqueId("svc"),
                        shopId = "", // Filled by ViewModel
                        name = name,
                        defaultQuantity = qty,
                        unit = unit,
                        pricePerUnit = price,
                        type = type
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ServiceItem(service: Service, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Service Type Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = when (service.type) {
                    ServiceType.WASH -> Color(0xFF2196F3).copy(alpha = 0.1f)
                    ServiceType.DRY -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    ServiceType.WASH_DRY -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                    ServiceType.IRON -> Color(0xFFE91E63).copy(alpha = 0.1f)
                    ServiceType.OTHER -> Color(0xFF607D8B).copy(alpha = 0.1f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (service.type) {
                            ServiceType.WASH -> Icons.Rounded.LocalLaundryService
                            ServiceType.DRY -> Icons.Rounded.Air
                            ServiceType.WASH_DRY -> Icons.Rounded.AllInclusive
                            ServiceType.IRON -> Icons.Rounded.Iron
                            ServiceType.OTHER -> Icons.Rounded.Category
                        },
                        contentDescription = null,
                        tint = when (service.type) {
                            ServiceType.WASH -> Color(0xFF2196F3)
                            ServiceType.DRY -> Color(0xFFFF9800)
                            ServiceType.WASH_DRY -> Color(0xFF9C27B0)
                            ServiceType.IRON -> Color(0xFFE91E63)
                            ServiceType.OTHER -> Color(0xFF607D8B)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = PriceFormatter.format(service.pricePerUnit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ ${service.unit.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
