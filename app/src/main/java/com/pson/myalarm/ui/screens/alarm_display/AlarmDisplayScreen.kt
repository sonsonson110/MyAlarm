package com.pson.myalarm.ui.screens.alarm_display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AlarmDisplayScreen(
    uiState: AlarmDisplayUiState,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState is AlarmDisplayUiState.Error) {
                Text(
                    uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                return@Box
            }
            if (uiState is AlarmDisplayUiState.Loading) {
                return@Box
            }
            // Central content (note and snooze button)
            val item = (uiState as? AlarmDisplayUiState.Success)?.item ?: return@Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Note display
                Text(
                    text = item.alarm.note ?: "Untitled alarm",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Snooze button
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .width(200.dp),
                ) {
                    Text(
                        "Snooze",
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
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    "Stop",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}