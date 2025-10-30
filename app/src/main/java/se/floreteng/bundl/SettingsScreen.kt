package se.floreteng.bundl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.floreteng.bundl.apprule.AddAppRuleDialog
import se.floreteng.bundl.apprule.AppRuleEvent
import se.floreteng.bundl.apprule.AppRuleState


@Composable
fun SettingsScreen(
    state: AppRuleState,
    onEvent: (AppRuleEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onEvent(AppRuleEvent.ShowDialog)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add setting")
            }
        }) { padding ->
        if (state.isAddingAppRule) {
            AddAppRuleDialog(state = state, onEvent = onEvent)
        }

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.appRules) { appRule ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column {
                        Text(text = appRule.packageName, fontSize = 20.sp)
                        Text(text = appRule.mode.name)
                        Text(text = appRule.filterString ?: "No filter")
                        IconButton(
                            onClick = {
                                onEvent(AppRuleEvent.DeleteAppRule(appRule))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        }
    }
}