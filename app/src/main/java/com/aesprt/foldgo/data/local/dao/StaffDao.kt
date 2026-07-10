package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.StaffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE shopId = :shopId")
    fun getStaffByShop(shopId: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE staffId = :staffId")
    suspend fun getStaffById(staffId: String): StaffEntity?

    @Upsert
    suspend fun upsertStaff(staff: StaffEntity)

    @Query("DELETE FROM staff WHERE staffId = :staffId")
    suspend fun deleteStaff(staffId: String)
}
