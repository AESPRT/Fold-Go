package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow

class GetShopByIdUseCase(private val repository: ShopRepository) {
    operator fun invoke(shopId: String): Flow<Shop?> = repository.getShop(shopId)
}

class UpsertShopUseCase(private val repository: ShopRepository) {
    suspend operator fun invoke(shop: Shop) = repository.upsertShop(shop)
}

class HasShopUseCase(private val repository: ShopRepository) {
    suspend operator fun invoke(): Boolean = repository.hasShop()
}
