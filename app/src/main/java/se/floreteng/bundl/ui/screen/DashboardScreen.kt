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
import se.floreteng.bundl.data.model.AppConfig
import se.floreteng.bundl.viewmodel.BundlViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BundlViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isBundlingEnabled by viewModel.isBundlingEnabled.collectAsState()
    val pendingNotifications by viewModel.pendingNotifications.collectAsState()
    val appConfigs by viewModel.appConfigs.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bundl") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙\uFE0F")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bundling Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bundling",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBundlingEnabled) "Active" else "Inactive",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isBundlingEnabled,
                                onCheckedChange = { viewModel.toggleBundling() }
                            )
                        }
                    }
                }
            }

            // Pending Notifications Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToHistory
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Pending Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${pendingNotifications.size} notifications waiting",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // App-wise breakdown
            item {
                Text(
                    text = "By App",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(appConfigs.filter { it.isEnabled }) { app ->
                AppNotificationCard(
                    app = app,
                    viewModel = viewModel
                )
            }

            // Next delivery time
            item {
                if (schedules.isNotEmpty()) {
                    val nextSchedule = schedules
                        .filter { it.isEnabled }
                        .minByOrNull { it.hour * 60 + it.minute }

                    nextSchedule?.let { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Next Delivery",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = String.format("%02d:%02d", schedule.hour, schedule.minute),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
            }

            // Deliver Now button
            item {
                Button(
                    onClick = { viewModel.deliverNow() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pendingNotifications.isNotEmpty()
                ) {
                    Text("Deliver All Now")
                }
            }
        }
    }
}

@Composable
fun AppNotificationCard(
    app: AppConfig,
    viewModel: BundlViewModel
) {
    val count by viewModel.getPendingCountForApp(app.appPackage).collectAsState(initial = 0)

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
            Column {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$count pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (count > 0) {
                Badge {
                    Text(count.toString())
                }
            }
        }
    }
}
