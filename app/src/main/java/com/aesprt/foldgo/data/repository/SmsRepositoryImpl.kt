package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.BuildConfig
import com.aesprt.foldgo.data.remote.SmsService
import com.aesprt.foldgo.domain.repository.SmsRepository

class SmsRepositoryImpl(
    private val smsService: SmsService
) : SmsRepository {
    
    // TODO: Move this to Shop Settings or secure storage
    private val apiKey = BuildConfig.SEMAPHORE_API_KEY

    override suspend fun sendSms(fromNumber: String, toNumber: String, message: String): Result<Unit> {
        return try {
            val response = smsService.sendSms(apiKey, toNumber, message, "CommuTech")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Semaphore Error: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
