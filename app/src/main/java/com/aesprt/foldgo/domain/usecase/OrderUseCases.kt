package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.Service
import com.aesprt.foldgo.domain.repository.OrderRepository
import com.aesprt.foldgo.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow

class GetAllOrdersUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<List<Order>> = repository.getAllOrders()
}

class GetOrderByIdUseCase(private val repository: OrderRepository) {
    operator fun invoke(orderId: String): Flow<Order?> = repository.getOrderById(orderId)
}

class UpsertOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(order: Order) = repository.upsertOrder(order)
}

class DeleteOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(order: Order) = repository.deleteOrder(order)
}

class GetServicesUseCase(private val repository: ServiceRepository) {
    operator fun invoke(shopId: String): Flow<List<Service>> = repository.getServicesByShop(shopId)
}

class UpsertServiceUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(service: Service) = repository.upsertService(service)
}
