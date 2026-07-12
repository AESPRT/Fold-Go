package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.ShopDao
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.data.local.entities.*
import com.aesprt.foldgo.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShopRepositoryImpl(
    private val shopDao: ShopDao
) : ShopRepository {
    override fun getShop(shopId: String): Flow<Shop?> {
        return shopDao.getShopById(shopId).map { it?.toDomain() }
    }

    override fun getFirstShop(): Flow<Shop?> {
        return shopDao.getFirstShop().map { it?.toDomain() }
    }

    override suspend fun upsertShop(shop: Shop) {
        shopDao.upsertShop(shop.toEntity())
    }

    override suspend fun hasShop(): Boolean {
        return shopDao.getShopCount() > 0
    }
}
