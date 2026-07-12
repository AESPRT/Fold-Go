package com.aesprt.foldgo.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.OrderCard
import com.aesprt.foldgo.presentation.components.SummaryCard
import androidx.compose.ui.tooling.preview.Preview
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    onOrderClick: (String) -> Unit,
    onNewOrderClick: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ModernBackground {
        DashboardContent(
            uiState = uiState,
            onOrderClick = onOrderClick,
            onNewOrderClick = onNewOrderClick,
            onAutoFinish = viewModel::autoFinishCycle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onOrderClick: (String) -> Unit,
    onNewOrderClick: () -> Unit,
    onAutoFinish: (String, String) -> Unit
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Grid (Always Visible)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryCard(
                            title = "Total Intake",
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
                            onClick = { onOrderClick(orderWithMachine.order.orderId) },
                            onTimerFinished = { orderWithMachine.machine?.let { onAutoFinish(it.machineId, orderWithMachine.order.orderId) } }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardContentLoadingPreview() {
    FoldGoTheme {
        DashboardContent(
            uiState = DashboardUiState(isLoading = true),
            onOrderClick = {},
            onNewOrderClick = {},
            onAutoFinish = {_, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardContentEmptyPreview() {
    FoldGoTheme {
        DashboardContent(
            uiState = DashboardUiState(orders = emptyList()),
            onOrderClick = {},
            onNewOrderClick = {},
            onAutoFinish = {_, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardContentPreview() {
    FoldGoTheme {
        DashboardContent(
            uiState = DashboardUiState(
                orders = listOf(
                    OrderWithMachine(
                        Order(
                            orderId = "1",
                            shopId = "shop1",
                            customerId = "cust1",
                            customerName = "John Doe",
                            customerPhone = "1234567890",
                            orderNumber = "FG-1024",
                            items = emptyList(),
                            totalAmount = 25.0,
                            paidAmount = 0.0,
                            status = OrderStatus.INTAKE,
                            intakePhotos = emptyList(),
                            machineId = null,
                            staffId = "staff1",
                            staffName = "Operator 1",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        ),
                        null
                    ),
                    OrderWithMachine(
                        Order(
                            orderId = "2",
                            shopId = "shop1",
                            customerId = "cust2",
                            customerName = "Jane Doe",
                            customerPhone = "9876543210",
                            orderNumber = "FG-1025",
                            items = emptyList(),
                            totalAmount = 45.0,
                            paidAmount = 45.0,
                            status = OrderStatus.READY,
                            intakePhotos = emptyList(),
                            machineId = "M1",
                            staffId = "staff1",
                            staffName = "Operator 1",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        ),
                        null
                    )
                )
            ),
            onOrderClick = {},
            onNewOrderClick = {},
            onAutoFinish = {_, _ -> }
        )
    }
}
