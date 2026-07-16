package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.data.local.entities.models.SmsSubscriptionEntity
import com.aesprt.foldgo.data.local.entities.models.SmsTransactionLogEntity
import com.aesprt.foldgo.domain.model.SmsSubscription
import com.aesprt.foldgo.domain.model.SmsTransactionLog

fun SmsSubscriptionEntity.toDomain() = SmsSubscription(
    shopId = shopId,
    planName = planName,
    allocatedSms = allocatedSms,
    usedSms = usedSms,
    billingCycleStart = billingCycleStart,
    billingCycleEnd = billingCycleEnd,
    isActive = isActive
)

fun SmsSubscription.toEntity() = SmsSubscriptionEntity(
    shopId = shopId,
    planName = planName,
    allocatedSms = allocatedSms,
    usedSms = usedSms,
    billingCycleStart = billingCycleStart,
    billingCycleEnd = billingCycleEnd,
    isActive = isActive
)

fun SmsTransactionLogEntity.toDomain() = SmsTransactionLog(
    logId = logId,
    jobOrderId = jobOrderId,
    recipientNumber = recipientNumber,
    messageBody = messageBody,
    segmentsCharged = segmentsCharged,
    timestamp = timestamp,
    apiResponseId = apiResponseId
)

fun SmsTransactionLog.toEntity() = SmsTransactionLogEntity(
    logId = logId,
    jobOrderId = jobOrderId,
    recipientNumber = recipientNumber,
    messageBody = messageBody,
    segmentsCharged = segmentsCharged,
    timestamp = timestamp,
    apiResponseId = apiResponseId
)
