package com.aesprt.foldgo.domain.repository

interface SmsRepository {
    suspend fun sendSms(fromNumber: String, toNumber: String, message: String): Result<Unit>
}
