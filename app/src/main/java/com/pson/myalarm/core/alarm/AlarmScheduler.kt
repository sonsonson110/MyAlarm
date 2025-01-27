package com.pson.myalarm.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pson.myalarm.core.receiver.AlarmReceiver
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
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
        val alarmTime = item.alarm.alarmTime

        // Schedule the alarm for the nearest time in the future
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarmTime.hour)
            set(Calendar.MINUTE, alarmTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (item.weeklySchedules.isEmpty()) {
                // No repeat days: Set to the nearest future time
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            } else {
                // Repeat days specified: Find the next valid day
                // Moves forward in time until match one in repeat dates
                while (!item.weeklySchedules.any { schedule ->
                        get(Calendar.DAY_OF_WEEK) == schedule.dayOfWeek.toCalendarDay()
                    }) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.alarm.id.hashCode(),
            generateIntent(item),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
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
            action = AlarmReceiver.RECEIVER_ACTION
            putExtra("ALARM_ID", item.alarm.id)
            putExtra("ALARM_NOTE", item.alarm.note)
        }
}