package com.pson.myalarm.ui.screens

sealed class Screens(val route: String) {
    data object AlarmListScreen : Screens("alarms")
    data object AlarmEditScreen : Screens("alarms/edit?alarmId={alarmId}") {
        fun createRoute(alarmId: Long?) = "alarms/edit?alarmId=$alarmId"
    }

    data object TimerScreen : Screens("timer")

    data object StopwatchScreen : Screens("stopwatch")
}