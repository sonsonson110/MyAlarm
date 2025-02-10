package com.pson.myalarm.glance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pson.myalarm.R
import com.pson.myalarm.core.data.model.Alarm
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.model.DayOfWeek
import com.pson.myalarm.core.data.model.WeeklySchedule
import com.pson.myalarm.glance.MyAlarmWidget
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Use glance compose only, showing the most recent alarm
@Composable
internal fun OnGoingAlarmWidget(
    item: AlarmWithWeeklySchedules,
//    onWidgetClick: (Long) -> Unit,
    onWidgetClick: Action, // Remove later
    onAlarmCancel: () -> Unit,
) = with(item) {
    val timePart = alarm.alarmTime
        .format(DateTimeFormatter.ofPattern("hh:mm a"))
        .split(' ')

    Scaffold(
        backgroundColor = GlanceTheme.colors.surface,
        titleBar = {
            TitleBar(
                iconColor = GlanceTheme.colors.primary,
                startIcon = ImageProvider(R.drawable.outline_access_alarm_24),
                title = "Ongoing alarm"
            )
        },
//        Recover later
//        modifier = GlanceModifier.clickable { onWidgetClick(alarm.id) }
        modifier = GlanceModifier.clickable(onWidgetClick)
    ) {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    timePart[0],
                    style = TextStyle(
                        fontSize = 40.sp,
                        color = GlanceTheme.colors.primary
                    ),
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    timePart[1],
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.secondary
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.outline_snooze_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.size(14.dp)
                )
                Spacer(GlanceModifier.width(4.dp))
                val snoozeMinutes = alarm.snoozeTimeMinutes ?: 5
                Text(
                    "$snoozeMinutes minutes",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            if (weeklySchedules.isNotEmpty()) {
                Row {
                    com.pson.myalarm.core.data.model.DayOfWeek.entries.forEachIndexed { idx, day ->
                        val daysInWeek = com.pson.myalarm.core.data.model.DayOfWeek.entries.size
                        val isRepeatDay = weeklySchedules.map { it.dayOfWeek }.contains(day)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier.padding(end = if (idx != daysInWeek) 6.dp else 0.dp)
                        ) {
                            if (isRepeatDay) {
                                Box(
                                    modifier = GlanceModifier
                                        .width(6.dp).height(4.dp)
                                        .background(GlanceTheme.colors.primary)
                                        .cornerRadius(4.dp)
                                ) {}
                            } else {
                                Spacer(
                                    modifier = GlanceModifier
                                        .width(6.dp).height(4.dp)
                                )
                            }
                            Text(
                                day.abbreviation[0].toString(), style = TextStyle(
                                    fontSize = 14.sp,
                                    color = if (isRepeatDay) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                                    fontWeight = if (isRepeatDay) FontWeight.Bold else FontWeight.Normal,
                                )
                            )
                        }
                    }
                }
            }
            Spacer(GlanceModifier.height(6.dp))
            Button(
                "Cancel",
                onClick = onAlarmCancel,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = GlanceTheme.colors.surfaceVariant,
                    contentColor = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Composable
internal fun AlarmWidgetPreview() {
    GlanceTheme {
        // Placeholder purpose
        val startMainActivity = actionStartActivity(MyAlarmWidget.createMainActivityIntent())
        val item = AlarmWithWeeklySchedules(
            alarm = Alarm(
                id = 0L,
                alarmTime = LocalTime.of(8, 0),
                note = "Test alarm",
                snoozeTimeMinutes = 15,
                isActive = true,
            ),
            weeklySchedules = listOf(
                WeeklySchedule(dayOfWeek = DayOfWeek.MONDAY),
                WeeklySchedule(dayOfWeek = DayOfWeek.THURSDAY),
                WeeklySchedule(dayOfWeek = DayOfWeek.SUNDAY),
            )
        )
        OnGoingAlarmWidget(item, startMainActivity, {})
    }
}