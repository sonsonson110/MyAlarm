package com.pson.myalarm

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.pson.myalarm.glance.MyAlarmWidget
import kotlinx.coroutines.runBlocking

class MyAlarmApplication : Application() {
    lateinit var globalStateManager: GlobalStateManager
    lateinit var appModule: IMyAlarmAppModule

    override fun onCreate() {
        super.onCreate()
        appModule = MyAlarmAppModule(this@MyAlarmApplication)
        globalStateManager = GlobalStateManager()
        runBlocking {
            MyAlarmWidget().updateAll(this@MyAlarmApplication)
        }
    }
}