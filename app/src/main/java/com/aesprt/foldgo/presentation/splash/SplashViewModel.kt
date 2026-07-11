package com.aesprt.foldgo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.usecase.HasShopUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object ShopRegistration : SplashDestination()
    object Login : SplashDestination()
    object StaffSelection : SplashDestination()
    object Dashboard : SplashDestination()
}

class SplashViewModel(
    private val preferenceManager: PreferenceManager,
    private val hasShopUseCase: HasShopUseCase
) : ViewModel() {

    suspend fun getNextDestination(): SplashDestination {
        val onboardingCompleted = preferenceManager.isOnboardingCompleted.first()
        if (!onboardingCompleted) {
            return SplashDestination.Onboarding
        }

        val hasShop = hasShopUseCase()
        if (!hasShop) {
            return SplashDestination.ShopRegistration
        }

        val currentShopId = preferenceManager.currentShopId.first()
        if (currentShopId == null) {
            return SplashDestination.Login
        }

        val currentStaffId = preferenceManager.currentStaffId.first()
        if (currentStaffId == null) {
            return SplashDestination.StaffSelection
        }

        return SplashDestination.Dashboard
    }
}
