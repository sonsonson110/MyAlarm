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
import com.pson.myalarm.R

class AlarmService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        ensureNotificationChannelExists()
        startForeground(alarmId.hashCode(), getNotification(alarmId))
        return START_STICKY
    }

    private fun getNotification(alarmId: Long): Notification {
        val fullScreenIntent = Intent(this, AlarmDisplayActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Put alarm note here!")
            .setContentText("Alarm is scheduled for HH:mm. Tap here to dismiss!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)  // Show even when locked
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