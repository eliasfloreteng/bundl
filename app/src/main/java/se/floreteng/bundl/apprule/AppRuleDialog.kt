package se.floreteng.bundl.apprule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import se.floreteng.bundl.utils.AppInfoUtil

@Composable
fun AppRuleDialog(
    state: AppRuleState,
    onEvent: (AppRuleEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAppPicker by remember { mutableStateOf(false) }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { appInfo ->
                onEvent(AppRuleEvent.SetPackageName(appInfo.packageName))
                showAppPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = { onEvent(AppRuleEvent.HideDialog) },
        title = {
            Text(
                text = if (state.editingAppRule != null) "Edit App Rule" else "Add App Rule"
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Package Name Input with App Picker Button
                OutlinedTextField(
                    value = state.packageName,
                    onValueChange = { onEvent(AppRuleEvent.SetPackageName(it)) },
                    label = { Text("Package Name") },
                    placeholder = { Text("com.example.app") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showAppPicker = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pick an app")
                        }
                    },
                    supportingText = if (state.packageName.isNotBlank()) {
                        {
                            Text(
                                text = AppInfoUtil.getAppName(context, state.packageName),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null
                )

                // Mode Selection
                Column(modifier = Modifier.selectableGroup()) {
                    Text(
                        text = "Mode",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AppRuleMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (state.mode == mode),
                                    onClick = { onEvent(AppRuleEvent.SetMode(mode)) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (state.mode == mode),
                                onClick = null
                            )
                            Text(
                                text = mode.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Filter String Input (Optional)
                OutlinedTextField(
                    value = state.filterString,
                    onValueChange = { onEvent(AppRuleEvent.SetFilterString(it)) },
                    label = { Text("Filter String (Optional)") },
                    placeholder = { Text("Filter text") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("Leave empty to apply rule to all notifications")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(AppRuleEvent.SaveAppRule) },
                enabled = state.packageName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(AppRuleEvent.HideDialog) }) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun AppPickerDialog(
    onDismiss: () -> Unit,
    onAppSelected: (se.floreteng.bundl.utils.AppInfo) -> Unit
) {
    val context = LocalContext.current
    val installedApps = remember { AppInfoUtil.getInstalledApps(context, includeSystemApps = false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select App")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    placeholder = { Text("App name or package") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // App list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(filteredApps) { appInfo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(appInfo) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = appInfo.icon,
                                contentDescription = appInfo.appName,
                                modifier = Modifier.size(40.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = appInfo.appName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = appInfo.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
