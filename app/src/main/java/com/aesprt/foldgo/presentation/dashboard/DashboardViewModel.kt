package com.aesprt.foldgo.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.usecase.GetActiveOrdersUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    getActiveOrdersUseCase: GetActiveOrdersUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getActiveOrdersUseCase()
        .map { orders ->
            DashboardUiState(orders = orders, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
