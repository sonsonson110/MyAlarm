package com.pson.myalarm.ui.screens.alarm_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pson.myalarm.R
import com.pson.myalarm.data.model.DateOfWeek
import com.pson.myalarm.ui.screens.alarm_list.AlarmListUiState
import com.pson.myalarm.ui.shared.DayCircle
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmEditScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmEditViewModel = viewModel(factory = AlarmEditViewModel.Factory)
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit alarm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "navigate up button"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { TODO() }) {
                        Icon(
                            Icons.Outlined.Delete,
                            "Delete",
                            tint = Color.Red
                        )
                    }
                })
        }
    ) { paddingValues ->
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
                    Text((uiState.value as AlarmListUiState.Error).message)
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
                                timePickerState.hour,
                                timePickerState.minute
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
                                viewModel.onUiStateChange(state.copy(note = it.trim()))
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                        )
                    }

                    // Recurrent dates of week
                    val dateOfWeekEntries = DateOfWeek.entries
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = state.repeatDates.isNotEmpty(),
                            onCheckedChange = {
                                if (state.repeatDates.isNotEmpty())
                                    viewModel.onUiStateChange(state.copy(repeatDates = emptyList()))
                                else
                                    viewModel.onUiStateChange(state.copy(repeatDates = dateOfWeekEntries.toList()))
                            })
                        Text("Repeat alarm")
                    }
                    Row(
                        modifier = modifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateOfWeekEntries.forEach { day ->
                            DayCircle(
                                text = day.abbreviation,
                                isSelected = state.repeatDates.contains(day),
                                onClick = {
                                    val currentDates = state.repeatDates
                                    viewModel.onUiStateChange(state.copy(repeatDates = currentDates + day))
                                },
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Snooze time select
                    SnoozeSelector(
                        selectedOption = state.snoozeOption,
                        onOptionSelected = { viewModel.onUiStateChange(state.copy(snoozeOption = it)) })

                    // Save button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(onClick = { TODO() }) { Text("Save") }
                    }

                    Spacer(Modifier.height(28.dp))

                    Text(uiState.value.toString())
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
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
            OutlinedTextField(
                value = selectedOption.displayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SnoozeOption.options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayText) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        trailingIcon = {
                            if (option == selectedOption) Icon(
                                Icons.Filled.Check,
                                null
                            )
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DialExample(
    timePickerState: TimePickerState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TimePicker(
            state = timePickerState,
        )
    }
}