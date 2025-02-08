package com.pson.myalarm.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.pson.myalarm.core.data.model.Alarm
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.model.DayOfWeek
import com.pson.myalarm.core.data.model.WeeklySchedule
import com.pson.myalarm.glance.ui.AlarmWidget
import java.time.LocalTime

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