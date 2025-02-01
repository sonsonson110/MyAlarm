package com.pson.myalarm.ui.screens.alarm_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.model.DayOfWeek
import com.pson.myalarm.data.model.WeeklySchedule
import com.pson.myalarm.data.repository.IAlarmRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmEditViewModel(
    private val alarmRepository: IAlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val alarmId: Long = checkNotNull(savedStateHandle["alarmId"])

    private val _uiState: MutableStateFlow<AlarmEditUiState> =
        MutableStateFlow(AlarmEditUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    init {
        loadAlarm()
    }

    fun onUiStateChange(state: AlarmEditUiState.Success) = _uiState.update { state }

    fun onRepeatDaysChange(selectedDay: DayOfWeek) {
        val state = _uiState.value as AlarmEditUiState.Success
        var currentDays = state.repeatDays
        currentDays = if (currentDays.contains(selectedDay))
            currentDays.minus(selectedDay)
        else
            currentDays.plus(selectedDay)
        onUiStateChange(state.copy(repeatDays = currentDays))
    }

    private fun loadAlarm() {
        if (alarmId != 0L) {
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
                            repeatDays = item.weeklySchedules.map { it.dayOfWeek }.toSet(),
                            isActive = item.alarm.isActive
                        )
                    }
            }
        } else {
            _uiState.update { AlarmEditUiState.Success() }
        }
    }

    fun deleteAlarm(onDone: (() -> Unit)) {
        val state = _uiState.value as AlarmEditUiState.Success
        _uiState.update { state.copy(isDeleting = true) }
        viewModelScope.launch {
            try {
                alarmRepository.deleteAlarm(alarmId)
                alarmScheduler.cancel(state.toAlarmWithWeeklySchedules())
                onDone()
            } catch (e: Exception) {
                _snackbarMessages.emit("Failed to delete alarm: ${e.message ?: "Unknown error"}")
            } finally {
                _uiState.update { state.copy(isDeleting = false) }
            }
        }
    }

    fun saveAlarm(onDone: ((Long) -> Unit)) {
        val state = _uiState.value as AlarmEditUiState.Success
        _uiState.update { state.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val saveItem = state.toAlarmWithWeeklySchedules()
                val recordId = alarmRepository.saveAlarm(saveItem)

                if (isActive) {
                    val saveItemWithNewId =
                        saveItem.copy(alarm = saveItem.alarm.copy(id = recordId))
                    alarmScheduler.cancel(saveItemWithNewId)
                    alarmScheduler.schedule(saveItemWithNewId)
                }
                savedStateHandle["alarmId"] = recordId
                onDone(recordId)
            } catch (e: Exception) {
                _snackbarMessages.emit("Failed to save alarm: ${e.message ?: "Unknown error"}")
            } finally {
                _uiState.update { state.copy(isSaving = false) }
            }
        }
    }
}

sealed interface AlarmEditUiState {
    data class Success(
        val id: Long = 0,
        val alarmTime: LocalTime = LocalTime.now().plusMinutes(15),
        val note: String = "",
        val snoozeOption: SnoozeOption = SnoozeOption.Disabled,
        val repeatDays: Set<DayOfWeek> = emptySet(),
        val isActive: Boolean = true,
        // ui flag
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false
    ) : AlarmEditUiState {
        fun toAlarmWithWeeklySchedules(): AlarmWithWeeklySchedules = AlarmWithWeeklySchedules(
            alarm = Alarm(
                id = id,
                alarmTime = alarmTime,
                note = note.trim().ifEmpty { null },
                snoozeTimeMinutes = snoozeOption.minutes,
                isActive = isActive
            ),
            weeklySchedules = repeatDays.map {
                WeeklySchedule(dayOfWeek = it)
            }
        )
    }

    data object Loading : AlarmEditUiState

    data class Error(val message: String) : AlarmEditUiState
}

sealed class SnoozeOption(val minutes: Int?, val displayText: String) {
    data object Disabled : SnoozeOption(null, "Disable")
    data object FiveMin : SnoozeOption(5, "5 minutes")
    data object TenMin : SnoozeOption(10, "10 minutes")
    data object TwentyMin : SnoozeOption(20, "20 minutes")
    data object ThirtyMin : SnoozeOption(30, "30 minutes")
    data object OneHour : SnoozeOption(60, "1 hour")

    companion object {
        val options = listOf(Disabled, FiveMin, TenMin, TwentyMin, ThirtyMin, OneHour)

        fun fromMinutes(minutes: Int? = null): SnoozeOption {
            if (minutes == null) return Disabled
            return options.find { it.minutes == minutes } ?: Disabled
        }
    }
}