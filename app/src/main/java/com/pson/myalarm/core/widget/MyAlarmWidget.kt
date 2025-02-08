package com.pson.myalarm.core.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.model.DayOfWeek
import com.pson.myalarm.data.model.WeeklySchedule
import com.pson.myalarm.data.repository.AlarmRepository
import com.pson.myalarm.ui.components.AlarmWidget
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class MyAlarmWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: fetch most recent active alarm later
        provideContent {
            val item = AlarmWithWeeklySchedules(
                alarm = Alarm(
                    id = 0L,
                    alarmTime = LocalTime.of(8, 0),
                    note = "Test alarm",
                    isActive = true,
                ),
                weeklySchedules = listOf(
                    WeeklySchedule(dayOfWeek = DayOfWeek.MONDAY),
                    WeeklySchedule(dayOfWeek = DayOfWeek.THURSDAY),
                    WeeklySchedule(dayOfWeek = DayOfWeek.SUNDAY),
                )
            )
            AlarmWidget(
                item = item,
                onWidgetClick = { println("onWidgetClick") },
                onAlarmCancel = { println("onAlarmCancel") })
        }
    }
}