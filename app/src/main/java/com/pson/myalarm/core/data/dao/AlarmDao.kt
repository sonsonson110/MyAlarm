package com.pson.myalarm.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pson.myalarm.core.data.model.Alarm
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.model.WeeklySchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Transaction
    @Query("SELECT * FROM alarms ORDER BY alarmTime")
    fun getAll(): Flow<List<AlarmWithWeeklySchedules>>

    @Transaction
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getById(alarmId: Long): AlarmWithWeeklySchedules?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Query("UPDATE alarms SET isActive = NOT isActive WHERE id = :alarmId")
    suspend fun toggleAlarmActivation(alarmId: Long)

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteAlarm(alarmId: Long)

    @Query("DELETE FROM alarms WHERE id in (:alarmIds)")
    suspend fun deleteAlarms(alarmIds: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmWeeklySchedules(weeklySchedules: List<WeeklySchedule>)

    @Query("DELETE FROM weekly_schedules WHERE alarmId = :alarmId")
    suspend fun deleteSchedulesByAlarmId(alarmId: Long)

    @Transaction
    suspend fun saveAlarm(item: AlarmWithWeeklySchedules): Long {
        val isInserting = item.alarm.id != 0L
        val recordId = insertAlarm(item.alarm)

        if (!isInserting) {
            deleteSchedulesByAlarmId(item.alarm.id)
        }
        val alarmWeeklySchedules = item.weeklySchedules.map { it.copy(alarmId = recordId) }
        insertAlarmWeeklySchedules(alarmWeeklySchedules)
        return recordId
    }
}