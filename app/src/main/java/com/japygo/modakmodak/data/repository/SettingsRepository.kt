package com.japygo.modakmodak.data.repository

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
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val BGM_VOLUME = floatPreferencesKey("bgm_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val IS_VIBRATION_ENABLED = booleanPreferencesKey("is_vibration_enabled")
        val IS_SCREEN_ON_ENABLED = booleanPreferencesKey("is_screen_on_enabled")
        val DEFAULT_TIMER_MINUTES = intPreferencesKey("default_timer_minutes")
        val DEFAULT_TAG = stringPreferencesKey("default_tag")
        val IS_BREAK_ENABLED = booleanPreferencesKey("is_break_enabled")
        val BREAK_DURATION_MINUTES = intPreferencesKey("break_duration_minutes")
        val IS_BREAK_TIMER_ENABLED = booleanPreferencesKey("is_break_timer_enabled")
        val IS_NOTIFICATION_ENABLED = booleanPreferencesKey("is_notification_enabled")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val bgmVolume: Flow<Float> = dataStore.data.map { it[BGM_VOLUME] ?: 0.5f }
    val sfxVolume: Flow<Float> = dataStore.data.map { it[SFX_VOLUME] ?: 0.5f }
    val isVibrationEnabled: Flow<Boolean> = dataStore.data.map { it[IS_VIBRATION_ENABLED] ?: true }
    val isScreenOnEnabled: Flow<Boolean> = dataStore.data.map { it[IS_SCREEN_ON_ENABLED] ?: false }
    val defaultTimerMinutes: Flow<Int> = dataStore.data.map { it[DEFAULT_TIMER_MINUTES] ?: 25 }
    val defaultTag: Flow<String> = dataStore.data.map {
        it[DEFAULT_TAG] ?: if (Locale.getDefault().language == "ko") "#공부" else "#study"
    }
    val isBreakEnabled: Flow<Boolean> = dataStore.data.map { it[IS_BREAK_ENABLED] ?: true }
    val breakDurationMinutes: Flow<Int> = dataStore.data.map { it[BREAK_DURATION_MINUTES] ?: 5 }
    val isBreakTimerEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_BREAK_TIMER_ENABLED] ?: true }
    val isNotificationEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_NOTIFICATION_ENABLED] ?: true }
    val appLanguage: Flow<String> = dataStore.data.map {
        it[APP_LANGUAGE] ?: if (Locale.getDefault().language == "ko") "ko" else "en"
    }

    suspend fun setBgmVolume(volume: Float) {
        dataStore.edit { it[BGM_VOLUME] = volume }
    }

    suspend fun setSfxVolume(volume: Float) {
        dataStore.edit { it[SFX_VOLUME] = volume }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setScreenOnEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_SCREEN_ON_ENABLED] = enabled }
    }

    suspend fun setDefaultTimerMinutes(minutes: Int) {
        dataStore.edit { it[DEFAULT_TIMER_MINUTES] = minutes }
    }

    suspend fun setDefaultTag(tag: String) {
        dataStore.edit { it[DEFAULT_TAG] = tag }
    }

    suspend fun setBreakEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_BREAK_ENABLED] = enabled }
    }

    suspend fun setBreakDurationMinutes(minutes: Int) {
        dataStore.edit { it[BREAK_DURATION_MINUTES] = minutes }
    }

    suspend fun setBreakTimerEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_BREAK_TIMER_ENABLED] = enabled }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setAppLanguage(language: String) {
        dataStore.edit { it[APP_LANGUAGE] = language }
    }
}
