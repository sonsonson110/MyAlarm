package com.pson.myalarm.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == RECEIVER_ACTION) {
            val alarmId = intent.getLongExtra("ALARM_ID", -1)
            val note = intent.getStringExtra("ALARM_NOTE")
            println("Alarm triggered: $alarmId - $note")
        }
    }

    companion object {
        const val RECEIVER_ACTION = "ALARM_TRIGGER"
    }
}