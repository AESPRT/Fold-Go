package com.aesprt.foldgo.presentation.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.repository.MachineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MachineUiState(
    val machines: List<Machine> = emptyList(),
    val isLoading: Boolean = false
)

class MachineViewModel(
    private val repository: MachineRepository
) : ViewModel() {

    val uiState: StateFlow<MachineUiState> = repository.getAllMachines()
        .map { machines ->
            MachineUiState(machines = machines, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MachineUiState(isLoading = true)
        )

    fun updateStatus(machineId: String, status: String) {
        viewModelScope.launch {
            repository.updateMachineStatus(machineId, status)
        }
    }
}
