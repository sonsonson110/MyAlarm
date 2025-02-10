package com.pson.myalarm.glance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.pson.myalarm.R
import com.pson.myalarm.glance.MyAlarmWidget

@Composable
fun AlarmTriggeringWidget() {
    val startMainActivity = actionStartActivity(MyAlarmWidget.createMainActivityIntent())
    Scaffold(
        backgroundColor = GlanceTheme.colors.errorContainer,
        titleBar = {
            TitleBar(
                iconColor = GlanceTheme.colors.error,
                startIcon = ImageProvider(R.drawable.outline_access_alarm_24),
                title = "Alarm Ringing"
            )
        },
        modifier = GlanceModifier.clickable(startMainActivity)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.outline_alarm_on_24),
                contentDescription = "Ringing alarm",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.error),
                modifier = GlanceModifier.size(50.dp).fillMaxWidth()
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = "Alarm is ringing!",
                style = TextStyle(
                    color = GlanceTheme.colors.onErrorContainer,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun AlarmTriggeringWidgetPreview() {
    GlanceTheme {
        AlarmTriggeringWidget()
    }
}