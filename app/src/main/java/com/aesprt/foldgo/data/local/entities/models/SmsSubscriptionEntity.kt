package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_subscriptions")
data class SmsSubscriptionEntity(
    @PrimaryKey val shopId: String,
    val planName: String,
    val allocatedSms: Int,
    val usedSms: Int,
    val billingCycleStart: Long,
    val billingCycleEnd: Long,
    val isActive: Boolean
)
