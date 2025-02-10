package com.pson.myalarm.core.data.repository

import com.pson.myalarm.core.data.dao.AlarmDao
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import kotlinx.coroutines.flow.Flow

interface IAlarmRepository {
    suspend fun saveAlarm(item: AlarmWithWeeklySchedules): Long
    suspend fun deleteAlarm(alarmId: Long)
    suspend fun deleteAlarms(alarmIds: List<Long>)
    suspend fun toggleAlarmActivation(alarmId: Long)
    suspend fun getAlarm(alarmId: Long): AlarmWithWeeklySchedules?
    fun observeAllAlarms(): Flow<List<AlarmWithWeeklySchedules>>
    fun observeAllActiveAlarm(): Flow<List<AlarmWithWeeklySchedules>>
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

    override fun observeAllAlarms(): Flow<List<AlarmWithWeeklySchedules>> {
        return alarmDao.observeAll()
    }

    override fun observeAllActiveAlarm(): Flow<List<AlarmWithWeeklySchedules>> {
        return alarmDao.observeAllActive()
    }
}