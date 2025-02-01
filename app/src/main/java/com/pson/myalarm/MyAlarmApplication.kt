package com.pson.myalarm

import android.app.Application

class MyAlarmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appModule = MyAlarmAppModule(this@MyAlarmApplication)
    }

    companion object {
        lateinit var appModule: MyAlarmAppModule
    }
}