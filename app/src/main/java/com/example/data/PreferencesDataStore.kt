package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_preferences")

enum class LauncherTheme(val title: String, val description: String) {
    ELEGANT_DARK("Elegant Dark", "Refined obsidian with warm champagne gold accents"),
    MONOCHROME_OLED("Monochrome OLED", "Pitch black background with crisp white text"),
    E_INK_PAPER("E-Ink Paper", "Reflective warm paper with dark graphite ink"),
    AMBER_CRT("Amber CRT", "Retro cyberpunk terminal with warm amber glow"),
    RETRO_MATRIX("Retro Matrix", "Classic terminal phosphor green typography"),
    MINIMAL_SLATE("Minimal Slate", "Deep dark navy slate with subtle modern accents"),
    CYBERPUNK_CYAN("Cyberpunk Neon", "High-contrast dark with electric neon cyan")
}

enum class ClockFont(val displayName: String, val description: String) {
    RETRO_MONO("Retro Monospace", "Classic fixed-width hacker terminal"),
    CLEAN_SANS("Modern Sans", "Geometric clean contemporary sans-serif"),
    ELEGANT_SERIF("Classic Serif", "Refined literary editorial typography"),
    DIGITAL_MATRIX("Digital Matrix", "High-contrast retro dot matrix"),
    TERMINAL_VT100("Terminal VT100", "Vintage green-screen command console"),
    RETRO_TYPEWRITER("Typewriter", "Vintage mechanical typewriter serif"),
    CUSTOM_FILE("Custom TTF/OTF", "Loaded font file from device storage")
}

data class LauncherSettings(
    val theme: LauncherTheme = LauncherTheme.ELEGANT_DARK,
    val clockFont: ClockFont = ClockFont.RETRO_MONO,
    val customFontDisplayName: String = "",
    val applyFontToAllUI: Boolean = true,
    val is24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val wallpaperDim: Float = 0.85f,
    val isWallpaperEnabled: Boolean = false,
    val activeProfileId: Int = 1,
    val lockPrivateSpaceWithFocus: Boolean = true,
    val showBatteryInUtility: Boolean = true,
    val useBatteryWidget: Boolean = false,
    val searchEnginePackage: String = "",
    val swipeRightAction: String = "browser", // "browser", "utility", "phone", "camera", "search", "custom"
    val swipeLeftAction: String = "utility",   // "utility", "browser", "camera", "phone", "search", "custom"
    val swipeRightPackage: String = "",
    val swipeLeftPackage: String = ""
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("launcher_theme")
        val CLOCK_FONT = stringPreferencesKey("clock_font")
        val CUSTOM_FONT_NAME = stringPreferencesKey("custom_font_display_name")
        val APPLY_FONT_TO_ALL_UI = booleanPreferencesKey("apply_font_to_all_ui")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
        val IS_WALLPAPER_ENABLED = booleanPreferencesKey("is_wallpaper_enabled")
        val ACTIVE_PROFILE_ID = intPreferencesKey("active_profile_id")
        val LOCK_PRIVATE_SPACE_WITH_FOCUS = booleanPreferencesKey("lock_private_space_with_focus")
        val SHOW_BATTERY_IN_UTILITY = booleanPreferencesKey("show_battery_in_utility")
        val USE_BATTERY_WIDGET = booleanPreferencesKey("use_battery_widget")
        val SEARCH_ENGINE_PACKAGE = stringPreferencesKey("search_engine_package")
        val SWIPE_RIGHT_ACTION = stringPreferencesKey("swipe_right_action")
        val SWIPE_LEFT_ACTION = stringPreferencesKey("swipe_left_action")
        val SWIPE_RIGHT_PACKAGE = stringPreferencesKey("swipe_right_package")
        val SWIPE_LEFT_PACKAGE = stringPreferencesKey("swipe_left_package")
    }

    val settingsFlow: Flow<LauncherSettings> = context.dataStore.data.map { preferences ->
        val themeName = preferences[Keys.THEME] ?: LauncherTheme.ELEGANT_DARK.name
        val fontName = preferences[Keys.CLOCK_FONT] ?: ClockFont.RETRO_MONO.name
        
        LauncherSettings(
            theme = try { LauncherTheme.valueOf(themeName) } catch (e: Exception) { LauncherTheme.ELEGANT_DARK },
            clockFont = try { ClockFont.valueOf(fontName) } catch (e: Exception) { ClockFont.RETRO_MONO },
            customFontDisplayName = preferences[Keys.CUSTOM_FONT_NAME] ?: "",
            applyFontToAllUI = preferences[Keys.APPLY_FONT_TO_ALL_UI] ?: true,
            is24Hour = preferences[Keys.IS_24_HOUR] ?: false,
            showSeconds = preferences[Keys.SHOW_SECONDS] ?: false,
            showDate = preferences[Keys.SHOW_DATE] ?: true,
            wallpaperDim = preferences[Keys.WALLPAPER_DIM] ?: 0.85f,
            isWallpaperEnabled = preferences[Keys.IS_WALLPAPER_ENABLED] ?: false,
            activeProfileId = preferences[Keys.ACTIVE_PROFILE_ID] ?: 1,
            lockPrivateSpaceWithFocus = preferences[Keys.LOCK_PRIVATE_SPACE_WITH_FOCUS] ?: true,
            showBatteryInUtility = preferences[Keys.SHOW_BATTERY_IN_UTILITY] ?: true,
            useBatteryWidget = preferences[Keys.USE_BATTERY_WIDGET] ?: false,
            searchEnginePackage = preferences[Keys.SEARCH_ENGINE_PACKAGE] ?: "",
            swipeRightAction = preferences[Keys.SWIPE_RIGHT_ACTION] ?: "browser",
            swipeLeftAction = preferences[Keys.SWIPE_LEFT_ACTION] ?: "utility",
            swipeRightPackage = preferences[Keys.SWIPE_RIGHT_PACKAGE] ?: "",
            swipeLeftPackage = preferences[Keys.SWIPE_LEFT_PACKAGE] ?: ""
        )
    }

    suspend fun updateTheme(theme: LauncherTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun updateClockFont(font: ClockFont) {
        context.dataStore.edit { it[Keys.CLOCK_FONT] = font.name }
    }

    suspend fun updateCustomFontDisplayName(name: String) {
        context.dataStore.edit { it[Keys.CUSTOM_FONT_NAME] = name }
    }

    suspend fun updateApplyFontToAllUI(applyToAll: Boolean) {
        context.dataStore.edit { it[Keys.APPLY_FONT_TO_ALL_UI] = applyToAll }
    }

    suspend fun update24Hour(is24Hour: Boolean) {
        context.dataStore.edit { it[Keys.IS_24_HOUR] = is24Hour }
    }

    suspend fun updateShowSeconds(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_SECONDS] = show }
    }

    suspend fun updateShowDate(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_DATE] = show }
    }

    suspend fun updateWallpaperDim(dim: Float) {
        context.dataStore.edit { it[Keys.WALLPAPER_DIM] = dim }
    }

    suspend fun updateWallpaperEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_WALLPAPER_ENABLED] = enabled }
    }

    suspend fun updateActiveProfileId(profileId: Int) {
        context.dataStore.edit { it[Keys.ACTIVE_PROFILE_ID] = profileId }
    }

    suspend fun updateLockPrivateSpaceWithFocus(lock: Boolean) {
        context.dataStore.edit { it[Keys.LOCK_PRIVATE_SPACE_WITH_FOCUS] = lock }
    }

    suspend fun updateUseBatteryWidget(useWidget: Boolean) {
        context.dataStore.edit { it[Keys.USE_BATTERY_WIDGET] = useWidget }
    }

    suspend fun updateSearchEnginePackage(pkg: String) {
        context.dataStore.edit { it[Keys.SEARCH_ENGINE_PACKAGE] = pkg }
    }

    suspend fun updateSwipeRightAction(action: String) {
        context.dataStore.edit { it[Keys.SWIPE_RIGHT_ACTION] = action }
    }

    suspend fun updateSwipeLeftAction(action: String) {
        context.dataStore.edit { it[Keys.SWIPE_LEFT_ACTION] = action }
    }

    suspend fun updateSwipeRightPackage(pkg: String) {
        context.dataStore.edit { it[Keys.SWIPE_RIGHT_PACKAGE] = pkg }
    }

    suspend fun updateSwipeLeftPackage(pkg: String) {
        context.dataStore.edit { it[Keys.SWIPE_LEFT_PACKAGE] = pkg }
    }
}
