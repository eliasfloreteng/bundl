package se.floreteng.bundl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import se.floreteng.bundl.utils.NotificationAccessUtil

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val bundlingEnabled by viewModel.isBundlingEnabled.collectAsState()
    val shouldShowPermissionDialog by viewModel.shouldShowPermissionDialog.collectAsState()
    val shouldShowNotificationPermissionDialog by viewModel.shouldShowNotificationPermissionDialog.collectAsState()

    // Permission launcher for notification permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, deliver notifications
            viewModel.deliverAllNotifications(context)
        }
        viewModel.onNotificationPermissionDialogDismissed()
    }

    // Monitor lifecycle to check permission state when returning from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndUpdateBundlingState(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Card {
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
                                    text = if (bundlingEnabled) "Active" else "Inactive",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (bundlingEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Switch(
                                checked = bundlingEnabled,
                                onCheckedChange = { viewModel.onBundlingToggled(it, context) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = { viewModel.onDeliverNotificationsClicked(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deliver All Notifications")
                }
            }
        }

        // Notification Listener Permission Dialog
        if (shouldShowPermissionDialog) {
            NotificationPermissionDialog(
                onConfirm = { viewModel.onPermissionDialogConfirmed(context) },
                onDismiss = { viewModel.onPermissionDialogDismissed() }
            )
        }

        // Notification Posting Permission Dialog
        if (shouldShowNotificationPermissionDialog) {
            val permission = NotificationAccessUtil.getNotificationPermission()
            if (permission != null) {
                NotificationPostPermissionDialog(
                    onConfirm = {
                        notificationPermissionLauncher.launch(permission)
                    },
                    onDismiss = {
                        viewModel.onNotificationPermissionDialogDismissed()
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "🔔",
                style = MaterialTheme.typography.headlineLarge
            )
        },
        title = {
            Text(
                text = "Notification Access Required",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "To enable notification bundling, Bundl needs permission to access your notifications.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You'll be redirected to Settings where you can enable notification access for Bundl.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NotificationPostPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "📬",
                style = MaterialTheme.typography.headlineLarge
            )
        },
        title = {
            Text(
                text = "Notification Permission Required",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "To deliver bundled notifications, Bundl needs permission to post notifications.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This allows the app to show you summary notifications for the apps you've bundled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}