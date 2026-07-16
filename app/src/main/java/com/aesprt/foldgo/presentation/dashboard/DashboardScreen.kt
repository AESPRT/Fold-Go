package com.aesprt.foldgo.presentation.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.DevicePreviews
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.presentation.components.*
import com.aesprt.foldgo.presentation.order.OrderDetailScreen
import com.aesprt.foldgo.presentation.order.OrderDetailViewModel
import com.aesprt.foldgo.ui.navigation.DashboardRoute
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DashboardScreen(
    onOrderClick: (String) -> Unit,
    onNewOrderClick: () -> Unit,
    widthSizeClass: WindowWidthSizeClass,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTablet = widthSizeClass == WindowWidthSizeClass.Expanded

    ModernBackground {
        if (isTablet) {
            DashboardTabletContent(
                uiState = uiState,
                onNewOrderClick = onNewOrderClick
            )
        } else {
            DashboardContent(
                uiState = uiState,
                onOrderClick = onOrderClick,
                onNewOrderClick = onNewOrderClick,
                contentPadding = contentPadding
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onOrderClick: (String) -> Unit,
    onNewOrderClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    FoldGoLogo(iconSize = 32.dp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewOrderClick) {
                Icon(Icons.Default.Add, contentDescription = "New Order")
            }
        },
        bottomBar = {
            // Reserve space for the floating bottom bar so FAB is pushed up correctly
            Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                FoldGoLoading()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Grid (Always Visible)
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryCard(
                            title = "Total Queued",
                            value = PriceFormatter.format(uiState.totalIntakeAmount),
                            icon = Icons.Rounded.Payments,
                            iconColor = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Active Orders",
                            value = uiState.activeOrdersCount.toString(),
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            iconColor = Color(0xFF03A9F4),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    SummaryCard(
                        title = "Total Sales (Revenue)",
                        value = PriceFormatter.format(uiState.totalSalesAmount),
                        icon = Icons.Rounded.Payments,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "Recent Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (uiState.orders.isEmpty()) {
                    item {
                        FoldGoEmptyState(
                            message = "No active orders",
                            description = "Tap the + button to create your first laundry order and start tracking progress."
                        )
                    }
                } else {
                    items(uiState.orders) { orderWithMachine ->
                        OrderCard(
                            order = orderWithMachine.order,
                            machine = orderWithMachine.machine,
                            onClick = { onOrderClick(orderWithMachine.order.orderId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTabletContent(
    uiState: DashboardUiState,
    onNewOrderClick: () -> Unit
) {
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    
    // Auto-select first order if none selected
    LaunchedEffect(uiState.orders) {
        if (selectedOrderId == null && uiState.orders.isNotEmpty()) {
            selectedOrderId = uiState.orders.first().order.orderId
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Dashboard,
                                contentDescription = null,
                                modifier = Modifier.padding(6.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Fold&Go — Staff Console",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Tablet dashboard - Freshly Managed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onNewOrderClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Order")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Total Queued",
                    value = PriceFormatter.format(uiState.totalIntakeAmount),
                    icon = Icons.Rounded.Payments,
                    iconColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Active Orders",
                    value = uiState.activeOrdersCount.toString(),
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    iconColor = Color(0xFF03A9F4),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Sales (Revenue)",
                    value = PriceFormatter.format(uiState.totalSalesAmount),
                    icon = Icons.Rounded.Payments,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                // Assuming idle machines could be added later, using a placeholder for now to match the image
                SummaryCard(
                    title = "Machines Idle",
                    value = "4", // Placeholder
                    icon = Icons.Rounded.LocalLaundryService,
                    iconColor = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Recent Orders
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "Recent Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            FoldGoLoading()
                        }
                    } else if (uiState.orders.isEmpty()) {
                        FoldGoEmptyState(
                            message = "No active orders",
                            description = "Tap the New Order button to start."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.orders) { orderWithMachine ->
                                val isSelected = selectedOrderId == orderWithMachine.order.orderId
                                OrderCard(
                                    order = orderWithMachine.order,
                                    machine = orderWithMachine.machine,
                                    onClick = { selectedOrderId = orderWithMachine.order.orderId },
                                    modifier = if (isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                                    } else Modifier
                                )
                            }
                        }
                    }
                }

                // Right Column: Order Details
                Surface(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    if (selectedOrderId != null) {
                        val detailViewModel: OrderDetailViewModel = koinViewModel(key = selectedOrderId) { parametersOf(selectedOrderId) }
                        val detailUiState by detailViewModel.uiState.collectAsState()

                        if (detailUiState.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                FoldGoLoading()
                            }
                        } else if (detailUiState.order != null) {
                            OrderDetailScreen(orderId = detailUiState.order?.orderId ?: "", onNavigateBack = {})
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select an order to view details", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Composable
fun DashboardMobilePreview() {
    FoldGoTheme {
        Scaffold(
            bottomBar = {
                FoldGoBottomBar(
                    currentRoute = DashboardRoute::class.qualifiedName,
                    onNavigate = {}
                )
            }
        ) { padding ->
            DashboardContent(
                uiState = DashboardUiState(
                    orders = emptyList(),
                    totalIntakeAmount = 3385.0,
                    activeOrdersCount = 3,
                    totalSalesAmount = 3385.0
                ),
                onOrderClick = {},
                onNewOrderClick = {},
                contentPadding = padding
            )
        }
    }
}

@DevicePreviews
@Composable
fun DashboardTabletPreview() {
    FoldGoTheme {
        TabletScaffold(
            currentRoute = DashboardRoute::class.qualifiedName,
            onNavigate = {}
        ) { _ ->
            DashboardTabletContent(
                uiState = DashboardUiState(
                    orders = emptyList(),
                    totalIntakeAmount = 5115.0,
                    activeOrdersCount = 2,
                    totalSalesAmount = 5215.0
                ),
                onNewOrderClick = {}
            )
        }
    }
}
