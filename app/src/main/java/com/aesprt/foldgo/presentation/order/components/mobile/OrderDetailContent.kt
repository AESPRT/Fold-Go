package com.aesprt.foldgo.presentation.order.components.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.order.components.ActiveCycleCard
import com.aesprt.foldgo.presentation.order.components.ItemsListCard
import com.aesprt.foldgo.presentation.order.components.OrderHeader
import com.aesprt.foldgo.presentation.order.components.OrderInfoCard
import com.aesprt.foldgo.presentation.order.components.PendingMachineAssignmentCard
import com.aesprt.foldgo.presentation.order.components.StatusActionCard
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@Composable
fun OrderDetailContent(
    order: Order,
    machine: Machine?,
    availableAddOns: List<AddOn>,
    availableMachines: List<Machine>,
    onReady: (DeliveryMethod, Double) -> Unit,
    onAssignMachine: (String) -> Unit,
    onDelivered: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            OrderHeader(order)
        }

        if (order.status == OrderStatus.PENDING) {
            item {
                PendingMachineAssignmentCard(
                    availableMachines = availableMachines,
                    onAssignMachine = onAssignMachine
                )
            }
        }

        val isServiceProcessingComplete = order.status == OrderStatus.DELIVERED
        val isReady = order.status == OrderStatus.READY
        val isPending = order.status == OrderStatus.PENDING

        if (!isServiceProcessingComplete && !isPending) {
            item {
                ActiveCycleCard(
                    order = order,
                    machine = machine
                )
            }
        }

        item {
            OrderInfoCard(order, machine, availableAddOns)
        }

        if (isReady) {
            item {
                StatusActionCard(
                    status = order.status,
                    order = order,
                    onReady = onReady,
                    onDelivered = onDelivered
                )
            }
        }

        item {
            ItemsListCard(order)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderDetailContentPreview() {
    FoldGoTheme {
        OrderDetailContent(
            order = Order(
                orderId = "1",
                shopId = "shop1",
                customerId = "cust1",
                customerName = "Juan Dela Cruz",
                customerPhone = "09123456789",
                orderNumber = "FG-1001",
                items = listOf(
                    com.aesprt.foldgo.domain.model.ServiceItem(
                        "Wash & Dry",
                        5.0,
                        "KG",
                        65.0,
                        325.0,
                        ServiceType.PER_KG
                    )
                ),
                totalAmount = 325.0,
                paidAmount = 0.0,
                status = OrderStatus.QUEUED,
                intakePhotos = emptyList(),
                machineId = "M1",
                staffId = "staff1",
                staffName = "Operator 1",
                selectedAddOns = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            machine = Machine(
                machineId = "M1",
                shopId = "shop1",
                name = "Washer 01",
                capacityKg = 8.0,
                status = MachineStatus.IDLE,
                lastMaintenanceDate = 0L,
                endTime = System.currentTimeMillis() + 600000,
                cyclesCount = 0,
                assignedOrderId = null
            ),
            availableAddOns = emptyList(),
            availableMachines = emptyList(),
            onReady = { _, _ -> },
            onAssignMachine = {},
            onDelivered = {}
        )
    }
}
