package com.aesprt.foldgo.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.usecase.GetAllOrdersUseCase
import kotlinx.coroutines.flow.*

data class HistoryUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val getAllOrdersUseCase: GetAllOrdersUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getAllOrdersUseCase()
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
