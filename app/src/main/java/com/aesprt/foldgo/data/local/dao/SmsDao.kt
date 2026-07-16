package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.models.SmsSubscriptionEntity
import com.aesprt.foldgo.data.local.entities.models.SmsTransactionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_subscriptions WHERE shopId = :shopId")
    fun getSubscription(shopId: String): Flow<SmsSubscriptionEntity?>

    @Upsert
    suspend fun upsertSubscription(subscription: SmsSubscriptionEntity)

    @Query("UPDATE sms_subscriptions SET usedSms = :usedSms WHERE shopId = :shopId")
    suspend fun updateUsedSmsBalance(shopId: String, usedSms: Int)

    @Insert
    suspend fun insertLog(log: SmsTransactionLogEntity)

    @Query("SELECT * FROM sms_transaction_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SmsTransactionLogEntity>>
}
