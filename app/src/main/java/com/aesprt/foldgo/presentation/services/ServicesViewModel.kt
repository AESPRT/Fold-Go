package com.aesprt.foldgo.presentation.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Service
import com.aesprt.foldgo.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ServicesViewModel(
    private val serviceRepository: ServiceRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val shopId = preferenceManager.currentShopId.first()
            if (shopId != null) {
                serviceRepository.getServicesByShop(shopId).collect { services ->
                    _uiState.update { it.copy(services = services, isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Shop not found") }
            }
        }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            try {
                val shopId = preferenceManager.currentShopId.first()
                if (shopId != null) {
                    serviceRepository.upsertService(service.copy(shopId = shopId))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            try {
                serviceRepository.deleteService(service)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
