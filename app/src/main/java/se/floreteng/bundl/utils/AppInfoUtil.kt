package se.floreteng.bundl.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

object AppInfoUtil {

    /**
     * Get list of all installed apps (excluding system apps by default)
     * A system app is one that was pre-installed and hasn't been updated by the user
     */
    fun getInstalledApps(context: Context, includeSystemApps: Boolean = false): List<AppInfo> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return packages
            .filter { appInfo ->
                if (includeSystemApps) {
                    true
                } else {
                    // Include apps that are either:
                    // 1. Not system apps at all
                    // 2. System apps that have been updated (user-installed updates to system apps)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    !isSystemApp || isUpdatedSystemApp
                }
            }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = try {
                        packageManager.getApplicationIcon(appInfo.packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    /**
     * Get app name from package name
     */
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /**
     * Get app icon from package name
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
