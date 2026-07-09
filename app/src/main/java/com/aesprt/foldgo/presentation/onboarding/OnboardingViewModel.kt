package com.aesprt.foldgo.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            preferenceManager.setOnboardingCompleted(true)
        }
    }
}
