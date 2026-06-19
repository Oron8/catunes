package com.pepe.musicplayer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")
private val ACCENT_KEY = stringPreferencesKey("accent_color")
private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")   // "SYSTEM" | "DARK" | "LIGHT"
private val FONT_SCALE_KEY = floatPreferencesKey("font_scale")  // 0.85f | 1.0f | 1.15f | 1.3f
private val STABLE_VOLUME_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("stable_volume")

enum class AccentColor { PURPLE, GREEN, ORANGE, BLUE, RED }
enum class DarkModeOption { SYSTEM, DARK, LIGHT }

class ThemeRepository(private val context: Context) {

    val stableVolume: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[STABLE_VOLUME_KEY] ?: false
    }

    val accentColor: Flow<AccentColor> = context.themeDataStore.data.map { prefs ->
        val name = prefs[ACCENT_KEY] ?: AccentColor.PURPLE.name
        runCatching { AccentColor.valueOf(name) }.getOrDefault(AccentColor.PURPLE)
    }

    val darkMode: Flow<DarkModeOption> = context.themeDataStore.data.map { prefs ->
        val name = prefs[DARK_MODE_KEY] ?: DarkModeOption.SYSTEM.name
        runCatching { DarkModeOption.valueOf(name) }.getOrDefault(DarkModeOption.SYSTEM)
    }

    /** Multiplicador sobre MaterialTheme.typography. 1.0f = normal. */
    val fontScale: Flow<Float> = context.themeDataStore.data.map { prefs ->
        prefs[FONT_SCALE_KEY] ?: 1.0f
    }

    suspend fun setAccentColor(color: AccentColor) {
        context.themeDataStore.edit { it[ACCENT_KEY] = color.name }
    }

    suspend fun setDarkMode(option: DarkModeOption) {
        context.themeDataStore.edit { it[DARK_MODE_KEY] = option.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.themeDataStore.edit { it[FONT_SCALE_KEY] = scale }
    }

    suspend fun setStableVolume(enabled: Boolean) {
        context.themeDataStore.edit { it[STABLE_VOLUME_KEY] = enabled }
    }
}
