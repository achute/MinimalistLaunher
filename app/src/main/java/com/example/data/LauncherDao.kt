package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.AppLimitRule
import com.example.model.BottomSlot
import com.example.model.CustomAppLabel
import com.example.model.FocusProfile
import com.example.model.HiddenApp
import com.example.model.TaskItem
import com.example.model.WidgetSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    // Focus Profiles
    @Query("SELECT * FROM focus_profiles ORDER BY id ASC")
    fun getAllFocusProfiles(): Flow<List<FocusProfile>>

    @Query("SELECT * FROM focus_profiles ORDER BY id ASC")
    suspend fun getAllFocusProfilesDirect(): List<FocusProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusProfile(profile: FocusProfile): Long

    @Update
    suspend fun updateFocusProfile(profile: FocusProfile)

    @Query("DELETE FROM focus_profiles WHERE id = :id")
    suspend fun deleteFocusProfile(id: Int)

    // App Limit Rules
    @Query("SELECT * FROM app_limit_rules")
    fun getAllLimitRules(): Flow<List<AppLimitRule>>

    @Query("SELECT * FROM app_limit_rules WHERE packageName = :packageName LIMIT 1")
    fun getLimitRule(packageName: String): Flow<AppLimitRule?>

    @Query("SELECT * FROM app_limit_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getLimitRuleDirect(packageName: String): AppLimitRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitRule(rule: AppLimitRule)

    @Query("DELETE FROM app_limit_rules WHERE packageName = :packageName")
    suspend fun deleteLimitRule(packageName: String)

    // Task Items (To-Do list)
    @Query("SELECT * FROM task_items ORDER BY isDone ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_items")
    suspend fun getAllTasksDirect(): List<TaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Query("DELETE FROM task_items WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM task_items WHERE isDone = 1")
    suspend fun clearCompletedTasks()

    // Hidden Apps (Private Space)
    @Query("SELECT * FROM hidden_apps")
    fun getAllHiddenApps(): Flow<List<HiddenApp>>

    @Query("SELECT * FROM hidden_apps")
    suspend fun getAllHiddenAppsDirect(): List<HiddenApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiddenApp(hiddenApp: HiddenApp)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun deleteHiddenApp(packageName: String)

    // Custom App Labels
    @Query("SELECT * FROM custom_app_labels")
    fun getAllCustomLabels(): Flow<List<CustomAppLabel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLabel(customLabel: CustomAppLabel)

    @Query("DELETE FROM custom_app_labels WHERE packageName = :packageName")
    suspend fun deleteCustomLabel(packageName: String)

    // Bottom Action Bar Slots
    @Query("SELECT * FROM bottom_slots ORDER BY slotIndex ASC")
    fun getAllBottomSlots(): Flow<List<BottomSlot>>

    @Query("SELECT * FROM bottom_slots ORDER BY slotIndex ASC")
    suspend fun getAllBottomSlotsDirect(): List<BottomSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBottomSlot(slot: BottomSlot)

    // Widget Slots
    @Query("SELECT * FROM widget_slots")
    fun getAllWidgetSlots(): Flow<List<WidgetSlot>>

    @Query("SELECT * FROM widget_slots")
    suspend fun getAllWidgetSlotsDirect(): List<WidgetSlot>

    @Query("SELECT * FROM widget_slots WHERE slotKey = :slotKey LIMIT 1")
    fun getWidgetSlot(slotKey: String): Flow<WidgetSlot?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetSlot(slot: WidgetSlot)

    @Query("DELETE FROM widget_slots WHERE slotKey = :slotKey")
    suspend fun deleteWidgetSlot(slotKey: String)
}
