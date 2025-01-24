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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.model.DateOfWeek
import com.pson.myalarm.ui.shared.DayCircle
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

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
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    var showHighlight by remember { mutableStateOf(isHighlighted) }

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
                Text(item.alarm.alarmTime.format(timeFormatter))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notification"
                    )
                    Text("in 20 hours, 30 minutes")
                }
                Text(item.alarm.note ?: "Untitled")
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