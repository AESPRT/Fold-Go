package com.aesprt.foldgo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Staff
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StaffSelectionUiState(
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val shopId: String = ""
)

class StaffSelectionViewModel(
    private val getStaffByShopUseCase: GetStaffByShopUseCase,
    private val upsertStaffUseCase: UpsertStaffUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffSelectionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceManager.currentShopId.collect { shopId ->
                if (shopId != null) {
                    _uiState.update { it.copy(shopId = shopId) }
                    loadStaff(shopId)
                }
            }
        }
    }

    private fun loadStaff(shopId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getStaffByShopUseCase(shopId).collect { list ->
                _uiState.update { it.copy(staffList = list, isLoading = false) }
            }
        }
    }

    fun addStaff(name: String, role: String) {
        val shopId = _uiState.value.shopId
        if (shopId.isBlank()) return

        viewModelScope.launch {
            val staff = Staff(
                staffId = IdGeneratorUtils.generateStaffId(),
                shopId = shopId,
                name = name,
                role = role,
                createdAt = System.currentTimeMillis()
            )
            upsertStaffUseCase(staff)
        }
    }

    fun selectStaff(staff: Staff) {
        viewModelScope.launch {
            preferenceManager.setCurrentStaffId(staff.staffId)
            preferenceManager.setCurrentStaffName(staff.name)
        }
    }
}
