package com.pson.myalarm.ui.screens.alarm_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.core.alarm.IAlarmScheduler
import com.pson.myalarm.core.data.model.Alarm
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.model.DayOfWeek
import com.pson.myalarm.core.data.model.WeeklySchedule
import com.pson.myalarm.core.data.repository.IAlarmRepository
import com.pson.myalarm.core.worker.AudioFileCopyWorker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalTime
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AlarmEditViewModel(
    private val alarmRepository: IAlarmRepository,
    private val alarmScheduler: IAlarmScheduler,
    savedStateHandle: SavedStateHandle,
    private val workManager: WorkManager
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

    fun onMusicSelected(uri: String?, name: String?) {
        val state = _uiState.value as AlarmEditUiState.Success
        _uiState.update {
            state.copy(audioUri = uri, audioName = name)
        }
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
                            isActive = item.alarm.isActive,
                            audioUri = item.alarm.audioUri,
                            audioName = item.alarm.audioName
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
                val alarmId = when {
                    state.audioUri != null -> saveAlarmWithAudio(state)
                    else -> saveAlarmWithoutAudio(state)
                }
                onDone(alarmId)
            } catch (e: Exception) {
                _snackbarMessages.emit("Failed to save alarm: ${e.message ?: "Unknown error"}")
            } finally {
                _uiState.update { state.copy(isSaving = false) }
            }
        }
    }

    private suspend fun saveAlarmWithAudio(state: AlarmEditUiState.Success): Long {
        var saveItem = state.toAlarmWithWeeklySchedules()
        val hasExisted = saveItem.alarm.id != 0L
        val audioCopyWorkId =
            copyAudioToAppDir(saveItem.alarm.audioUri!!, saveItem.alarm.audioName!!)

        return withTimeoutOrNull(30_000) { // 30 seconds timeout
            suspendCancellableCoroutine { continuation ->
                val workInfoFlow = workManager.getWorkInfoByIdFlow(audioCopyWorkId)

                viewModelScope.launch {
                    workInfoFlow.collect { workInfo ->
                        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                            val audioUriFromAppDir =
                                workInfo.outputData.getString(AudioFileCopyWorker.KEY_RESULT_URI)
                            saveItem =
                                saveItem.copy(alarm = saveItem.alarm.copy(audioUri = audioUriFromAppDir))

                            val recordId = alarmRepository.saveAlarm(saveItem)
                            saveItem = saveItem.copy(alarm = saveItem.alarm.copy(id = recordId))
                            scheduleAlarm(saveItem, hasExisted)
                            continuation.resume(recordId)
                        } else if (workInfo?.state == WorkInfo.State.FAILED) {
                            continuation.resumeWithException(Exception("Failed to copy audio file"))
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    workManager.cancelWorkById(audioCopyWorkId)
                }
            }
        } ?: throw Exception("Audio copy operation timed out")
    }

    private suspend fun saveAlarmWithoutAudio(state: AlarmEditUiState.Success): Long {
        var saveItem = state.toAlarmWithWeeklySchedules()
        val hasExisted = saveItem.alarm.id != 0L
        val recordId = alarmRepository.saveAlarm(saveItem)
        saveItem = saveItem.copy(alarm = saveItem.alarm.copy(id = recordId))
        scheduleAlarm(saveItem, hasExisted)
        return recordId
    }

    private fun scheduleAlarm(item: AlarmWithWeeklySchedules, hasExisted: Boolean) {
        if (!item.alarm.isActive) return
        if (hasExisted)
            alarmScheduler.cancel(item)
        alarmScheduler.schedule(item)
    }

    private fun copyAudioToAppDir(uri: String, fileName: String): UUID {
        // https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#expedited
        val copyWork = OneTimeWorkRequestBuilder<AudioFileCopyWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(
                workDataOf(
                    AudioFileCopyWorker.KEY_INPUT_URI to uri,
                    AudioFileCopyWorker.KEY_FILE_NAME to fileName
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            AudioFileCopyWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            copyWork
        )

        return copyWork.id
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as MyAlarmApplication
                val repository = application.appModule.alarmRepository
                val scheduler = application.appModule.alarmScheduler
                val workManager = application.appModule.workManager
                val savedStateHandle = extras.createSavedStateHandle()

                return AlarmEditViewModel(
                    alarmRepository = repository,
                    alarmScheduler = scheduler,
                    savedStateHandle = savedStateHandle,
                    workManager = workManager,
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
        val repeatDays: Set<DayOfWeek> = emptySet(),
        val isActive: Boolean = true,
        val audioUri: String? = null,
        val audioName: String? = null,
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
                isActive = isActive,
                audioUri = audioUri,
                audioName = audioName
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