package com.pson.myalarm

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pson.myalarm.core.alarm.IAlarmScheduler
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.ui.screens.alarm_display.AlarmDisplayScreen
import com.pson.myalarm.ui.theme.MyAlarmTheme
import java.time.LocalTime

// TODO: implement
class AlarmDisplayActivity : ComponentActivity() {
    private lateinit var alarmScheduler: IAlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setWindowDisplayFlag()
        hideSystemBars()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Initialize scheduler
        alarmScheduler = MyAlarmApplication.appModule.alarmScheduler

        setContent {
            MyAlarmTheme {
                AlarmDisplayScreen(
                    AlarmWithWeeklySchedules(
                        alarm = Alarm(
                            id = 1,
                            alarmTime = LocalTime.of(11, 0),
                            note = "Test alarm",
                            isActive = true
                        ),
                        weeklySchedules = emptyList()
                    ),
                    onSnooze = { finish() },
                    onDismiss = { finish() })
            }
        }
    }

    private fun setWindowDisplayFlag() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or  // Dismiss keyguard
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or  // Show over lock screen
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON    // Turn screen on
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}