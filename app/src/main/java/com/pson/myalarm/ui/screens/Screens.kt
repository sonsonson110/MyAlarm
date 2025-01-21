package com.pson.myalarm.ui.screens

sealed class Screens(val route: String) {
    data object AlarmScreen : Screens("alarm")
    data object AlarmEditScreen : Screens("alarm/edit?alarmId={alarmId}") {
        fun createRoute(alarmId: Long?) = "alarm/edit?alarmId=$alarmId"
    }

    data object TimerScreen : Screens("timer")

    data object StopwatchScreen : Screens("stopwatch")
}