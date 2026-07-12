package com.aesprt.foldgo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "foldgo_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val CURRENT_SHOP_ID = stringPreferencesKey("current_shop_id")
        private val CURRENT_STAFF_ID = stringPreferencesKey("current_staff_id")
        private val CURRENT_STAFF_NAME = stringPreferencesKey("current_staff_name")
        private val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val SMS_CREDITS = intPreferencesKey("sms_credits")
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

    val isSmsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[SMS_ENABLED] ?: true }

    suspend fun setSmsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SMS_ENABLED] = enabled }
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_MODE_ENABLED] ?: false }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_ENABLED] = enabled }
    }

    val smsCredits: Flow<Int> = context.dataStore.data
        .map { it[SMS_CREDITS] ?: 0 }

    suspend fun setSmsCredits(credits: Int) {
        context.dataStore.edit { it[SMS_CREDITS] = credits }
    }

    suspend fun deductSmsCredit() {
        context.dataStore.edit { preferences ->
            val current = preferences[SMS_CREDITS] ?: 0
            if (current > 0) {
                preferences[SMS_CREDITS] = current - 1
            }
        }
    }
}
