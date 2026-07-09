package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Inventory
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllInventory(): Flow<List<Inventory>>
    suspend fun upsertInventory(item: Inventory)
    suspend fun updateStock(itemId: String, newStock: Double)
}
