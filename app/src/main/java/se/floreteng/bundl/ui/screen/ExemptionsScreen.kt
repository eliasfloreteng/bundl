package se.floreteng.bundl.ui.screen

import android.app.Notification
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
import se.floreteng.bundl.data.model.ExemptionRule
import se.floreteng.bundl.viewmodel.BundlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExemptionsScreen(
    appPackage: String,
    viewModel: BundlViewModel,
    onNavigateBack: () -> Unit
) {
    val rules by viewModel.getExemptionRulesForApp(appPackage).collectAsState(initial = emptyList())
    val appConfigs by viewModel.appConfigs.collectAsState()
    val appName = appConfigs.find { it.appPackage == appPackage }?.appName ?: "App"

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$appName Exemptions") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Exemption rules allow certain notifications to bypass bundling and be delivered instantly.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No exemption rules",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add a rule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rules) { rule ->
                        ExemptionRuleCard(
                            rule = rule,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddExemptionDialog(
                appPackage = appPackage,
                onDismiss = { showAddDialog = false },
                onConfirm = { ruleType ->
                    val categoryFilter = when (ruleType) {
                        "MESSAGE" -> Notification.CATEGORY_MESSAGE
                        "CALL" -> Notification.CATEGORY_CALL
                        else -> null
                    }

                    viewModel.addExemptionRule(
                        ExemptionRule(
                            appPackage = appPackage,
                            ruleType = ruleType,
                            keywords = null,
                            categoryFilter = categoryFilter,
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
fun ExemptionRuleCard(
    rule: ExemptionRule,
    viewModel: BundlViewModel
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getRuleTypeDisplayName(rule.ruleType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getRuleDescription(rule),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.updateExemptionRule(rule.copy(isEnabled = enabled))
                    }
                )
                IconButton(
                    onClick = { viewModel.deleteExemptionRule(rule) }
                ) {
                    Text("🗑\uFE0F")
                }
            }
        }
    }
}

@Composable
fun AddExemptionDialog(
    appPackage: String,
    onDismiss: () -> Unit,
    onConfirm: (ruleType: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("MESSAGE") }

    val ruleTypes = when (appPackage) {
        "com.snapchat.android" -> listOf(
            "MESSAGE" to "Direct Messages",
            "MENTION" to "Mentions & Tags"
        )
        "com.instagram.android" -> listOf(
            "MESSAGE" to "Direct Messages",
            "MENTION" to "Mentions & Tags"
        )
        "com.zhiliaoapp.musically" -> listOf(
            "MESSAGE" to "Direct Messages",
            "MENTION" to "Mentions"
        )
        else -> listOf(
            "MESSAGE" to "Messages",
            "CALL" to "Calls"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exemption Rule") },
        text = {
            Column {
                Text("Select notification type to exempt:")
                Spacer(modifier = Modifier.height(16.dp))

                ruleTypes.forEach { (type, displayName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedType) }) {
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

fun getRuleTypeDisplayName(ruleType: String): String {
    return when (ruleType) {
        "MESSAGE" -> "Direct Messages"
        "CALL" -> "Calls"
        "MENTION" -> "Mentions & Tags"
        else -> ruleType
    }
}

fun getRuleDescription(rule: ExemptionRule): String {
    return when (rule.ruleType) {
        "MESSAGE" -> "Deliver message notifications instantly"
        "CALL" -> "Deliver call notifications instantly"
        "MENTION" -> "Deliver mentions and tags instantly"
        else -> "Custom exemption rule"
    }
}
