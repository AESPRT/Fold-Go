package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.models.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE shopId = :shopId")
    fun getServicesByShop(shopId: String): Flow<List<ServiceEntity>>

    @Upsert
    suspend fun upsertService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)
    
    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServiceCount(): Int
}
