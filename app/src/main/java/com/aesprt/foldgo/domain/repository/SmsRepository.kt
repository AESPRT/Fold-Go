package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.SmsSubscription
import com.aesprt.foldgo.domain.model.SmsTransactionLog
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun sendSms(toNumber: String, message: String, jobOrderId: String): Result<Unit>
    fun getSubscription(): Flow<SmsSubscription?>
    suspend fun updateSubscription(subscription: SmsSubscription)
    fun getLogs(): Flow<List<SmsTransactionLog>>
}
