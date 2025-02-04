package com.pson.myalarm.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pson.myalarm.AlarmDisplayActivity
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.R
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.repository.IAlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class AlarmService : Service() {

    private lateinit var alarmRepository: IAlarmRepository

    override fun onCreate() {
        super.onCreate()
        alarmRepository = MyAlarmApplication.appModule.alarmRepository
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra("ALARM_ID", -1) ?: return START_NOT_STICKY
        if (alarmId == -1L) return START_NOT_STICKY

        ensureNotificationChannelExists()

        CoroutineScope(Dispatchers.IO).launch {
            val item = alarmRepository.getAlarm(alarmId)
            if (item == null) stopSelf()

            val notification = getAlarmNotification(item!!)
            startForeground(alarmId.hashCode(), notification)
        }
        return START_STICKY
    }

    private fun getAlarmNotification(item: AlarmWithWeeklySchedules): Notification =
        with(item.alarm) {
            val fullScreenIntent =
                Intent(this@AlarmService, AlarmDisplayActivity::class.java).apply {
                    putExtra("ALARM_ID", id)
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this@AlarmService,
                id.hashCode(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            return NotificationCompat.Builder(this@AlarmService, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(item.alarm.note ?: "Untitled alarm")
                .setContentText("Alarm is scheduled for ${alarmTime.format(timeFormatter)}. Tap here to dismiss!")
                .setSmallIcon(R.drawable.outline_access_alarm_24)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)  // Show even when locked
                .setOngoing(true)
                .setAutoCancel(false)
                .build()
        }

    private fun ensureNotificationChannelExists() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Alarm reminder notification",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Allow an alarm notification to show up"
        }
        // Register the channel with the system
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "alarm_channel"
    }
}