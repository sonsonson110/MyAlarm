package com.pson.myalarm.ui.screens.alarm_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmListViewModel(
    private val alarmRepository: AlarmRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState: MutableStateFlow<AlarmListUiState> =
        MutableStateFlow(AlarmListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getAlarms()
    }

    private fun getAlarms() {
        viewModelScope.launch {
            val alarms = alarmRepository.getAllAlarms()
            if (alarms.isEmpty())
                _uiState.update { AlarmListUiState.Empty }
            else
                _uiState.update { AlarmListUiState.Success(alarms) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return AlarmListViewModel(
                    (application as MyAlarmApplication).alarmRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}

sealed interface AlarmListUiState {
    data class Success(
        val alarmsWithWeeklySchedules: List<AlarmWithWeeklySchedules>
    ) : AlarmListUiState

    data object Loading : AlarmListUiState

    data object Empty : AlarmListUiState

    data class Error(val message: String) : AlarmListUiState
}