package com.aesprt.foldgo.domain.model

data class SmsTransactionLog(
    val logId: Long = 0,
    val jobOrderId: String,
    val recipientNumber: String,
    val messageBody: String,
    val segmentsCharged: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val apiResponseId: String? = null
)
