package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.MachineCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineCategoryDao {
    @Query("SELECT * FROM machine_categories")
    fun getAllCategories(): Flow<List<MachineCategoryEntity>>

    @Upsert
    suspend fun upsertCategory(category: MachineCategoryEntity)

    @Query("SELECT COUNT(*) FROM machine_categories")
    suspend fun getCategoryCount(): Int
}
