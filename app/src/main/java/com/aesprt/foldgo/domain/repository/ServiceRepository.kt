package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Service
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun getServicesByShop(shopId: String): Flow<List<Service>>
    suspend fun upsertService(service: Service)
    suspend fun deleteService(service: Service)
}
