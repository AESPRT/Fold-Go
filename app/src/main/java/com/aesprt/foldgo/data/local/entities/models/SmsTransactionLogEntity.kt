package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_transaction_logs")
data class SmsTransactionLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val jobOrderId: String,
    val recipientNumber: String,
    val messageBody: String,
    val segmentsCharged: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val apiResponseId: String? = null
)
