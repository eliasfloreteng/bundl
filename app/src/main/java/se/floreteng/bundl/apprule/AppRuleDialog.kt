package se.floreteng.bundl.apprule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun AppRuleDialog(
    state: AppRuleState,
    onEvent: (AppRuleEvent) -> Unit,
    modifier: Modifier = Modifier
) {
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
                // Package Name Input
                OutlinedTextField(
                    value = state.packageName,
                    onValueChange = { onEvent(AppRuleEvent.SetPackageName(it)) },
                    label = { Text("Package Name") },
                    placeholder = { Text("com.example.app") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
