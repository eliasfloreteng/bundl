package se.floreteng.bundl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import se.floreteng.bundl.ui.navigation.Screen
import se.floreteng.bundl.ui.screen.*
import se.floreteng.bundl.ui.theme.BundlTheme
import se.floreteng.bundl.util.ScheduleManager
import se.floreteng.bundl.viewmodel.BundlViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BundlViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule notifications
            scheduleNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleNotifications()
            }
        } else {
            scheduleNotifications()
        }

        setContent {
            BundlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BundlApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun scheduleNotifications() {
        lifecycleScope.launch {
            val scheduleManager = ScheduleManager(applicationContext)
            scheduleManager.scheduleAll()
        }
    }
}

@Composable
fun BundlApp(
    viewModel: BundlViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSchedules = { navController.navigate(Screen.Schedules.route) },
                onNavigateToAppConfig = { navController.navigate(Screen.AppConfig.route) }
            )
        }

        composable(Screen.AppConfig.route) {
            AppConfigScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExemptions = { appPackage ->
                    navController.navigate(Screen.Exemptions.createRoute(appPackage))
                }
            )
        }

        composable(Screen.Schedules.route) {
            SchedulesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Exemptions.route,
            arguments = listOf(navArgument("appPackage") { type = NavType.StringType })
        ) { backStackEntry ->
            val appPackage = backStackEntry.arguments?.getString("appPackage") ?: ""
            ExemptionsScreen(
                appPackage = appPackage,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}