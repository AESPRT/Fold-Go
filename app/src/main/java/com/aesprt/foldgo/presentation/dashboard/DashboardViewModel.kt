package com.aesprt.foldgo.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.repository.OrderRepository
import com.aesprt.foldgo.domain.usecase.GetActiveOrdersUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val orders: List<Order> = emptyList(),
    val totalIntakeAmount: Double = 0.0,
    val totalSalesAmount: Double = 0.0,
    val activeOrdersCount: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getAllOrders()
        .map { orders ->
            val activeOrders = orders.filter { it.status != OrderStatus.DELIVERED }
            DashboardUiState(
                orders = activeOrders,
                totalIntakeAmount = orders.sumOf { it.totalAmount },
                totalSalesAmount = orders.sumOf { it.paidAmount },
                activeOrdersCount = activeOrders.size,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
