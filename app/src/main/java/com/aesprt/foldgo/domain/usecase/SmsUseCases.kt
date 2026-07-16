package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.SmsSubscription
import com.aesprt.foldgo.domain.model.SmsTransactionLog
import com.aesprt.foldgo.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow

class GetSubscriptionUseCase(private val repository: SmsRepository) {
    operator fun invoke(): Flow<SmsSubscription?> = repository.getSubscription()
}

class UpdateSubscriptionUseCase(private val repository: SmsRepository) {
    suspend operator fun invoke(subscription: SmsSubscription) = repository.updateSubscription(subscription)
}

class GetSmsLogsUseCase(private val repository: SmsRepository) {
    operator fun invoke(): Flow<List<SmsTransactionLog>> = repository.getLogs()
}
