package com.aesprt.foldgo.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SmsService {
    @FormUrlEncoded
    @POST("api/v4/messages")
    suspend fun sendSms(
        @Field("apikey") apiKey: String,
        @Field("number") number: String,
        @Field("message") message: String,
        @Field("sendername") senderName: String? = null
    ): Response<List<SmsResponse>>
}

@Serializable
data class SmsResponse(
    val message_id: Long? = null,
    val external_id: String? = null,
    val user: String? = null,
    val status: String? = null,
    val status_code: Int? = null,
    val recipient: String? = null,
    val message: String? = null,
    val sender_name: String? = null,
    val network: String? = null,
    val price: Double? = null,
    val date_sent: String? = null,
    val date_updated: String? = null
)
