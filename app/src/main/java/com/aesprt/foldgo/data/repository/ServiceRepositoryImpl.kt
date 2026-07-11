package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.ServiceDao
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.domain.model.Service
import com.aesprt.foldgo.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepositoryImpl(
    private val serviceDao: ServiceDao
) : ServiceRepository {
    override fun getServicesByShop(shopId: String): Flow<List<Service>> {
        return serviceDao.getServicesByShop(shopId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertService(service: Service) {
        serviceDao.upsertService(service.toEntity())
    }

    override suspend fun deleteService(service: Service) {
        serviceDao.deleteService(service.toEntity())
    }
}
