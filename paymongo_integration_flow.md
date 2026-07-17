# PayMongo Mobile Integration Guide for Fold&Go (Android Integration Flow)

This document provides a comprehensive blueprint for integrating **PayMongo Hosted Checkout** into your **Fold&Go** backend and Android client application using an **Individual / Unregistered Account**. This flow natively supports GCash, Maya, and QR Ph.

---

## 1. Architectural Architecture & Data Flow

PayMongo checkouts are web-based. Secure operations (API key handling and fulfillment) happen strictly on your backend, while the Android app presents the checkout interface.

```
+---------------+              +--------------------+              +-------------------+
|  Android App  |              |  Fold&Go Backend   |              |   PayMongo API    |
+---------------+              +--------------------+              +-------------------+
        |                                |                                   |
        | 1. Request SMS Package         |                                   |
        |------------------------------->|                                   |
        |                                | 2. POST /v2/checkout_sessions    |
        |                                |---------------------------------->|
        |                                |                                   |
        |                                | 3. Return session object          |
        |                                |<----------------------------------|
        | 4. Deliver `checkout_url`      |                                   |
        |<-------------------------------|                                   |
        |                                                                    |
        | 5. Launch Custom Tab / WebView                                     |
        |------------------------------------------------------------------->| (User enters GCash/Maya OTP)
        |                                                                    |
        |                                | 6. Webhook Notification           |
        |                                |    (checkout_session.payment.paid)|
        |                                |<----------------------------------|
        |                                |                                   |
        |                                | 7. Validate & Provision Credits   |
        |                                |---.                               |
        |                                |   | Updates DB                    |
        |                                |<--'                               |
        | 8. Check balance / Refresh UI  |                                   |
        |------------------------------->|                                   |
        |                                |                                   |
```

---

## 2. PayMongo Account Activation Steps (Individual Starter)

Since you are running an unregistered business or starting as an early-stage individual developer, follow these steps to secure live credentials:

1. **Sign Up:** Create an account at [PayMongo](https://dashboard.paymongo.com/).
2. **Select Account Type:** Choose **Individual / Sole Proprietor (Unregistered)**.
3. **Submit Identity Verification (KYC):**
   * Provide a clear photo of one valid government ID (Passport, UMID, Driver's License, PhilID).
   * Complete the live selfie verification.
4. **Link Payout Account:** Input your personal Philippine bank account details (or digital wallet like Maya/GCash). Ensure the bank account owner name exactly matches your submitted government ID.
5. **Collect API Credentials:** Once activated, copy the **Live Secret Key** (`sk_live_...`) and **Live Public Key** (`pk_live_...`) from the **Developers** tab.

---

## 3. Backend Integration (API & Webhooks)

### Step A: Generate the Checkout Session
Your backend acts as the gateway to PayMongo. It obscures secrets from the client app and ensures precise value conversions (PayMongo processes values in **cents/centavos**).

* **Endpoint:** `POST https://api.paymongo.com/v2/checkout_sessions`
* **Headers:**
  * `Authorization: Basic <Base64 encoded Secret Key + colon>`
  * `Content-Type: application/json`

#### Production Payload Example
```json
{
  "data": {
    "attributes": {
      "billing": {
        "email": "customer@foldandgo.ph",
        "name": "Juan Dela Cruz",
        "phone": "+639171234567"
      },
      "send_email_receipt": true,
      "show_description": true,
      "show_line_items": true,
      "cancel_url": "https://api.foldandgo.com/v1/payments/redirect/cancel",
      "success_url": "https://api.foldandgo.com/v1/payments/redirect/success",
      "description": "Fold&Go SMS Package Top-up",
      "line_items": [
        {
          "amount": 50000,
          "currency": "PHP",
          "name": "1,000 SMS Credits Bundle",
          "quantity": 1
        }
      ],
      "payment_method_types": [
        "gcash",
        "paymaya",
        "qrph"
      ],
      "reference_number": "TXN-SMS-20260717-99",
      "metadata": {
        "user_id": "user_android_88271",
        "sms_credit_qty": "1000"
      }
    }
  }
}
```

### Step B: Establish and Listen to Webhooks
Do not rely on the client browser redirecting to the `success_url` to grant SMS credits. A network drop or closed tab will interrupt your loop. Implement a Webhook receiver.

1. Navigate to the PayMongo Dashboard -> **Developers** -> **Webhooks**.
2. Click **Create Webhook** and input your public endpoint (e.g., `https://api.foldandgo.com/v1/payments/paymongo-webhook`).
3. Select the event: `checkout_session.payment.paid`.
4. Secure the **Webhook Signing Secret** generated on the screen.

#### Backend Webhook Logic Implementation (Node.js/Express)
```javascript
const express = require('express');
const crypto = require('crypto');
const app = express();

app.use(express.json());

const WEBHOOK_SIGNING_SECRET = process.env.PAYMONGO_WH_SECRET;

app.post('/v1/payments/paymongo-webhook', async (req, res) => {
    const signature = req.headers['paymongo-signature'];
    
    // 1. Verify Request Signature to guarantee source security
    if (!signature) return res.status(400).send('Missing signature');
    
    const parts = signature.split(',');
    const timestamp = parts[0].split('=')[1];
    const originalSignature = parts[1].split('=')[1];
    
    const rawBody = JSON.stringify(req.body);
    const dataToSign = `${timestamp}.${rawBody}`;
    
    const computedSignature = crypto
        .createHmac('sha256', WEBHOOK_SIGNING_SECRET)
        .update(dataToSign)
        .digest('hex');
        
    if (computedSignature !== originalSignature) {
        return res.status(401).send('Signature mismatch verification failed');
    }

    // 2. Process Valid Payload
    const event = req.body.data;
    if (event.attributes.type === 'checkout_session.payment.paid') {
        const sessionObj = event.attributes.data.attributes;
        const referenceNumber = sessionObj.reference_number;
        const userId = sessionObj.metadata.user_id;
        const smsVolume = parseInt(sessionObj.metadata.sms_credit_qty, 10);

        try {
            // Idempotency: Verify your DB does not possess this referenceNumber as processed
            const alreadyProcessed = await checkDbForTransaction(referenceNumber);
            if (!alreadyProcessed) {
                await allocateSmsCredits(userId, smsVolume);
                await commitTransactionStatus(referenceNumber, 'SUCCESS');
            }
            return res.status(200).send({ status: 'fulfilled' });
        } catch (dbError) {
            console.error('Database transaction failure:', dbError);
            return res.status(500).send('Fulfillment pipeline stalled');
        }
    }
    
    res.status(400).send('Event unhandled');
});
```

---

## 4. Android Client Implementation (Kotlin)

Integrate using Android Custom Tabs for optimal stability, security, and runtime performance.

### Step A: Project Configuration
Append the Android Custom Tabs library inside your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.browser:browser:1.8.0")
    // Your preferred networking components (e.g., Ktor or Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
```

### Step B: Execution Code inside the Purchase Lifecycle
Implement the trigger inside your UI Component (Fragment or Activity). Ensure you query your own backend for balance verification when returning to the application screen focus context.

```kotlin
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SmsPurchaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_purchase)

        // Simulating selection of 1,000 SMS Package
        val packageId = "pkg_sms_1000"
        val currentUserId = "user_android_88271"

        initiatePurchasePipeline(currentUserId, packageId)
    }

    private fun initiatePurchasePipeline(userId: String, pkgId: String) {
        lifecycleScope.launch {
            try {
                // 1. Invoke API endpoint on your Fold&Go backend
                val response = RetrofitClient.apiService.createSession(CheckoutPayload(userId, pkgId))
                
                if (response.isSuccessful && response.body() != null) {
                    val checkoutUrl = response.body()!!.checkoutUrl
                    
                    // 2. Open Web Payment Flow securely via Android Custom Tabs
                    executeCustomTabPayment(this@SmsPurchaseActivity, checkoutUrl)
                } else {
                    showError("Failed to initiate checkout with backend gateway.")
                }
            } catch (e: Exception) {
                showError("Network exception encountered: ${e.localizedMessage}")
            }
        }
    }

    private fun executeCustomTabPayment(context: Context, targetUrl: String) {
        val customTabsIntent = CustomTabsIntent.Builder().apply {
            // Stylize toolbar match Fold&Go theme variables
            setToolbarColor(ContextCompat.getColor(context, R.color.foldandgo_primary))
            setShowTitle(true)
        }.build()
        
        customTabsIntent.launchUrl(context, Uri.parse(targetUrl))
    }

    override fun onResume() {
        super.onResume()
        // 3. User returned to app screen focus. Trigger a soft balance refresh check from backend.
        refreshUserSmsBalance()
    }

    private fun refreshUserSmsBalance() {
        // Query backend for newly added balance via background coroutine worker
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

// Network structural mappings
data class CheckoutPayload(val userId: String, val packageId: String)
data class CheckoutSessionResponse(val checkoutUrl: String, val referenceNumber: String)
```

---

## 5. Security & Launch Checklists

* **Never Expose Secret Keys:** Keep `sk_live_...` and your webhook signing secrets environment-locked strictly on your server production dashboard. Do not hardcode credentials inside Kotlin strings or build variants.
* **Idempotency Safeguard:** Ensure you mark incoming Webhook `reference_number` data as unique indices in your data layer to prevent duplicate runs from network retries.
* **User Intent Validation:** On returning to the app view inside `onResume()`, display a clean "Syncing transaction..." loader while fetching the system's true balance state.
