package com.pson.myalarm.glance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.pson.myalarm.R
import com.pson.myalarm.glance.MyAlarmWidget

@Composable
internal fun CreateAlarmWidget() {
    val startMainActivity = actionStartActivity(MyAlarmWidget.createMainActivityIntent())
    Scaffold(
        backgroundColor = GlanceTheme.colors.surface,
        titleBar = {
            TitleBar(
                iconColor = GlanceTheme.colors.primary,
                startIcon = ImageProvider(R.drawable.outline_access_alarm_24),
                title = "No active alarm"
            )
        }
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.outline_alarm_add_24),
                contentDescription = "Add alarm",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                modifier = GlanceModifier.size(50.dp)
            )
            Spacer(GlanceModifier.height(12.dp))
            Button(
                text = "Create Alarm",
                onClick = startMainActivity,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = GlanceTheme.colors.primary,
                    contentColor = GlanceTheme.colors.onPrimary
                )
            )
        }
    }
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun CreateAlarmWidgetPreview() {
    GlanceTheme {
        CreateAlarmWidget()
    }
}