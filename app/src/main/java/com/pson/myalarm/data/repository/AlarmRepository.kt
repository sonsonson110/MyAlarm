package com.pson.myalarm.data.repository

import com.pson.myalarm.data.dao.AlarmDao
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import kotlinx.coroutines.flow.Flow

internal interface IAlarmRepository {
    suspend fun saveAlarm(item: AlarmWithWeeklySchedules): Long
    suspend fun deleteAlarm(alarmId: Long)
    suspend fun deleteAlarms(alarmIds: List<Long>)
    suspend fun toggleAlarmActivation(alarmId: Long)
    suspend fun getAlarm(alarmId: Long): AlarmWithWeeklySchedules?
    suspend fun getAllAlarms(): Flow<List<AlarmWithWeeklySchedules>>
}

class AlarmRepository(private val alarmDao: AlarmDao) : IAlarmRepository {
    override suspend fun saveAlarm(item: AlarmWithWeeklySchedules): Long {
        return alarmDao.saveAlarm(item)
    }

    override suspend fun deleteAlarm(alarmId: Long) {
        return alarmDao.deleteAlarm(alarmId)
    }

    override suspend fun deleteAlarms(alarmIds: List<Long>) {
        return alarmDao.deleteAlarms(alarmIds)
    }

    override suspend fun toggleAlarmActivation(alarmId: Long) {
        return alarmDao.toggleAlarmActivation(alarmId)
    }

    override suspend fun getAlarm(alarmId: Long): AlarmWithWeeklySchedules? {
        return alarmDao.getById(alarmId)
    }

    override suspend fun getAllAlarms(): Flow<List<AlarmWithWeeklySchedules>> {
        return alarmDao.getAll()
    }
}