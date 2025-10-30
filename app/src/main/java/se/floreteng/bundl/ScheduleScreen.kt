package se.floreteng.bundl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import se.floreteng.bundl.schedule.Schedule
import se.floreteng.bundl.schedule.ScheduleEvent
import se.floreteng.bundl.schedule.ScheduleViewModel

@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Reschedule alarms when returning from background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.rescheduleAllAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(ScheduleEvent.ShowDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add schedule")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Delivery Schedule",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set times to automatically deliver bundled notifications",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.schedules.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No schedules yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add a delivery time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.schedules) { schedule ->
                        ScheduleItem(
                            schedule = schedule,
                            onToggle = { enabled ->
                                viewModel.onEvent(ScheduleEvent.ToggleSchedule(schedule, enabled))
                                viewModel.rescheduleAllAlarms(context)
                            },
                            onDelete = {
                                viewModel.onEvent(ScheduleEvent.DeleteSchedule(schedule))
                            }
                        )
                    }
                }
            }
        }

        if (state.isAddingSchedule) {
            AddScheduleDialog(
                onDismiss = { viewModel.onEvent(ScheduleEvent.HideDialog) },
                onConfirm = {
                    viewModel.onEvent(ScheduleEvent.SaveSchedule)
                    viewModel.rescheduleAllAlarms(context)
                },
                onHourChange = { viewModel.onEvent(ScheduleEvent.SetHour(it)) },
                onMinuteChange = { viewModel.onEvent(ScheduleEvent.SetMinute(it)) },
                initialHour = state.selectedHour,
                initialMinute = state.selectedMinute
            )
        }
    }
}

@Composable
private fun ScheduleItem(
    schedule: Schedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", schedule.hour, schedule.minute),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete schedule",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Delivery Time",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onHourChange(timePickerState.hour)
                    onMinuteChange(timePickerState.minute)
                    onConfirm()
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
