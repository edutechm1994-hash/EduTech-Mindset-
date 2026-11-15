package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.eduTechDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "edutech_mindset_prefs"
)

enum class ThemeMode(val labelAr: String) {
    SYSTEM("النظام"),
    LIGHT("نهاري"),
    DARK("ليلي"),
}

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val isHighMotionEnabled: Boolean = true,
    val hasCompletedWelcome: Boolean = false,
    val founderAvatarUri: String = "",
) {
    fun resolveDarkTheme(isSystemDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val HIGH_MOTION = booleanPreferencesKey("high_motion_enabled")
        val COMPLETED_WELCOME = booleanPreferencesKey("completed_welcome")
        val FOUNDER_AVATAR_URI = stringPreferencesKey("founder_avatar_uri")
    }

    val preferencesFlow: Flow<AppPreferences> = context.eduTechDataStore.data.map { prefs ->
        val rawMode = prefs[Keys.THEME_MODE]
        val resolvedMode = when {
            rawMode != null -> runCatching { ThemeMode.valueOf(rawMode) }.getOrDefault(ThemeMode.SYSTEM)
            prefs[Keys.DARK_THEME] == true -> ThemeMode.DARK
            prefs[Keys.DARK_THEME] == false -> ThemeMode.LIGHT
            else -> ThemeMode.SYSTEM
        }
        AppPreferences(
            themeMode = resolvedMode,
            dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: true,
            isDarkTheme = resolvedMode == ThemeMode.DARK,
            isHighMotionEnabled = prefs[Keys.HIGH_MOTION] ?: true,
            hasCompletedWelcome = prefs[Keys.COMPLETED_WELCOME] ?: false,
            founderAvatarUri = prefs[Keys.FOUNDER_AVATAR_URI] ?: "",
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.eduTechDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
            when (mode) {
                ThemeMode.DARK -> prefs[Keys.DARK_THEME] = true
                ThemeMode.LIGHT -> prefs[Keys.DARK_THEME] = false
                ThemeMode.SYSTEM -> prefs.remove(Keys.DARK_THEME)
            }
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.eduTechDataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setHighMotion(enabled: Boolean) {
        context.eduTechDataStore.edit { it[Keys.HIGH_MOTION] = enabled }
    }

    suspend fun setCompletedWelcome(completed: Boolean) {
        context.eduTechDataStore.edit { it[Keys.COMPLETED_WELCOME] = completed }
    }

    suspend fun setFounderAvatarUri(uri: String) {
        context.eduTechDataStore.edit { it[Keys.FOUNDER_AVATAR_URI] = uri }
    }
}
