package com.aesprt.foldgo.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.presentation.components.FoldGoEmptyState
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.OrderCard
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    onOrderClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryContent(
        uiState = uiState,
        onOrderClick = onOrderClick,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onOrderClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        FoldGoLogo(
                            iconSize = 32.dp,
                            title = "Order History",
                            supportingText = "View and manage your order history",
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )
            },
            bottomBar = {
                // Reserve space for the floating bottom bar
                Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    FoldGoLoading()
                }
            } else if (uiState.orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    FoldGoEmptyState(
                        message = "No history found",
                        description = "Orders will appear here once they are marked as delivered.",
                        icon = Icons.Rounded.History
                    )
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
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(uiState.orders) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onOrderClick(order.orderId) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryContentPreview() {
    FoldGoTheme {
        HistoryContent(
            uiState = HistoryUiState(
                orders = listOf(
                    Order(
                        orderId = "1",
                        shopId = "shop1",
                        customerId = "cust1",
                        customerName = "Juan Dela Cruz",
                        customerPhone = "09123456789",
                        orderNumber = "FG-1001",
                        items = emptyList(),
                        totalAmount = 150.0,
                        paidAmount = 150.0,
                        status = OrderStatus.DELIVERED,
                        intakePhotos = emptyList(),
                        machineId = null,
                        staffId = "staff1",
                        staffName = "Operator 1",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            ),
            onOrderClick = {}
        )
    }
}
