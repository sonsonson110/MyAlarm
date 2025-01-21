package com.pson.myalarm.ui.screens.alarm_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.data.model.DateOfWeek
import com.pson.myalarm.data.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmEditViewModel(
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val alarmId: Long? = savedStateHandle["alarmId"]

    private val _uiState: MutableStateFlow<AlarmEditUiState> =
        MutableStateFlow(AlarmEditUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadAlarm()
    }

    fun onUiStateChange(state: AlarmEditUiState.Success) = _uiState.update { state }

    private fun loadAlarm() {
        if (alarmId != null && alarmId != -1L) {
            viewModelScope.launch {
                val item = alarmRepository.getAlarm(alarmId)
                if (item == null)
                    _uiState.update { AlarmEditUiState.Error("Alarm not found!") }
                else
                    _uiState.update {
                        AlarmEditUiState.Success(
                            id = item.alarm.id,
                            alarmTime = item.alarm.alarmTime,
                            note = item.alarm.note ?: "",
                            snoozeOption = SnoozeOption.fromMinutes(item.alarm.snoozeTimeMinutes),
                            repeatDates = item.weeklySchedules.map { it.dateOfWeek }
                        )
                    }
            }
        } else {
            _uiState.update { AlarmEditUiState.Success() }
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

                return AlarmEditViewModel(
                    (application as MyAlarmApplication).alarmRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}

sealed interface AlarmEditUiState {
    data class Success(
        val id: Long = 0,
        val alarmTime: LocalTime = LocalTime.now().plusMinutes(15),
        val note: String = "",
        val snoozeOption: SnoozeOption = SnoozeOption.FiveMin,
        val repeatDates: List<DateOfWeek> = emptyList()
    ) : AlarmEditUiState

    data object Loading : AlarmEditUiState

    data class Error(val message: String) : AlarmEditUiState
}

sealed class SnoozeOption(private val minutes: Int, val displayText: String) {
    data object Disabled : SnoozeOption(0, "Disable")
    data object FiveMin : SnoozeOption(5, "5 minutes")
    data object TenMin : SnoozeOption(10, "10 minutes")
    data object TwentyMin : SnoozeOption(20, "20 minutes")
    data object ThirtyMin : SnoozeOption(30, "30 minutes")
    data object OneHour : SnoozeOption(60, "1 hour")

    companion object {
        val options = listOf(Disabled, FiveMin, TenMin, TwentyMin, ThirtyMin, OneHour)

        fun fromMinutes(minutes: Int): SnoozeOption {
            return options.find { it.minutes == minutes } ?: Disabled
        }
    }
}