package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetActiveOrdersUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<List<Order>> {
        return repository.getAllOrders().map { orders ->
            orders.filter { it.status != OrderStatus.DELIVERED }
                .sortedByDescending { it.updatedAt }
        }
    }
}
