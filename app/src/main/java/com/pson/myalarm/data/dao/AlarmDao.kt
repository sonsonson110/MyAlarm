package com.pson.myalarm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules

@Dao
interface AlarmDao {
    @Transaction
    @Query("SELECT * FROM alarms ORDER BY alarmTime")
    suspend fun getAll(): List<AlarmWithWeeklySchedules>

    @Transaction
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getById(alarmId: Long): AlarmWithWeeklySchedules?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
}