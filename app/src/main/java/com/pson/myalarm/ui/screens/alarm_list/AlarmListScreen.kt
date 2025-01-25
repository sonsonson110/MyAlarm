package com.pson.myalarm.ui.screens.alarm_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.model.DateOfWeek
import com.pson.myalarm.ui.shared.DayCircle
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
internal fun AlarmListScreen(
    modifier: Modifier = Modifier,
    onEditAlarm: (Long?) -> Unit,
    recentSavedAlarmId: Long,
    resetRecentSavedAlarmId: () -> Unit,
    viewModel: AlarmListViewModel = viewModel(factory = AlarmListViewModel.Factory),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            // Show FAB only for Success or Empty states
            if (uiState.value is AlarmListUiState.Success ||
                uiState.value is AlarmListUiState.Empty
            ) {
                FloatingActionButton(onClick = { onEditAlarm(null) }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add Alarm"
                    )
                }
            }
        }
    ) { paddingValues ->
        when (uiState.value) {
            is AlarmListUiState.Success -> {
                val alarmsWithWeeklySchedules =
                    (uiState.value as AlarmListUiState.Success).alarmsWithWeeklySchedules
                val scrollState = rememberLazyListState()

                LaunchedEffect(recentSavedAlarmId) {
                    if (recentSavedAlarmId != -1L) {
                        val itemIndex =
                            alarmsWithWeeklySchedules.indexOfFirst { it.alarm.id == recentSavedAlarmId }
                        if (itemIndex != -1) {
                            scrollState.animateScrollToItem(itemIndex)
                        }
                    }
                }


                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(alarmsWithWeeklySchedules, key = { it.alarm.id }) { item ->
                        val isHighlighted = recentSavedAlarmId == item.alarm.id
                        AlarmItem(
                            item = item,
                            isHighlighted = isHighlighted,
                            onHighlightComplete = resetRecentSavedAlarmId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEditAlarm(item.alarm.id) },
                            onToggleAlarm = viewModel::toggleAlarm
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            is AlarmListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AlarmListUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No alarms set",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            is AlarmListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text((uiState.value as AlarmListUiState.Error).message)
                }
            }
        }
    }
}

@Composable
internal fun AlarmItem(
    item: AlarmWithWeeklySchedules,
    onToggleAlarm: (AlarmWithWeeklySchedules) -> Unit,
    isHighlighted: Boolean,
    onHighlightComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHighlight by remember { mutableStateOf(isHighlighted) }
    var currentDescription by remember { mutableStateOf(item.getNextTriggerTimeDescription()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentDescription = item.getNextTriggerTimeDescription()

            // Calculate the delay to the start of the next minute
            val currentTimeMillis = System.currentTimeMillis()
            val nextMinuteMillis = ((currentTimeMillis / 60000L) + 1) * 60000L
            val delayMillis = nextMinuteMillis - currentTimeMillis
            delay(delayMillis) // Delay until the start of the next minute
        }
    }

    LaunchedEffect(isHighlighted) {
        currentDescription = item.getNextTriggerTimeDescription()
    }

    if (showHighlight) {
        LaunchedEffect(Unit) {
            delay(5000) // Highlight duration (5 seconds)
            showHighlight = false
            onHighlightComplete()
        }
    }

    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 3.dp,
            color = if (showHighlight) Color.LightGray else Color.Transparent
        ),
        modifier = modifier
            .padding(8.dp)
            .graphicsLayer {
                if (showHighlight) {
                    shadowElevation = 12.dp.toPx()
                    translationY = -4.dp.toPx()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timePart =
                        item.alarm.alarmTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                            .split(' ')
                    Text(
                        timePart[0],
                        color = if (!item.alarm.isActive) MaterialTheme.colorScheme.outline else Color.Unspecified,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        timePart[1],
                        color = if (!item.alarm.isActive) MaterialTheme.colorScheme.outline else Color.Unspecified,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notification"
                    )
                    Text(
                        currentDescription,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    item.alarm.note ?: "Untitled",
                    color = if (!item.alarm.isActive) MaterialTheme.colorScheme.outline else Color.Unspecified
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DateOfWeek.entries.forEach { day ->
                        DayCircle(
                            day.abbreviation,
                            item.weeklySchedules.any { it.dateOfWeek == day },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Switch(item.alarm.isActive, onCheckedChange = { onToggleAlarm(item) })
        }
    }
}

internal fun AlarmWithWeeklySchedules.getNextTriggerTimeDescription(): String {
    val now = LocalDateTime.now()
    val currentTime = now.toLocalTime()
    val currentDay = now.dayOfWeek.value

    // If no repeat dates are specified, create a fallback schedule for today or tomorrow
    if (weeklySchedules.isEmpty()) {
        val timeDifference = if (alarm.alarmTime.isBefore(currentTime)) {
            // Add a full day to the second time to handle spanning midnight
            Duration.between(currentTime, alarm.alarmTime).plusDays(1)
        } else {
            Duration.between(currentTime, alarm.alarmTime)
        }

        val hours = timeDifference.toHours()
        val minutes = timeDifference.toMinutes() - hours * 60

        // Build the description
        return buildString {
            append("in ")
            if (hours > 0) append("$hours hours, ")
            if (minutes > 0) append("$minutes minutes")
        }
    }

    // Find the next valid schedule
    val nextSchedule = weeklySchedules.minBy { schedule ->
        val scheduleDay = schedule.dateOfWeek.toCalendarDay()
        val isToday = scheduleDay == currentDay
        val timeOffset = if (isToday && alarm.alarmTime > currentTime) {
            // Alarm is later today
            Duration.between(currentTime, alarm.alarmTime).toMinutes()
        } else {
            // Calculate days until the next valid day
            val daysUntil = (scheduleDay - currentDay + 7) % 7
            Duration.ofDays(daysUntil.toLong()).toMinutes() +
                    if (daysUntil == 0) 0 else alarm.alarmTime.toSecondOfDay() / 60L
        }
        timeOffset
    }

    val nextTriggerDateTime = now.with(
        TemporalAdjusters.nextOrSame(
            DayOfWeek.of(nextSchedule.dateOfWeek.toCalendarDay())
        )
    ).with(alarm.alarmTime)

    val duration = Duration.between(now, nextTriggerDateTime)
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60

    // Build the description
    return buildString {
        append("in ")
        if (days > 0) append("$days days, ")
        if (hours > 0) append("$hours hours, ")
        append("$minutes minutes")
    }
}
