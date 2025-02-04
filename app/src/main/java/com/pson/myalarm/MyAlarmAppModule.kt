package com.pson.myalarm

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.data.AlarmDatabase
import com.pson.myalarm.data.repository.AlarmRepository
import com.pson.myalarm.data.repository.IAlarmRepository
import com.pson.myalarm.ui.screens.alarm_edit.AlarmEditViewModel
import com.pson.myalarm.ui.screens.alarm_list.AlarmListViewModel

interface IMyAlarmAppModule {
    val database: AlarmDatabase
    val alarmRepository: IAlarmRepository
    val alarmScheduler: AlarmScheduler
    val mainViewModelFactory: ViewModelProvider.Factory
}

class MyAlarmAppModule(private val appContext: Context) : IMyAlarmAppModule {
    override val database: AlarmDatabase by lazy { AlarmDatabase.getDatabase(appContext) }

    override val alarmRepository: IAlarmRepository
        get() = AlarmRepository(database.alarmDao())

    override val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(appContext) }

    override val mainViewModelFactory: ViewModelProvider.Factory
        get() = viewModelFactory {
            initializer {
                AlarmListViewModel(
                    alarmRepository,
                    alarmScheduler
                )
            }
            initializer {
                AlarmEditViewModel(
                    alarmRepository,
                    alarmScheduler,
                    this.createSavedStateHandle()
                )
            }
        }
}