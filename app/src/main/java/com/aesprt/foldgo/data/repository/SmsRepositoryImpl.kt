package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.remote.SmsService
import com.aesprt.foldgo.domain.repository.SmsRepository

class SmsRepositoryImpl(
    private val smsService: SmsService
) : SmsRepository {
    
    // TODO: Move this to Shop Settings or secure storage
    private val apiKey = "504f64d26e9cc6f6142730dd83808f54"

    override suspend fun sendSms(number: String, message: String): Result<Unit> {
        return try {
            val response = smsService.sendSms(apiKey, number, message, "CommuTech")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send SMS: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
