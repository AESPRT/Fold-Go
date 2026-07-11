package com.aesprt.foldgo.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aesprt.foldgo.core.notification.NotificationHelper
import com.aesprt.foldgo.data.local.FoldGoDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MachineCompletionWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val database: FoldGoDatabase by inject()
    private val notificationHelper: NotificationHelper by inject()

    override suspend fun doWork(): Result {
        val machineId = inputData.getString("machineId") ?: return Result.failure()
        
        val machine = database.machineDao.getMachineById(machineId)
        if (machine != null) {
            notificationHelper.showMachineCompletionNotification(machine.name)
            return Result.success()
        }
        
        return Result.failure()
    }
}
