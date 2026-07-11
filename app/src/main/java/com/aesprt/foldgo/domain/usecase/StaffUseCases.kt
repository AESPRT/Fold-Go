package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.Staff
import com.aesprt.foldgo.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow

class GetStaffByShopUseCase(private val repository: StaffRepository) {
    operator fun invoke(shopId: String): Flow<List<Staff>> = repository.getStaffByShop(shopId)
}

class GetStaffByIdUseCase(private val repository: StaffRepository) {
    suspend operator fun invoke(staffId: String): Staff? = repository.getStaffById(staffId)
}

class UpsertStaffUseCase(private val repository: StaffRepository) {
    suspend operator fun invoke(staff: Staff) = repository.upsertStaff(staff)
}

class DeleteStaffUseCase(private val repository: StaffRepository) {
    suspend operator fun invoke(staffId: String) = repository.deleteStaff(staffId)
}
