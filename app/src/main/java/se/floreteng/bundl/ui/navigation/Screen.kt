package se.floreteng.bundl.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object Settings : Screen("settings")
    object AppConfig : Screen("app_config")
    object Schedules : Screen("schedules")
    object Exemptions : Screen("exemptions/{appPackage}") {
        fun createRoute(appPackage: String) = "exemptions/$appPackage"
    }
}
