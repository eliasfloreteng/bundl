package se.floreteng.bundl.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import se.floreteng.bundl.data.model.NotificationSchedule
import se.floreteng.bundl.viewmodel.BundlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    viewModel: BundlViewModel,
    onNavigateBack: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Schedules") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No schedules configured",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add a delivery time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(schedules) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        viewModel = viewModel
                    )
                }
            }
        }

        if (showAddDialog) {
            AddScheduleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { hour, minute ->
                    val daysOfWeek = Gson().toJson(listOf(1, 2, 3, 4, 5, 6, 7))
                    viewModel.addSchedule(
                        NotificationSchedule(
                            hour = hour,
                            minute = minute,
                            daysOfWeek = daysOfWeek,
                            isEnabled = true
                        )
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: NotificationSchedule,
    viewModel: BundlViewModel
) {
    val gson = Gson()
    val daysOfWeek: List<Int> = try {
        gson.fromJson(schedule.daysOfWeek, object : TypeToken<List<Int>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }

    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val activeDays = daysOfWeek.map { dayNames[it % 7] }.joinToString(", ")

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d:%02d", schedule.hour, schedule.minute),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (activeDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeDays,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.updateSchedule(schedule.copy(isEnabled = enabled))
                    }
                )
                IconButton(
                    onClick = { viewModel.deleteSchedule(schedule) }
                ) {
                    Text("🗑\uFE0F")
                }
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Time") },
        text = {
            Column {
                Text("Select a time for notification delivery:")
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour selector
                    OutlinedTextField(
                        value = hour.toString(),
                        onValueChange = {
                            hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour
                        },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f)
                    )

                    Text(":", style = MaterialTheme.typography.headlineSmall)

                    // Minute selector
                    OutlinedTextField(
                        value = minute.toString().padStart(2, '0'),
                        onValueChange = {
                            minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute
                        },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute) }) {
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
