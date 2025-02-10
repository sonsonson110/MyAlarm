package com.pson.myalarm.glance

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.glance.ui.AlarmTriggeringWidget
import com.pson.myalarm.glance.ui.CreateAlarmWidget
import com.pson.myalarm.glance.ui.OnGoingAlarmWidget
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MyAlarmWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val application = context.applicationContext as MyAlarmApplication
        val globalStateManager = application.globalStateManager
        val alarmRepository = application.appModule.alarmRepository
        val toggleAlarmUseCase = application.appModule.toggleAlarmUseCase

        provideContent {
            val scope = rememberCoroutineScope()
            val widgetState by combine(
                globalStateManager.triggeringAlarmId,
                alarmRepository.observeAllActiveAlarm()
            ) { triggeringId, alarms ->
                val isTriggering = triggeringId != -1L

                // Get nearest alarm
                val nearest = if (alarms.isEmpty()) null else {
                    alarms.map { alarm ->
                        alarm to AlarmScheduler.getFutureScheduleTime(alarm)
                    }.minByOrNull { (_, time) -> time }?.first
                }

                WidgetState(isTriggering, nearest)
            }.collectAsState(initial = WidgetState(false, null))

            GlanceTheme {
                // TODO: Route to matching alarm id in alarm list screen later
                val startMainActivity = actionStartActivity(createMainActivityIntent())
                when {
                    widgetState.isTriggering -> {
                        AlarmTriggeringWidget()
                    }
                    widgetState.nearestAlarm != null -> {
                        OnGoingAlarmWidget(
                            item = widgetState.nearestAlarm!!,
                            onWidgetClick = startMainActivity,
                            onAlarmCancel = {
                                scope.launch {
                                    toggleAlarmUseCase(widgetState.nearestAlarm!!)
                                }
                            }
                        )
                    }
                    else -> {
                        CreateAlarmWidget()
                    }
                }
            }
        }
    }
    companion object {
        fun createMainActivityIntent(): Intent {
            return Intent().apply {
                component = ComponentName(
                    "com.pson.myalarm",
                    "com.pson.myalarm.MainActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}

private data class WidgetState(
    val isTriggering: Boolean,
    val nearestAlarm: AlarmWithWeeklySchedules?
)