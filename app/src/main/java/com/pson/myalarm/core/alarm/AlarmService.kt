package com.pson.myalarm.core.alarm

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.pson.myalarm.AlarmDisplayActivity
import com.pson.myalarm.GlobalStateManager
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.R
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.repository.IAlarmRepository
import com.pson.myalarm.ui.components.AlarmOverlay
import com.pson.myalarm.ui.theme.MyAlarmTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.time.format.DateTimeFormatter

class AlarmService : Service(), LifecycleOwner,
    SavedStateRegistryOwner {

    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        _savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle = _lifecycleRegistry

    private var alarmId = -1L
    private lateinit var alarmScheduler: IAlarmScheduler
    private lateinit var alarmRepository: IAlarmRepository

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    private var autoSnoozeJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        alarmRepository = MyAlarmApplication.appModule.alarmRepository
        alarmScheduler = MyAlarmApplication.appModule.alarmScheduler

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmId = intent?.getLongExtra("ALARM_ID", -1) ?: return START_NOT_STICKY
        if (alarmId == -1L) return START_NOT_STICKY

        // Block to check if alarm exists
        val item = runBlocking(Dispatchers.IO) {
            alarmRepository.getAlarm(alarmId)
        } ?: return START_NOT_STICKY  // Don't start if no alarm found

        ensureNotificationChannelExists()

        serviceScope.launch {
            GlobalStateManager.setTriggeringAlarmId(alarmId)
            val notification = getAlarmNotification(item)
            startForeground(alarmId.hashCode(), notification)

            // Main thread for UI
            withContext(Dispatchers.Main) {
                showOverlayWindow(item)
                playSound(item.alarm.audioUri)
                startAutoSnoozeTimer(item)
            }
        }
        return START_STICKY
    }

    private fun startAutoSnoozeTimer(item: AlarmWithWeeklySchedules) {
        autoSnoozeJob?.cancel() // Cancel any existing timer

        autoSnoozeJob = serviceScope.launch {
            delay(50_000L)
            // Auto snooze the alarm
            alarmScheduler.snooze(item)
            stopService()
        }
    }

    private fun getAlarmNotification(item: AlarmWithWeeklySchedules): Notification =
        with(item.alarm) {
            val fullScreenIntent =
                Intent(this@AlarmService, AlarmDisplayActivity::class.java).apply {
                    putExtra("ALARM_ID", id)
                    putExtra("SHOULD_EXIT_ON_EVENT", true)
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                or Intent.FLAG_FROM_BACKGROUND
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
                .setContentText("Alarm is set for ${alarmTime.format(timeFormatter)}. Tap to dismiss!")
                .setSmallIcon(R.drawable.outline_access_alarm_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)  // Show even when locked
                .build()
        }

    private fun showOverlayWindow(item: AlarmWithWeeklySchedules) {
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }

        composeView = ComposeView(this).apply {
            // https://www.techyourchance.com/jetpack-compose-inside-android-service/
            setViewTreeLifecycleOwner(this@AlarmService)
            setViewTreeSavedStateRegistryOwner(this@AlarmService)

            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            setContent {
                MyAlarmTheme {
                    AlarmOverlay(
                        title = item.alarm.note ?: "Untitled alarm",
                        time = item.alarm.alarmTime.format(timeFormatter),
                        onSnooze = {
                            alarmScheduler.snooze(item)
                            stopService()
                        },
                        onStop = {
                            alarmScheduler.schedule(item)
                            stopService()
                        }
                    )
                }
            }
        }
        windowManager.addView(composeView, params)
    }

    private fun hideOverlayWindow() {
        if (composeView == null) return

        windowManager.removeView(composeView)
        composeView = null

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private fun stopService() {
        autoSnoozeJob?.cancel()
        GlobalStateManager.setTriggeringAlarmId(-1)
        stopAlarmDisplayActivity()
        hideOverlayWindow()
        stopSound()
        // Remove the notification
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(alarmId.hashCode())
        // Stop the service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel all coroutines when service is destroyed
        stopService()
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private fun ensureNotificationChannelExists() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Alarm reminder notification",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Allow an alarm notification to show up"
            setSound(null, null) // keep the importance but remove sound
        }
        // Register the channel with the system
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private fun stopAlarmDisplayActivity() {
        if (!isAlarmActivityInForeground())
            return
        Intent(this@AlarmService, AlarmDisplayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("SHOULD_FINISH", true)
        }.let { startActivity(it) }
    }

    private fun isAlarmActivityInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTasks = activityManager.appTasks
        if (appTasks.isEmpty()) return false

        val topActivity = appTasks[0].taskInfo?.topActivity
        return topActivity?.className?.contains(AlarmDisplayActivity::class.simpleName!!) == true
    }

    private fun playSound(musicUri: String?) {
        stopSound()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )

            // Set data source
            try {
                val uri = musicUri?.let { Uri.parse(it) }
                if (uri != null) {
                    setDataSource(this@AlarmService, uri)
                } else {
                    throw FileNotFoundException("Music file not found")
                }
            } catch (e: Exception) {
                // Fallback to default alarm sound if the file is missing or any error occurs
                val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setDataSource(this@AlarmService, defaultAlarmUri)
            }

            isLooping = true
            prepare()
            start()
        }
    }

    private fun stopSound() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "alarm_channel"
    }
}