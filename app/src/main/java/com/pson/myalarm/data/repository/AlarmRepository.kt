package com.pson.myalarm.data.repository

import com.pson.myalarm.data.dao.AlarmDao
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules

internal interface IAlarmRepository {
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarmId: Long)
    suspend fun deleteAlarms(alarmIds: List<Long>)
    suspend fun getAlarm(alarmId: Long): AlarmWithWeeklySchedules?
    suspend fun getAllAlarms(): List<AlarmWithWeeklySchedules>
}

class AlarmRepository(private val alarmDao: AlarmDao) : IAlarmRepository {
    override suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlarm(alarmId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlarms(alarmIds: List<Long>) {
        TODO("Not yet implemented")
    }

    override suspend fun getAlarm(alarmId: Long): AlarmWithWeeklySchedules? {
        return alarmDao.getById(alarmId)
    }

    override suspend fun getAllAlarms(): List<AlarmWithWeeklySchedules> {
        return alarmDao.getAll()
    }
}