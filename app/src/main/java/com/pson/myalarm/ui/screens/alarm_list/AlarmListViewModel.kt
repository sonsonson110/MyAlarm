package com.pson.myalarm.ui.screens.alarm_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmListViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
) : ViewModel() {
    private val _uiState: MutableStateFlow<AlarmListUiState> =
        MutableStateFlow(AlarmListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getAlarms()
    }

    fun toggleSelectingMode() {
        val state = _uiState.value as AlarmListUiState.Success
        val currentMode = state.selectingMode
        _uiState.update { state.copy(selectingMode = !currentMode, selectedItems = emptySet()) }
    }

    fun onItemSelect(itemId: Long) {
        val state = _uiState.value as AlarmListUiState.Success
        var currentItems = state.selectedItems
        currentItems = if (currentItems.contains(itemId)) {
            currentItems.minus(itemId)
        } else {
            currentItems.plus(itemId)
        }
        _uiState.update { state.copy(selectedItems = currentItems) }
    }

    fun onTriStateCheckboxSelect() {
        val state = _uiState.value as AlarmListUiState.Success
        if (state.selectedItems.size == state.alarmsWithWeeklySchedules.size) {
            _uiState.update { state.copy(selectedItems = emptySet()) }
        } else {
            val itemIds = state.alarmsWithWeeklySchedules.map { it.alarm.id }.toSet()
            _uiState.update { state.copy(selectedItems = itemIds) }
        }
    }

    fun bulkDelete() {
        val state = _uiState.value as AlarmListUiState.Success
        _uiState.update { state.copy(isDeleting = true) }

        // Cancel any pending alarm
        state.alarmsWithWeeklySchedules
            .filter { it.alarm.isActive && state.selectedItems.contains(it.alarm.id) }
            .forEach { alarmScheduler.cancel(it) }

        viewModelScope.launch {
            alarmRepository.deleteAlarms(state.selectedItems.toList())
            _uiState.update {
                state.copy(
                    isDeleting = false,
                    selectingMode = false,
                    selectedItems = emptySet()
                )
            }
        }
    }

    private fun getAlarms() {
        viewModelScope.launch {
            alarmRepository.getAllAlarms()
                .collect { alarms ->
                    _uiState.update {
                        if (alarms.isEmpty()) AlarmListUiState.Empty
                        else AlarmListUiState.Success(alarms)
                    }
                }
        }
    }

    fun toggleAlarm(item: AlarmWithWeeklySchedules) {
        viewModelScope.launch {
            if (!item.alarm.isActive) {
                alarmScheduler.schedule(item)
            } else {
                alarmScheduler.cancel(item)
            }
            alarmRepository.toggleAlarmActivation(item.alarm.id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY]) as MyAlarmApplication

                return AlarmListViewModel(
                    application.alarmRepository,
                    application.alarmScheduler,
                ) as T
            }
        }
    }
}

sealed interface AlarmListUiState {
    data class Success(
        val alarmsWithWeeklySchedules: List<AlarmWithWeeklySchedules>,
        val selectedItems: Set<Long> = emptySet(),
        // ui flag
        val selectingMode: Boolean = false,
        val isDeleting: Boolean = false,
    ) : AlarmListUiState

    data object Loading : AlarmListUiState

    data object Empty : AlarmListUiState

    data class Error(val message: String) : AlarmListUiState
}