package com.pson.myalarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pson.myalarm.core.alarm.AlarmService
import com.pson.myalarm.ui.screens.alarm_display.AlarmDisplayScreen
import com.pson.myalarm.ui.screens.alarm_display.AlarmDisplayViewModel
import com.pson.myalarm.ui.theme.MyAlarmTheme
import kotlin.system.exitProcess

class AlarmDisplayActivity : ComponentActivity() {

    private lateinit var viewModel: AlarmDisplayViewModel
    private var shouldExitOnEvent = false
    private var alarmId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the data from the intent
        intent?.let {
            shouldExitOnEvent = it.getBooleanExtra("SHOULD_EXIT_ON_EVENT", false)
            alarmId = it.getLongExtra("ALARM_ID", -1)
        }

        if (alarmId == -1L) {
            exitProcess(0)
        }

        setWindowDisplayFlag()
        hideSystemBars()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        initializeDependencies()

        setContent {
            MyAlarmTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                AlarmDisplayScreen(
                    uiState = uiState,
                    onSnooze = {
                        viewModel.snooze()
                        stopAlarmService()
                        endAlarmDisplay()
                    },
                    onDismiss = {
                        viewModel.scheduleNext()
                        stopAlarmService()
                        endAlarmDisplay()
                    })
            }
        }
    }

    // Any interaction on navigation bar is counted as DISMISS button
    override fun onStop() {
        super.onStop()
        viewModel.snooze()
        stopAlarmService()
        finish() // finish the activity alone
    }

    private fun initializeDependencies() {

        viewModel = ViewModelProvider(
            this,
            AlarmDisplayViewModel.createFactory(alarmId)
        )[AlarmDisplayViewModel::class.java]
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

    private fun stopAlarmService() {
        val stopIntent = Intent(this, AlarmService::class.java)
        stopService(stopIntent)
    }

    // Used when user interacts with overlay when activity is upfront
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("SHOULD_FINISH", false)) {
            finishAffinity()
            return
        }
    }

    private fun endAlarmDisplay() {
        return when(shouldExitOnEvent) {
            true -> finishAffinity()
            false -> finish()
        }
    }
}