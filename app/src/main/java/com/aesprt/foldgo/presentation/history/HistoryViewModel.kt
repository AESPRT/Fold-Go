package com.aesprt.foldgo.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.repository.OrderRepository
import kotlinx.coroutines.flow.*

data class HistoryUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.getAllOrders()
        .map { orders ->
            HistoryUiState(
                orders = orders.filter { it.status == OrderStatus.DELIVERED }
                    .sortedByDescending { it.updatedAt },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )
}
