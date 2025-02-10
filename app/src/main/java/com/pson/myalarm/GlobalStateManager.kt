package com.pson.myalarm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GlobalStateManager {
    private val _triggeringAlarmId = MutableStateFlow<Long>(-1)
    val triggeringAlarmId = _triggeringAlarmId.asStateFlow()

    private val _mainActivityInForeground = MutableStateFlow(false)

    // Combined state flow that emits only when both conditions are met
    @OptIn(DelicateCoroutinesApi::class)
    private val shouldShowAlarm: StateFlow<Pair<Long, Boolean>> = combine(
        _triggeringAlarmId,
        _mainActivityInForeground
    ) { alarmId, isInForeground ->
        Pair(alarmId, isInForeground)
    }.stateIn(
        scope = GlobalScope,
        started = SharingStarted.Eagerly,
        initialValue = Pair(-1L, false)
    )

    fun setTriggeringAlarmId(alarmId: Long) {
        _triggeringAlarmId.value = alarmId
    }

    fun setIsMainActivityInForeground(bool: Boolean) {
        _mainActivityInForeground.value = bool
    }

    // Only use for alarm display screen
    fun observeAlarmTrigger(
        scope: CoroutineScope,
        onTrigger: (Long) -> Unit
    ) {
        scope.launch {
            shouldShowAlarm.collect { (alarmId, isInForeground) ->
                if (alarmId != -1L && isInForeground) {
                    onTrigger(alarmId)
                }
            }
        }
    }
}