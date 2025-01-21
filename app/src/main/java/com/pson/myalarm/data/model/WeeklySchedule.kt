package com.pson.myalarm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_schedules", foreignKeys = [
        ForeignKey(
            entity = Alarm::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        ),
    ], indices = [Index(value = ["alarmId"])]
)
data class WeeklySchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long = 0,
    val dateOfWeek: DateOfWeek,
)
