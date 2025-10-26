package se.floreteng.bundl

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.floreteng.bundl.data.BundlDatabase
import se.floreteng.bundl.data.model.AppConfig
import se.floreteng.bundl.util.PreferenceManager

class BundlApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDefaultData()
    }

    private fun initializeDefaultData() {
        val preferenceManager = PreferenceManager(this)

        if (preferenceManager.isFirstLaunch()) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = BundlDatabase.getDatabase(applicationContext)

                // Add default supported apps
                val defaultApps = listOf(
                    AppConfig("com.instagram.android", "Instagram", true),
                    AppConfig("com.snapchat.android", "Snapchat", true),
                    AppConfig("com.zhiliaoapp.musically", "TikTok", true)
                )

                database.appConfigDao().insertAll(defaultApps)
                preferenceManager.setFirstLaunch(false)
            }
        }
    }
}
