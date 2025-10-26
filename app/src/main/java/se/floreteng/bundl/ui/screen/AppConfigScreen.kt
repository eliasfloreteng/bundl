package se.floreteng.bundl.ui.screen

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigScreen(
    viewModel: BundlViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExemptions: (String) -> Unit
) {
    val appConfigs by viewModel.appConfigs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Managed Apps") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        if (appConfigs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No apps configured",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appConfigs) { app ->
                    AppConfigCard(
                        app = app,
                        viewModel = viewModel,
                        onNavigateToExemptions = onNavigateToExemptions
                    )
                }
            }
        }
    }
}

@Composable
fun AppConfigCard(
    app: AppConfig,
    viewModel: BundlViewModel,
    onNavigateToExemptions: (String) -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.appPackage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = app.isEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppConfig(app.copy(isEnabled = enabled))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { onNavigateToExemptions(app.appPackage) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configure Exemptions →")
            }
        }
    }
}
