package com.aesprt.foldgo.data.repository

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.aesprt.foldgo.core.work.MachineCompletionWorker
import com.aesprt.foldgo.data.local.dao.MachineCategoryDao
import com.aesprt.foldgo.data.local.dao.MachineDao
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineCategory
import com.aesprt.foldgo.domain.repository.MachineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class MachineRepositoryImpl(
    private val machineDao: MachineDao,
    private val categoryDao: MachineCategoryDao,
    private val workManager: WorkManager
) : MachineRepository {
    override fun getAllMachines(): Flow<List<Machine>> {
        return machineDao.getAllMachines().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertMachine(machine: Machine) {
        machineDao.upsertMachine(machine.toEntity())
    }

    override suspend fun updateMachineStatus(machineId: String, status: String) {
        machineDao.updateMachineStatus(machineId, status)
    }

    override suspend fun startMachineCycle(machineId: String, durationMinutes: Int) {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        machineDao.startCycle(machineId, endTime)

        val workRequest = OneTimeWorkRequestBuilder<MachineCompletionWorker>()
            .setInitialDelay(durationMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(workDataOf("machineId" to machineId))
            .addTag("machine_$machineId")
            .build()
        
        workManager.enqueue(workRequest)
    }

    override suspend fun finishMachineCycle(machineId: String) {
        machineDao.finishCycle(machineId)
        workManager.cancelAllWorkByTag("machine_$machineId")
    }

    override fun getAllCategories(): Flow<List<MachineCategory>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertCategory(category: MachineCategory) {
        categoryDao.upsertCategory(category.toEntity())
    }
}
