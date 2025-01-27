package com.pson.myalarm.ui.screens.alarm_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
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
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.ui.shared.DayCircle
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmListScreen(
    modifier: Modifier = Modifier,
    onEditAlarm: (Long?) -> Unit,
    recentSavedAlarmId: Long,
    resetRecentSavedAlarmId: () -> Unit,
    viewModel: AlarmListViewModel = viewModel(factory = AlarmListViewModel.Factory),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleting = (uiState.value as? AlarmListUiState.Success)?.isDeleting ?: false

    Scaffold(
        modifier = modifier,
        topBar = {
            val state = (uiState.value as? AlarmListUiState.Success)
            if (state != null && state.selectingMode) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val triStateCheckBoxState = when {
                                state.selectedItems.size == state.alarmsWithWeeklySchedules.size -> ToggleableState.On
                                state.selectedItems.isEmpty() -> ToggleableState.Off
                                else -> ToggleableState.Indeterminate
                            }
                            TriStateCheckbox(
                                state = triStateCheckBoxState,
                                onClick = viewModel::onTriStateCheckboxSelect,
                                enabled = !isDeleting
                            )
                            Text("${state.selectedItems.size} selected")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = viewModel::bulkDelete,
                            enabled = !isDeleting && state.selectedItems.any()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Bulk delete",
                                tint = if (!isDeleting && state.selectedItems.any())
                                    Color.Red.copy(alpha = 0.8f)
                                else
                                    Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(
                            onClick = viewModel::toggleSelectingMode,
                            enabled = !isDeleting
                        ) {
                            Icon(
                                Icons.Filled.Close, "Cancel selecting mode",
                                tint = if (isDeleting)
                                    Color.Gray.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        },
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
                val state = uiState.value as AlarmListUiState.Success
                val alarmsWithWeeklySchedules = state.alarmsWithWeeklySchedules
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
                            isSelected = state.selectedItems.contains(item.alarm.id),
                            selectingMode = state.selectingMode,
                            isHighlighted = isHighlighted,
                            onHighlightComplete = resetRecentSavedAlarmId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (state.selectingMode) {
                                            viewModel.onItemSelect(item.alarm.id)
                                        } else {
                                            onEditAlarm(item.alarm.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (!state.selectingMode) {
                                            viewModel.toggleSelectingMode()
                                            viewModel.onItemSelect(item.alarm.id)
                                        }
                                    }
                                ),
                            onToggleAlarm = viewModel::toggleAlarm,
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                // Overlay when deleting

                if (isDeleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable(indication = null, // disable ripple effect
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { }), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
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
    isSelected: Boolean,
    selectingMode: Boolean,
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
            .graphicsLayer {
                if (showHighlight) {
                    shadowElevation = 12.dp.toPx()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = if (selectingMode) Arrangement.SpaceEvenly else Arrangement.SpaceBetween
        ) {
            if (selectingMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .zIndex(-1f)
                )
            }
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
                    com.pson.myalarm.data.model.DayOfWeek.entries.forEach { day ->
                        DayCircle(
                            day.abbreviation,
                            item.weeklySchedules.any { it.dayOfWeek == day },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            if (!selectingMode)
                Switch(item.alarm.isActive, onCheckedChange = { onToggleAlarm(item) })
        }
    }
}

internal fun AlarmWithWeeklySchedules.getNextTriggerTimeDescription(): String {
    // Find the next valid schedule (this should be the same with AlarmScheduler::schedule)
    val alarmTime = alarm.alarmTime
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, alarmTime.hour)
        set(Calendar.MINUTE, alarmTime.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (weeklySchedules.isEmpty()) {
            // No repeat days: Set to the nearest future time
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            // Repeat days specified: Find the next valid day
            // Moves forward in time until match one in repeat dates
            while (!weeklySchedules.any { schedule ->
                    get(Calendar.DAY_OF_WEEK) == schedule.dayOfWeek.toCalendarDay()
                }) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            // For special case where same DOW *BUT* alarm time is before current time
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 7)
            }
        }
    }

    val duration = calendar.timeInMillis - System.currentTimeMillis()
    val days = TimeUnit.MILLISECONDS.toDays(duration)
    val hours = TimeUnit.MILLISECONDS.toHours(duration) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60

    // Build the description
    return buildString {
        append("in ")
        if (days > 0) append("$days days, ")
        if (hours > 0) append("$hours hours, ")
        append("$minutes minutes")
    }
}
