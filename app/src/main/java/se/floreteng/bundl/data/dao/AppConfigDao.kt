package se.floreteng.bundl.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.floreteng.bundl.data.model.AppConfig

@Dao
interface AppConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appConfig: AppConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appConfigs: List<AppConfig>)

    @Update
    suspend fun update(appConfig: AppConfig)

    @Delete
    suspend fun delete(appConfig: AppConfig)

    @Query("SELECT * FROM app_config ORDER BY appName")
    fun getAllAppConfigs(): Flow<List<AppConfig>>

    @Query("SELECT * FROM app_config WHERE isEnabled = 1")
    fun getEnabledApps(): Flow<List<AppConfig>>

    @Query("SELECT * FROM app_config WHERE appPackage = :packageName")
    suspend fun getAppConfig(packageName: String): AppConfig?

    @Query("SELECT isEnabled FROM app_config WHERE appPackage = :packageName")
    suspend fun isAppEnabled(packageName: String): Boolean?
}
