package com.pson.myalarm.ui.screens.alarm_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pson.myalarm.MyAlarmApplication
import com.pson.myalarm.R
import com.pson.myalarm.data.model.DayOfWeek
import com.pson.myalarm.ui.shared.DayCircle
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmEditScreen(
    onNavigateUp: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmEditViewModel = viewModel<AlarmEditViewModel>(factory = MyAlarmApplication.appModule.viewModelFactory)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isBusyPersisting = (uiState.value as? AlarmEditUiState.Success)?.let {
        it.isSaving || it.isDeleting
    } ?: false

    // Collect snackbar message
    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, topBar = {
        TopAppBar(title = { Text("Edit alarm") }, navigationIcon = {

            IconButton(
                onClick = { onNavigateUp(null) }, enabled = !isBusyPersisting
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate up button"
                )
            }
        }, actions = {
            val isUpdating = (uiState.value as? AlarmEditUiState.Success)?.id != 0L
            if (isUpdating) {
                IconButton(
                    onClick = { viewModel.deleteAlarm { onNavigateUp(null) } },
                    enabled = !isBusyPersisting
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        "Delete",
                        tint = if (!isBusyPersisting) Color.Red.copy(0.8f) else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        })
    }) { paddingValues ->
        when (uiState.value) {
            is AlarmEditUiState.Loading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AlarmEditUiState.Error -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text((uiState.value as AlarmEditUiState.Error).message)
                }
            }

            is AlarmEditUiState.Success -> {
                val state = uiState.value as AlarmEditUiState.Success
                val timePickerState = rememberTimePickerState(
                    is24Hour = false,
                    initialHour = state.alarmTime.hour,
                    initialMinute = state.alarmTime.minute
                )

                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    viewModel.onUiStateChange(
                        state.copy(
                            alarmTime = LocalTime.of(
                                timePickerState.hour, timePickerState.minute
                            )
                        )
                    )
                }

                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val focusManager = LocalFocusManager.current

                    DialExample(timePickerState, Modifier.fillMaxWidth())

                    // Note input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.outline_edit_square_24),
                            contentDescription = "Note icon",
                        )
                        OutlinedTextField(
                            value = state.note,
                            onValueChange = {
                                viewModel.onUiStateChange(state.copy(note = it))
                            },
                            placeholder = { Text("Leave a note") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            }),
                        )
                    }

                    // Recurrent days of week
                    val dayOfWeekEntries = DayOfWeek.entries
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = state.repeatDays.isNotEmpty(), onCheckedChange = {
                            if (state.repeatDays.isNotEmpty()) viewModel.onUiStateChange(
                                state.copy(
                                    repeatDays = emptySet()
                                )
                            )
                            else viewModel.onUiStateChange(state.copy(repeatDays = dayOfWeekEntries.toSet()))
                        })
                        Text("Repeat alarm")
                    }
                    Row(
                        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dayOfWeekEntries.forEach { day ->
                            DayCircle(
                                text = day.abbreviation,
                                isSelected = state.repeatDays.contains(day),
                                onClick = {
                                    viewModel.onRepeatDaysChange(day)
                                },
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Snooze time select
                    SnoozeSelector(selectedOption = state.snoozeOption,
                        onOptionSelected = { viewModel.onUiStateChange(state.copy(snoozeOption = it)) })

                    // Save button
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = { viewModel.saveAlarm { onNavigateUp(it) } },
                            enabled = !isBusyPersisting
                        ) {
                            Text("Save")
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                }

                // Overlay when persisting
                if (isBusyPersisting) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(indication = null, // disable ripple effect
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { }), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SnoozeSelector(
    selectedOption: SnoozeOption,
    onOptionSelected: (SnoozeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.outline_snooze_24),
            contentDescription = "Snooze icon",
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(value = selectedOption.displayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SnoozeOption.options.forEach { option ->
                    DropdownMenuItem(text = { Text(option.displayText) }, onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }, trailingIcon = {
                        if (option == selectedOption) Icon(
                            Icons.Filled.Check, null
                        )
                    }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DialExample(
    timePickerState: TimePickerState, modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        TimePicker(
            state = timePickerState,
        )
    }
}