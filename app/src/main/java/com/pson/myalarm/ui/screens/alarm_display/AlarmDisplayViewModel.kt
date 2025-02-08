package com.pson.myalarm.ui.screens.alarm_display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.core.alarm.IAlarmScheduler
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.repository.IAlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmDisplayViewModel(
    private val alarmRepository: IAlarmRepository,
    private val alarmScheduler: IAlarmScheduler,
    private val alarmId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow<AlarmDisplayUiState>(AlarmDisplayUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getAlarm()
    }

    private fun getAlarm() {
        viewModelScope.launch {
            val item = alarmRepository.getAlarm(alarmId)
            if (item == null) {
                _uiState.update { AlarmDisplayUiState.Error("Ops, something is not right") }
            } else {
                _uiState.update { AlarmDisplayUiState.Success(item = item) }
            }
        }
    }

    fun snooze() {
        val state = _uiState.value as? AlarmDisplayUiState.Success ?: return
        alarmScheduler.snooze(state.item)
    }

    fun scheduleNext() {
        val state = _uiState.value as? AlarmDisplayUiState.Success ?: return
        alarmScheduler.schedule(state.item)
    }

    companion object {
        fun createFactory(alarmId: Long) = object : ViewModelProvider.Factory {
            @Suppress("Unchecked_cast")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AlarmDisplayViewModel(
                    alarmRepository = MyAlarmApplication.appModule.alarmRepository,
                    alarmScheduler = MyAlarmApplication.appModule.alarmScheduler,
                    alarmId = alarmId
                ) as T
            }
        }
    }
}

sealed interface AlarmDisplayUiState {
    data class Success(
        val item: AlarmWithWeeklySchedules
    ) : AlarmDisplayUiState

    data object Loading : AlarmDisplayUiState

    data class Error(val message: String) : AlarmDisplayUiState
}