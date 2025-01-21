package com.pson.myalarm

import android.app.Application
import com.pson.myalarm.data.AlarmDatabase
import com.pson.myalarm.data.repository.AlarmRepository

class MyAlarmApplication: Application() {
    // Lazy initialization of database and repository
    private val database by lazy { AlarmDatabase.getDatabase(this) }
    val alarmRepository by lazy { AlarmRepository(database.alarmDao()) }
}