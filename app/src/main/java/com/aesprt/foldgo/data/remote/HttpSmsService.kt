package com.aesprt.foldgo.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HttpSmsService {
    @POST("messages/send")
    suspend fun sendSms(
        @Header("x-api-key") apiKey: String,
        @Body request: HttpSmsRequest
    ): Response<HttpSmsResponse>
}

@Serializable
data class HttpSmsRequest(
    val content: String,
    val from: String,
    val to: String
)

@Serializable
data class HttpSmsResponse(
    val id: String? = null,
    val content: String? = null,
    val from: String? = null,
    val to: String? = null,
    val status: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
