# SMS Subscription Implementation Blueprint
## Laundry POS Integration Guide (Semaphore Gateway)

This document outlines the architectural blueprint, data models, pricing mechanics, and implementation details for integrating a profitable SMS subscription notification system into the Laundry Point-of-Sale (POS) application using the **Semaphore API** gateway.

---

## 1. Business Strategy & Subscription Packages

A typical laundry shop transaction triggers exactly **two baseline SMS messages**:
1. **Drop-off Confirmation:** Acknowledges receipt, logs the Job Order (JO#), and specifies the total amount.
2. **Pickup Notification:** Alerts the customer that their laundry is clean, dried, folded, and ready for retrieval.

### Package Matrix
The pricing tiers are structured to absorb the **200 credits/month (₱112.00)** fixed operational cost for the custom Sender ID while ensuring a predictable recurring revenue stream from "breakage" (unused credit allocations).

| Plan Name | Monthly Retail Price | Allocated SMS | Est. Monthly Job Orders | Wholesale Cost Base (SMS + Sender ID) | Net Profit Margin | Target Shop Profile |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Starter Wash** | `₱299.00` | 250 SMS | ~125 JOs | ₱112.00 + ₱140.00 = **₱252.00** | **15.7%** (₱47.00) | Low-volume, family-owned setups |
| **Standard Spin** | `₱599.00` | 600 SMS | ~300 JOs | ₱112.00 + ₱336.00 = **₱448.00** | **25.2%** (₱151.00) | Standard urban neighborhood shops |
| **Mega Dry** | `₱1,199.00` | 1,500 SMS | ~750 JOs | ₱112.00 + ₱840.00 = **₱952.00** | **20.6%** (₱247.00) | High-volume commercial/industrial laundries |

---

## 2. High-Efficiency Template Strategy (1-SMS Segment Optimization)

To protect your margins, the software must rigidly enforce character limit bounds. Standard GSM-7 character encoding dictates a threshold of **160 characters per single SMS segment**. Going over this limit drops the threshold to **153 characters** per segment due to user data headers (UDH), doubling your Semaphore API costs.

### Optimized String Layouts

#### Template A: Drop-Off Receipt (89 Characters safely evaluated)
```text
[ShopName] JO#{jo_id}
Amt: P{amount}
Status: RECEIVED
We will text you once your laundry is ready.
```

#### Template B: Ready for Pickup (92 Characters safely evaluated)
```text
[ShopName] JO#{jo_id}
Amt: P{amount}
Status: READY FOR PICKUP
Please bring your claim stub to claim. Thank you!
```

*Developer Rule:* Implement programmatic truncation on the dynamically injected `[ShopName]` string (e.g., `.take(15)`) within your data validation layer to prevent dynamic string expansion from crossing the 160-character threshold.

---

## 3. Database Schema Blueprint (Room / SQLite)

To track subscription metrics, transaction ceilings, and usage balances without relying on external servers, the local SQLite/Room implementation requires two primary architectural entities: `SmsSubscription` and `SmsTransactionLog`.

```kotlin
package com.jimac.laundry_pos.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "sms_subscriptions")
data class SmsSubscription(
    @PrimaryKey val shopId: String,
    @ColumnInfo(name = "plan_name") val planName: String,
    @ColumnInfo(name = "allocated_sms") val allocatedSms: Int,
    @ColumnInfo(name = "used_sms") val usedSms: Int,
    @ColumnInfo(name = "billing_cycle_start") val billingCycleStart: Long, // Epoch Timestamp
    @ColumnInfo(name = "billing_cycle_end") val billingCycleEnd: Long,   // Epoch Timestamp
    @ColumnInfo(name = "is_active") val isActive: Boolean
) {
    val remainingSms: Int
        get() = (allocatedSms - usedSms).coerceAtLeast(0)
}

@Entity(tableName = "sms_transaction_logs")
data class SmsTransactionLog(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    @ColumnInfo(name = "job_order_id") val jobOrderId: String,
    @ColumnInfo(name = "recipient_number") val recipientNumber: String,
    @ColumnInfo(name = "message_body") val messageBody: String,
    @ColumnInfo(name = "segments_charged") val segmentsCharged: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "semaphore_api_response_id") val apiResponseId: String?
)
```

---

## 4. Network and Logic Implementation

### Retrofit Service API Interface
```kotlin
package com.jimac.laundry_pos.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SemaphoreApiService {
    @FormUrlEncoded
    @POST("api/v4/messages")
    suspend fun sendSms(
        @Field("apikey") apiKey: String,
        @Field("number") number: String,
        @Field("message") message: String,
        @Field("sendername") senderName: String
    ): Response<List<SemaphoreSmsResponse>>
}

data class SemaphoreSmsResponse(
    val message_id: Long,
    val user_id: Long,
    val user: String,
    val account_number: String,
    val number: String,
    val message: String,
    val status: String,
    val network: String,
    val type: String,
    val source: String
)
```

### Core Business Logic Dispatcher Repository
This block coordinates balance deduction checks, triggers the network task asynchronously via Kotlin Coroutines, and logs the execution inside the transaction layer.

```kotlin
package com.jimac.laundry_pos.repository

import com.jimac.laundry_pos.database.entities.SmsSubscription
import com.jimac.laundry_pos.database.entities.SmsTransactionLog
import com.jimac.laundry_pos.network.SemaphoreApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Math.ceil

class SmsNotificationRepository(
    private val apiService: SemaphoreApiService,
    private val subscriptionDao: SmsSubscriptionDao, 
    private val logDao: SmsTransactionLogDao,
    private val apiKey: String,
    private val senderName: String
) {
    suspend fun triggerLaundryNotification(
        shopId: String,
        jobOrderId: String,
        phoneNumber: String,
        message: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val subscription = subscriptionDao.getSubscription(shopId)
                ?: return@withContext Result.failure(Exception("No active SMS subscription found for this shop."))

            if (!subscription.isActive || subscription.remainingSms <= 0) {
                return@withContext Result.failure(Exception("Insufficient SMS balance. Please upgrade your tier."))
            }

            // Calculate local message segment counts
            val segmentCount = ceil(message.length.toDouble() / 160.0).toInt().coerceAtLeast(1)

            // Execute Remote A2P Gateway Dispatch
            val response = apiService.sendSms(apiKey, phoneNumber, message, senderName)
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val apiResponse = response.body()!![0]
                
                // Update Local Ledger
                subscriptionDao.updateUsedSmsBalance(shopId, subscription.usedSms + segmentCount)
                
                // Write Auditable Record
                logDao.insertLog(
                    SmsTransactionLog(
                        jobOrderId = jobOrderId,
                        recipientNumber = phoneNumber,
                        messageBody = message,
                        segmentsCharged = segmentCount,
                        apiResponseId = apiResponse.message_id.toString()
                    )
                )
                return@withContext Result.success(true)
            } else {
                return@withContext Result.failure(Exception("Gateway Error: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}
```

---

## 5. Deployment and Compliance Checklists

1. **Sender ID Registration:** Register your specialized custom alphabet-only string tag (`[ShopName]`) through your Semaphore dashboard before deployment. Unregistered strings revert to standard generic shared pool codes which decreases customer delivery confidence.
2. **Dynamic Segment Safeguard:** Implement visual length counters (`140 / 160`) directly inside the customization inputs layout inside the setup panel to warn the store operator if their templates are growing too long.
3. **Billing Reset Handlers:** Configure a silent background execution job running on local launch validation steps to compare current machine clock times against `billingCycleEnd`. When exceeded, reset `usedSms` back to `0` and recalculate plan authorization checks.