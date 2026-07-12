package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.models.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE shopId = :shopId")
    fun getShopById(shopId: String): Flow<ShopEntity?>

    @Query("SELECT * FROM shops LIMIT 1")
    fun getFirstShop(): Flow<ShopEntity?>

    @Upsert
    suspend fun upsertShop(shop: ShopEntity)

    @Query("SELECT COUNT(*) FROM shops")
    suspend fun getShopCount(): Int
}
