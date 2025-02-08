package com.pson.myalarm.core.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class AlarmWithWeeklySchedules(
    @Embedded val alarm: Alarm,
    @Relation(
        parentColumn = "id",
        entityColumn = "alarmId"
    )
    val weeklySchedules: List<WeeklySchedule> = emptyList()
)
