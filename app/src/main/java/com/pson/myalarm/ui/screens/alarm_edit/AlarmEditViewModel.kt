package com.pson.myalarm.ui.screens.alarm_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.model.DateOfWeek
import com.pson.myalarm.data.model.WeeklySchedule
import com.pson.myalarm.data.repository.AlarmRepository
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
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
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

    fun onRepeatDatesChange(selectedDate: DateOfWeek) {
        val state = _uiState.value as AlarmEditUiState.Success
        var currentDates = state.repeatDates
        currentDates = if (currentDates.contains(selectedDate))
            currentDates.minus(selectedDate)
        else
            currentDates.plus(selectedDate)
        onUiStateChange(state.copy(repeatDates = currentDates))
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
                            repeatDates = item.weeklySchedules.map { it.dateOfWeek }.toSet(),
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

    fun saveAlarm(onDone: (() -> Unit)) {
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
                onDone()
            } catch (e: Exception) {
                _snackbarMessages.emit("Failed to save alarm: ${e.message ?: "Unknown error"}")
            } finally {
                _uiState.update { state.copy(isSaving = false) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY]) as MyAlarmApplication
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return AlarmEditViewModel(
                    application.alarmRepository,
                    application.alarmScheduler,
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
        val snoozeOption: SnoozeOption = SnoozeOption.Disabled,
        val repeatDates: Set<DateOfWeek> = emptySet(),
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
            weeklySchedules = repeatDates.map {
                WeeklySchedule(dateOfWeek = it)
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

        fun fromMinutes(minutes: Int?): SnoozeOption {
            if (minutes == null) return Disabled
            return options.find { it.minutes == minutes } ?: Disabled
        }
    }
}