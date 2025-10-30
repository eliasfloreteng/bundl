package se.floreteng.bundl

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import se.floreteng.bundl.preferences.PreferencesManager
import se.floreteng.bundl.schedule.ScheduleRepository
import se.floreteng.bundl.schedule.ScheduleViewModel
import se.floreteng.bundl.ui.theme.BundlTheme
import se.floreteng.bundl.utils.NotificationAccessUtil

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
    val scheduleRepository = ScheduleRepository(database.scheduleDao)
    val preferencesManager = PreferencesManager(context)

    // Request notification permission on first launch (Android 13+)
    var hasAskedForPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled, no action needed
        hasAskedForPermission = true
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationAccessUtil.hasNotificationPermission(context) && !hasAskedForPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                hasAskedForPermission = true
            }
        }
    }

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
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                    HomeViewModel(preferencesManager, notificationRepository) as T
                }
                modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                    ScheduleViewModel(scheduleRepository) as T
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
            AppDestinations.HOME -> HomeScreen(
                viewModel = viewModel(factory = viewModelFactory)
            )
            AppDestinations.SCHEDULE -> ScheduleScreen(
                viewModel = viewModel(factory = viewModelFactory)
            )
            AppDestinations.HISTORY -> HistoryScreen(
                viewModel = viewModel(factory = viewModelFactory)
            )
            AppDestinations.RULES -> SettingsScreen(
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
    SCHEDULE("Schedule", Icons.Default.DateRange),
    HISTORY("History", Icons.AutoMirrored.Default.List),
    RULES("Rules", Icons.Default.CheckCircle),
}
