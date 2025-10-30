package se.floreteng.bundl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import se.floreteng.bundl.apprule.AppRuleRepository
import se.floreteng.bundl.apprule.AppRuleViewModel
import se.floreteng.bundl.notifications.NotificationRepository
import se.floreteng.bundl.notifications.NotificationViewModel
import se.floreteng.bundl.ui.theme.BundlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundlTheme {
                BundlApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun BundlApp() {
    val context = LocalContext.current
    val database = Room.databaseBuilder(
        context,
        BundlDatabase::class.java,
        "bundl_database"
    )
        .allowMainThreadQueries()
        // TODO("Remove before building production app")
        .fallbackToDestructiveMigration(true)
        .build()

    val appRuleRepository = AppRuleRepository(database.appRuleDao)
    val notificationRepository = NotificationRepository(database.notificationDao)

    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(AppRuleViewModel::class.java) -> {
                    AppRuleViewModel(appRuleRepository) as T
                }
                modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                    NotificationViewModel(notificationRepository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> HomeScreen()
            AppDestinations.HISTORY -> HistoryScreen(
                viewModel = viewModel(factory = viewModelFactory)
            )
            AppDestinations.SETTINGS -> SettingsScreen(
                viewModel = viewModel(factory = viewModelFactory)
            )
        }
    }
}


enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.AutoMirrored.Default.List),
    SETTINGS("Settings", Icons.Default.Settings),
}
