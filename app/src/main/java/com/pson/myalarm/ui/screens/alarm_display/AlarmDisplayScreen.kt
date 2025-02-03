package com.pson.myalarm.ui.screens.alarm_display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import java.time.LocalTime

@Composable
fun AlarmDisplayScreen(
    item: AlarmWithWeeklySchedules,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)), // Dim background to 70%
        contentAlignment = Alignment.Center
    ) {
        // Central content (note and snooze button)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Note display
            Text(
                text = item.alarm.note ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Snooze button
            Button(
                onClick = onSnooze,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    "Snooze 15min",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Stop button at bottom
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                "Stop",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun AlarmDisplayScreenPreview() {
    MaterialTheme {
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
            onSnooze = {},
            onDismiss = {}
        )
    }
}