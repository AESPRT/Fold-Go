package com.aesprt.foldgo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.repository.ShopRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object ShopRegistration : SplashDestination()
    object Dashboard : SplashDestination()
}

class SplashViewModel(
    private val preferenceManager: PreferenceManager,
    private val shopRepository: ShopRepository
) : ViewModel() {

    suspend fun getNextDestination(): SplashDestination {
        val onboardingCompleted = preferenceManager.isOnboardingCompleted.first()
        if (!onboardingCompleted) {
            return SplashDestination.Onboarding
        }

        val hasShop = shopRepository.hasShop()
        if (!hasShop) {
            return SplashDestination.ShopRegistration
        }

        return SplashDestination.Dashboard
    }
}
