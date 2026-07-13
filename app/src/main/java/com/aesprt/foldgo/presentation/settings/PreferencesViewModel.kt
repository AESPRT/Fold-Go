package com.aesprt.foldgo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PreferencesUiState(
    val isSmsEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = false,
    val smsCredits: Int = 0,
    val error: String? = null
)

class PreferencesViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferenceManager.isSmsEnabled,
                preferenceManager.isNotificationsEnabled,
                preferenceManager.isDarkModeEnabled,
                preferenceManager.smsCredits
            ) { sms, notifications, darkMode, credits ->
                PreferencesUiState(
                    isSmsEnabled = sms,
                    isNotificationsEnabled = notifications,
                    isDarkModeEnabled = darkMode,
                    smsCredits = credits
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun toggleSms(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && _uiState.value.smsCredits <= 0) {
                _uiState.update { it.copy(error = "Insufficient SMS credits. Please buy credits to enable alerts.") }
                // Ensure it stays disabled if no credits
                preferenceManager.setSmsEnabled(false)
            } else {
                preferenceManager.setSmsEnabled(enabled)
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun buyCredits() {
        viewModelScope.launch {
            // Mocking buying credits - adding 50 credits
            val current = _uiState.value.smsCredits
            preferenceManager.setSmsCredits(current + 50)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setNotificationsEnabled(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkModeEnabled(enabled)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
