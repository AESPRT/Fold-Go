package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.repository.SmsRepository

class SendSmsUseCase(private val repository: SmsRepository) {
    suspend operator fun invoke(number: String, message: String): Result<Unit> {
        return repository.sendSms(number, message)
    }
}
