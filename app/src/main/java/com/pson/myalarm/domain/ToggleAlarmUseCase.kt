package com.pson.myalarm.domain

import com.pson.myalarm.core.alarm.IAlarmScheduler
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.repository.IAlarmRepository

class ToggleAlarmUseCase(
    private val alarmRepository: IAlarmRepository,
    private val alarmScheduler: IAlarmScheduler,
) {
    suspend operator fun invoke(item: AlarmWithWeeklySchedules) {
        alarmRepository.toggleAlarmActivation(item.alarm.id)
        if (!item.alarm.isActive) {
            alarmScheduler.schedule(item)
        } else {
            alarmScheduler.cancel(item)
        }
    }
}