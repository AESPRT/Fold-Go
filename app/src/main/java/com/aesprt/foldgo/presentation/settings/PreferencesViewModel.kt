package com.aesprt.foldgo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.SmsSubscription
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class PreferencesUiState(
    val isSmsEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = false,
    val smsCredits: Int = 0,
    val planName: String? = null,
    val billingCycleEnd: Long? = null,
    val error: String? = null
)

class PreferencesViewModel(
    private val preferenceManager: PreferenceManager,
    private val getSubscriptionUseCase: GetSubscriptionUseCase,
    private val updateSubscriptionUseCase: UpdateSubscriptionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferenceManager.isSmsEnabled,
                preferenceManager.isNotificationsEnabled,
                preferenceManager.isDarkModeEnabled,
                getSubscriptionUseCase()
            ) { sms, notifications, darkMode, subscription ->
                PreferencesUiState(
                    isSmsEnabled = sms,
                    isNotificationsEnabled = notifications,
                    isDarkModeEnabled = darkMode,
                    smsCredits = subscription?.remainingSms ?: 0,
                    planName = subscription?.planName,
                    billingCycleEnd = subscription?.billingCycleEnd
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
                preferenceManager.setSmsEnabled(false)
            } else {
                preferenceManager.setSmsEnabled(enabled)
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun selectPlan(planName: String, smsCount: Int) {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: return@launch
            
            val calendar = Calendar.getInstance()
            val startTime = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val endTime = calendar.timeInMillis
            
            val newSubscription = SmsSubscription(
                shopId = shopId,
                planName = planName,
                allocatedSms = smsCount,
                usedSms = 0,
                billingCycleStart = startTime,
                billingCycleEnd = endTime,
                isActive = true
            )
            
            updateSubscriptionUseCase(newSubscription)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun buyCredits() {
        // This will now lead to plan selection
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
