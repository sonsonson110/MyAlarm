package com.pson.myalarm.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmTime: LocalTime,
    val note: String? = null,
    val snoozeTimeMinutes: Int? = null,
    val isActive: Boolean = true,
    val audioUri: String? = null,
    val audioName: String? = null
)