package com.aesprt.foldgo.presentation.order.components.tablet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.DevicePreviews
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.order.components.*
import com.aesprt.foldgo.presentation.order.components.PendingMachineAssignmentCard
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@Composable
fun OrderDetailTabletContent(
    order: Order,
    machine: Machine?,
    availableAddOns: List<AddOn>,
    availableMachines: List<Machine>,
    onReady: (DeliveryMethod, Double) -> Unit,
    onAssignMachine: (String) -> Unit,
    onDelivered: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val isServiceProcessingComplete = order.status == OrderStatus.DELIVERED
        val isPending = order.status == OrderStatus.PENDING
        val isReady = order.status == OrderStatus.READY
        // Left Column: Main Info & Cycles
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OrderHeader(order)

            if (isPending) {
                PendingMachineAssignmentCard(
                    availableMachines = availableMachines,
                    onAssignMachine = onAssignMachine
                )
            }

            if (!isServiceProcessingComplete && !isPending) {
                ActiveCycleCard(
                    order = order,
                    machine = machine
                )
            }

            ItemsListCard(order)
        }

        // Right Column: Details & Actions
        Column(
            modifier = Modifier.weight(0.4f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OrderInfoCard(order, machine, availableAddOns)

            if (isReady) {
                StatusActionCard(
                    status = order.status,
                    order = order,
                    onReady = onReady,
                    onDelivered = onDelivered
                )
            }
        }
    }
}

@DevicePreviews
@Composable
fun OrderDetailContentTabletPreview() {
    FoldGoTheme {
        OrderDetailTabletContent(
            order = Order(
                orderId = "1",
                shopId = "shop1",
                customerId = "cust1",
                customerName = "Juan Dela Cruz",
                customerPhone = "09123456789",
                orderNumber = "FG-1001",
                items = listOf(
                    ServiceItem(
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
