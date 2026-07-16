package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.CustomerDao
import com.aesprt.foldgo.data.local.entities.models.CustomerEntity
import com.aesprt.foldgo.domain.model.Customer
import com.aesprt.foldgo.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomerRepositoryImpl(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCustomerById(customerId: String): Customer? {
        return customerDao.getCustomerById(customerId)?.toDomain()
    }

    override suspend fun upsertCustomer(customer: Customer) {
        customerDao.upsertCustomer(customer.toEntity())
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer.toEntity())
    }

    private fun CustomerEntity.toDomain() = Customer(
        customerId = customerId,
        name = name,
        phone = phone,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Customer.toEntity() = CustomerEntity(
        customerId = customerId,
        name = name,
        phone = phone,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
