package com.aesprt.foldgo.domain.model

data class SmsSubscription(
    val shopId: String,
    val planName: String,
    val allocatedSms: Int,
    val usedSms: Int,
    val billingCycleStart: Long,
    val billingCycleEnd: Long,
    val isActive: Boolean
) {
    val remainingSms: Int
        get() = (allocatedSms - usedSms).coerceAtLeast(0)
}
