package com.pson.myalarm.core.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.pson.myalarm.ui.components.AlarmWidget

class MyAlarmWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: fetch widget data
        provideContent {
            AlarmWidget()
        }
    }
}