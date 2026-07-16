package com.aesprt.foldgo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.aesprt.foldgo.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "foldgo_prefs")

class PreferenceManager(
    private val context: Context,
    private val shopRepository: ShopRepository
) {

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val CURRENT_SHOP_ID = stringPreferencesKey("current_shop_id")
        private val CURRENT_STAFF_ID = stringPreferencesKey("current_staff_id")
        private val CURRENT_STAFF_NAME = stringPreferencesKey("current_staff_name")
        
        // Settings Keys for the Map
        const val SMS_ENABLED_KEY = "sms_enabled"
        const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
        const val DARK_MODE_ENABLED_KEY = "dark_mode_enabled"
        const val SMS_CREDITS_KEY = "sms_credits"
        const val SMS_PLAN_NAME_KEY = "sms_plan_name"
        const val SMS_BILLING_CYCLE_END_KEY = "sms_billing_cycle_end"
        const val DELIVERY_FEES_KEY = "delivery_fees"
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val currentShopId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_SHOP_ID]
        }

    suspend fun setCurrentShopId(shopId: String?) {
        context.dataStore.edit { preferences ->
            if (shopId == null) {
                preferences.remove(CURRENT_SHOP_ID)
            } else {
                preferences[CURRENT_SHOP_ID] = shopId
            }
        }
    }

    val currentStaffId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_STAFF_ID]
        }

    suspend fun setCurrentStaffId(staffId: String?) {
        context.dataStore.edit { preferences ->
            if (staffId == null) {
                preferences.remove(CURRENT_STAFF_ID)
            } else {
                preferences[CURRENT_STAFF_ID] = staffId
            }
        }
    }

    val currentStaffName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_STAFF_NAME]
        }

    suspend fun setCurrentStaffName(name: String?) {
        context.dataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(CURRENT_STAFF_NAME)
            } else {
                preferences[CURRENT_STAFF_NAME] = name
            }
        }
    }

    val isSmsEnabled: Flow<Boolean> = shopRepository.getFirstShop()
        .map { it?.settings?.get(SMS_ENABLED_KEY)?.toBoolean() ?: true }

    suspend fun setSmsEnabled(enabled: Boolean) {
        val shop = shopRepository.getFirstShop().first() ?: return
        val updatedSettings = shop.settings.toMutableMap().apply {
            put(SMS_ENABLED_KEY, enabled.toString())
        }
        shopRepository.upsertShop(shop.copy(settings = updatedSettings))
    }

    val isNotificationsEnabled: Flow<Boolean> = shopRepository.getFirstShop()
        .map { it?.settings?.get(NOTIFICATIONS_ENABLED_KEY)?.toBoolean() ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        val shop = shopRepository.getFirstShop().first() ?: return
        val updatedSettings = shop.settings.toMutableMap().apply {
            put(NOTIFICATIONS_ENABLED_KEY, enabled.toString())
        }
        shopRepository.upsertShop(shop.copy(settings = updatedSettings))
    }

    val isDarkModeEnabled: Flow<Boolean> = shopRepository.getFirstShop()
        .map { it?.settings?.get(DARK_MODE_ENABLED_KEY)?.toBoolean() ?: false }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        val shop = shopRepository.getFirstShop().first() ?: return
        val updatedSettings = shop.settings.toMutableMap().apply {
            put(DARK_MODE_ENABLED_KEY, enabled.toString())
        }
        shopRepository.upsertShop(shop.copy(settings = updatedSettings))
    }

    val smsCredits: Flow<Int> = shopRepository.getFirstShop()
        .map { it?.settings?.get(SMS_CREDITS_KEY)?.toIntOrNull() ?: 0 }

    suspend fun setSmsCredits(credits: Int) {
        val shop = shopRepository.getFirstShop().first() ?: return
        val updatedSettings = shop.settings.toMutableMap().apply {
            put(SMS_CREDITS_KEY, credits.toString())
        }
        shopRepository.upsertShop(shop.copy(settings = updatedSettings))
    }

    val smsPlanName: Flow<String?> = shopRepository.getFirstShop()
        .map { it?.settings?.get(SMS_PLAN_NAME_KEY) }

    val smsBillingCycleEnd: Flow<Long?> = shopRepository.getFirstShop()
        .map { it?.settings?.get(SMS_BILLING_CYCLE_END_KEY)?.toLongOrNull() }

    suspend fun updateSubscriptionSettings(planName: String, credits: Int, billingCycleEnd: Long) {
        val shop = shopRepository.getFirstShop().first() ?: return
        val updatedSettings = shop.settings.toMutableMap().apply {
            put(SMS_PLAN_NAME_KEY, planName)
            put(SMS_CREDITS_KEY, credits.toString())
            put(SMS_BILLING_CYCLE_END_KEY, billingCycleEnd.toString())
        }
        shopRepository.upsertShop(shop.copy(settings = updatedSettings))
    }

    suspend fun deductSmsCredit() {
        val shop = shopRepository.getFirstShop().first() ?: return
        val current = shop.settings[SMS_CREDITS_KEY]?.toIntOrNull() ?: 0
        if (current > 0) {
            val updatedSettings = shop.settings.toMutableMap().apply {
                put(SMS_CREDITS_KEY, (current - 1).toString())
            }
            shopRepository.upsertShop(shop.copy(settings = updatedSettings))
        }
    }

    val deliveryFees: Flow<List<Double>> = shopRepository.getFirstShop()
        .map { 
            it?.settings?.get(DELIVERY_FEES_KEY)
                ?.split(",")
                ?.mapNotNull { s -> s.toDoubleOrNull() } 
                ?: listOf(50.0, 100.0)
        }

    suspend fun setDeliveryFees(fees: List<Double>) {
        val shop = shopRepository.getFirstShop().first() ?: return
        val updatedSettings = shop.settings.toMutableMap().apply {
            put(DELIVERY_FEES_KEY, fees.joinToString(","))
        }
        shopRepository.upsertShop(shop.copy(settings = updatedSettings))
    }
}
