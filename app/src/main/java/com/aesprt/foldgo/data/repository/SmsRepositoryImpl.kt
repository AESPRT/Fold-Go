package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.BuildConfig
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.data.local.dao.SmsDao
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.data.local.entities.models.SmsTransactionLogEntity
import com.aesprt.foldgo.data.remote.SmsService
import com.aesprt.foldgo.domain.model.SmsSubscription
import com.aesprt.foldgo.domain.model.SmsTransactionLog
import com.aesprt.foldgo.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.ceil

class SmsRepositoryImpl(
    private val smsService: SmsService,
    private val smsDao: SmsDao,
    private val preferenceManager: PreferenceManager
) : SmsRepository {
    
    private val apiKey = BuildConfig.SEMAPHORE_API_KEY

    override suspend fun sendSms(toNumber: String, message: String, jobOrderId: String): Result<Unit> {
        return try {
            val shopId = preferenceManager.currentShopId.first() ?: return Result.failure(Exception("Shop not found"))
            val subscription = smsDao.getSubscription(shopId).first() 
                ?: return Result.failure(Exception("No active SMS subscription found"))

            if (!subscription.isActive || subscription.usedSms >= subscription.allocatedSms) {
                return Result.failure(Exception("Insufficient SMS balance or inactive subscription"))
            }

            // Calculate segments (160 chars per segment)
            val segments = ceil(message.length.toDouble() / 160.0).toInt().coerceAtLeast(1)
            
            if (subscription.usedSms + segments > subscription.allocatedSms) {
                return Result.failure(Exception("Message exceeds remaining SMS balance"))
            }

            val response = smsService.sendSms(apiKey, toNumber, message, "FoldGo")
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val apiResponse = response.body()!![0]
                
                // Update balance
                smsDao.updateUsedSmsBalance(shopId, subscription.usedSms + segments)
                
                // Log transaction
                smsDao.insertLog(
                    SmsTransactionLogEntity(
                        jobOrderId = jobOrderId,
                        recipientNumber = toNumber,
                        messageBody = message,
                        segmentsCharged = segments,
                        apiResponseId = apiResponse.message_id?.toString()
                    )
                )
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Semaphore Error: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSubscription(): Flow<SmsSubscription?> {
        return preferenceManager.currentShopId.map { shopId ->
            if (shopId != null) {
                smsDao.getSubscription(shopId).first()?.toDomain()
            } else null
        }
    }

    override suspend fun updateSubscription(subscription: SmsSubscription) {
        smsDao.upsertSubscription(subscription.toEntity())
        preferenceManager.updateSubscriptionSettings(
            planName = subscription.planName,
            credits = subscription.allocatedSms - subscription.usedSms,
            billingCycleEnd = subscription.billingCycleEnd
        )
    }

    override fun getLogs(): Flow<List<SmsTransactionLog>> {
        return smsDao.getAllLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
