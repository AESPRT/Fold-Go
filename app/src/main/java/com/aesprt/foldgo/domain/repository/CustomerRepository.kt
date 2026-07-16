package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun getCustomerById(customerId: String): Customer?
    suspend fun upsertCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
}
