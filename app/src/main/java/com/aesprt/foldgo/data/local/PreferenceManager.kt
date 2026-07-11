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
}
