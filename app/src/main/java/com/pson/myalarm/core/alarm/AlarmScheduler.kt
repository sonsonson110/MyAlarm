package com.pson.myalarm.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pson.myalarm.core.receiver.AlarmReceiver
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.util.TimeHelper
import java.util.Calendar

interface IAlarmScheduler {
    fun schedule(item: AlarmWithWeeklySchedules)
    fun cancel(item: AlarmWithWeeklySchedules)
}

class AlarmScheduler(
    private val context: Context,
) : IAlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(item: AlarmWithWeeklySchedules) {
//        val scheduleTime = getFutureScheduleTime(item)
        val scheduleTime = System.currentTimeMillis() + 5000L

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.alarm.id.hashCode(),
            generateIntent(item),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(scheduleTime, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(scheduleTime, pendingIntent),
                pendingIntent
            )
        }
    }

    override fun cancel(item: AlarmWithWeeklySchedules) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.alarm.id.hashCode(),
            generateIntent(item),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun generateIntent(item: AlarmWithWeeklySchedules) =
        Intent(this.context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", item.alarm.id)
        }

    companion object {
        fun getFutureScheduleTime(item: AlarmWithWeeklySchedules): Long {
            val currentTime = TimeHelper.nowInMillis()

            // Schedule the alarm for the nearest time in the future
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime
                item.alarm.alarmTime.let {
                    set(Calendar.HOUR_OF_DAY, it.hour)
                    set(Calendar.MINUTE, it.minute)
                }
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // No repeat days: Set to the nearest future time
            if (item.weeklySchedules.isEmpty()) {
                if (calendar.timeInMillis <= currentTime) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                return calendar.timeInMillis
            }

            // For repeating alarm: find next valid day
            val isCurrentDayMatched = item.weeklySchedules.any { schedule ->
                calendar.get(Calendar.DAY_OF_WEEK) == schedule.dayOfWeek.toCalendarDay()
            }

            // If current day is not valid, find next valid day
            if (!isCurrentDayMatched) {
                do {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                } while (!item.weeklySchedules.any { schedule ->
                        calendar.get(Calendar.DAY_OF_WEEK) == schedule.dayOfWeek.toCalendarDay()
                    })
            }

            // Handle case when time is in past
            if (calendar.timeInMillis < currentTime) {
                when (item.weeklySchedules.size) {
                    // Single day repeat: jump to next week
                    1 -> calendar.add(Calendar.DAY_OF_MONTH, 7)
                    // Multiple days: find next valid day
                    else -> {
                        do {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        } while (!item.weeklySchedules.any { schedule ->
                                calendar.get(Calendar.DAY_OF_WEEK) == schedule.dayOfWeek.toCalendarDay()
                            })
                    }
                }
            }
            return calendar.timeInMillis
        }
    }
}