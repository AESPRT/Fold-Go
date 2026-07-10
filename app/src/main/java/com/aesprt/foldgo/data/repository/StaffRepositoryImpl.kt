package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.StaffDao
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.domain.model.Staff
import com.aesprt.foldgo.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StaffRepositoryImpl(
    private val staffDao: StaffDao
) : StaffRepository {
    override fun getStaffByShop(shopId: String): Flow<List<Staff>> {
        return staffDao.getStaffByShop(shopId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStaffById(staffId: String): Staff? {
        return staffDao.getStaffById(staffId)?.toDomain()
    }

    override suspend fun upsertStaff(staff: Staff) {
        staffDao.upsertStaff(staff.toEntity())
    }

    override suspend fun deleteStaff(staffId: String) {
        staffDao.deleteStaff(staffId)
    }
}
