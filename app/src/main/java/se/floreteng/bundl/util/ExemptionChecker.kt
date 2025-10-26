package se.floreteng.bundl.util

import android.app.Notification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import se.floreteng.bundl.data.BundlDatabase

class ExemptionChecker(private val database: BundlDatabase) {

    private val gson = Gson()

    fun isExempt(
        packageName: String,
        category: String?,
        title: String?,
        text: String?
    ): Boolean {
        return runBlocking {
            val rules = database.exemptionRuleDao().getEnabledRulesForApp(packageName)

            for (rule in rules) {
                if (matchesRule(rule.ruleType, rule.categoryFilter, rule.keywords, category, title, text)) {
                    return@runBlocking true
                }
            }
            false
        }
    }

    private fun matchesRule(
        ruleType: String,
        categoryFilter: String?,
        keywordsJson: String?,
        category: String?,
        title: String?,
        text: String?
    ): Boolean {
        // Check category filter
        if (!categoryFilter.isNullOrEmpty() && category != null) {
            when (ruleType) {
                "MESSAGE" -> {
                    if (category == Notification.CATEGORY_MESSAGE) {
                        return true
                    }
                }
                "CALL" -> {
                    if (category == Notification.CATEGORY_CALL) {
                        return true
                    }
                }
            }
        }

        // Check keywords
        if (!keywordsJson.isNullOrEmpty()) {
            try {
                val keywords: List<String> = gson.fromJson(
                    keywordsJson,
                    object : TypeToken<List<String>>() {}.type
                )

                val combinedText = "${title ?: ""} ${text ?: ""}".lowercase()

                for (keyword in keywords) {
                    if (combinedText.contains(keyword.lowercase())) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // Invalid JSON, ignore
            }
        }

        return false
    }
}
