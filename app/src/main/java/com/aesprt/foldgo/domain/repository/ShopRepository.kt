package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Shop
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    fun getShop(shopId: String): Flow<Shop?>
    fun getFirstShop(): Flow<Shop?>
    suspend fun upsertShop(shop: Shop)
    suspend fun hasShop(): Boolean
}
