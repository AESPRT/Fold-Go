package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.InventoryDao
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.domain.model.Inventory
import com.aesprt.foldgo.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(
    private val inventoryDao: InventoryDao
) : InventoryRepository {
    override fun getAllInventory(): Flow<List<Inventory>> {
        return inventoryDao.getAllInventory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertInventory(item: Inventory) {
        inventoryDao.upsertInventory(item.toEntity())
    }

    override suspend fun updateStock(itemId: String, newStock: Double) {
        inventoryDao.updateStock(itemId, newStock)
    }
}
