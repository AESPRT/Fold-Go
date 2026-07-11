package com.aesprt.foldgo.domain.repository

interface SmsRepository {
    suspend fun sendSms(number: String, message: String): Result<Unit>
}
