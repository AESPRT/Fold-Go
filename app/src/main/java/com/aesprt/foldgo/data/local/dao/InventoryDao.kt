package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Upsert
    suspend fun upsertInventory(item: InventoryEntity)

    @Query("UPDATE inventory SET currentStock = :newStock WHERE itemId = :itemId")
    suspend fun updateStock(itemId: String, newStock: Double)
}
