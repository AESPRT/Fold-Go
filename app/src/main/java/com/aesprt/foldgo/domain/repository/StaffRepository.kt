package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Staff
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    fun getStaffByShop(shopId: String): Flow<List<Staff>>
    suspend fun getStaffById(staffId: String): Staff?
    suspend fun upsertStaff(staff: Staff)
    suspend fun deleteStaff(staffId: String)
}
