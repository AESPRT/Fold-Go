package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.Customer
import com.aesprt.foldgo.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow

class GetAllCustomersUseCase(private val repository: CustomerRepository) {
    operator fun invoke(): Flow<List<Customer>> = repository.getAllCustomers()
}

class SearchCustomersUseCase(private val repository: CustomerRepository) {
    operator fun invoke(query: String): Flow<List<Customer>> = repository.searchCustomers(query)
}

class GetCustomerByIdUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customerId: String): Customer? = repository.getCustomerById(customerId)
}

class UpsertCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customer: Customer) = repository.upsertCustomer(customer)
}

class DeleteCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customer: Customer) = repository.deleteCustomer(customer)
}
