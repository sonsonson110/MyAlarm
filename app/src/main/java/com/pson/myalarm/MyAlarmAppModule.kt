package com.pson.myalarm

import android.content.Context
import androidx.work.WorkManager
import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.core.alarm.IAlarmScheduler
import com.pson.myalarm.core.data.AlarmDatabase
import com.pson.myalarm.core.data.repository.AlarmRepository
import com.pson.myalarm.core.data.repository.IAlarmRepository
import com.pson.myalarm.domain.ToggleAlarmUseCase

interface IMyAlarmAppModule {
    val database: AlarmDatabase
    val alarmRepository: IAlarmRepository
    val alarmScheduler: IAlarmScheduler
    val workManager: WorkManager

    // use case
    val toggleAlarmUseCase: ToggleAlarmUseCase
}

class MyAlarmAppModule(private val appContext: Context) : IMyAlarmAppModule {
    override val database: AlarmDatabase by lazy { AlarmDatabase.getDatabase(appContext) }

    override val alarmRepository: IAlarmRepository by lazy { AlarmRepository(database.alarmDao()) }

    override val alarmScheduler: IAlarmScheduler by lazy { AlarmScheduler(appContext) }

    override val workManager: WorkManager = WorkManager.getInstance(appContext)

    override val toggleAlarmUseCase: ToggleAlarmUseCase =
        ToggleAlarmUseCase(
            alarmRepository = alarmRepository,
            alarmScheduler = alarmScheduler,
        )
}