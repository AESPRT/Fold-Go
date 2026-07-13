package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.BuildConfig
import com.aesprt.foldgo.data.remote.HttpSmsRequest
import com.aesprt.foldgo.data.remote.HttpSmsService
import com.aesprt.foldgo.domain.repository.SmsRepository

class HttpSmsRepositoryImpl(
    private val httpSmsService: HttpSmsService
) : SmsRepository {
    
    private val apiKey = BuildConfig.HTTP_SMS_API_KEY

    override suspend fun sendSms(fromNumber: String, toNumber: String, message: String): Result<Unit> {
        return try {
            val formattedFrom = if (fromNumber.startsWith("+")) fromNumber else "+$fromNumber"
            val formattedTo = if (toNumber.startsWith("+")) toNumber else "+$toNumber"
            
            val request = HttpSmsRequest(
                content = message,
                from = formattedFrom,
                to = formattedTo
            )
            
            val response = httpSmsService.sendSms(apiKey, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HttpSMS Error: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
