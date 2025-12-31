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
        val IS_STUDY_NOTIFICATION_ENABLED = booleanPreferencesKey("is_study_notification_enabled")
        val IS_BREAK_NOTIFICATION_ENABLED = booleanPreferencesKey("is_break_notification_enabled")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val IS_HARDCORE_MODE_ENABLED = booleanPreferencesKey("is_hardcore_mode_enabled")
        
        // Shop Ad Limits
        val LAST_AD_VIEW_DATE = stringPreferencesKey("last_ad_view_date_iso") // YYYY-MM-DD
        val DAILY_AD_VIEW_COUNT = intPreferencesKey("daily_ad_view_count")
    }

    val bgmVolume: Flow<Float> = dataStore.data.map { it[BGM_VOLUME] ?: 0.5f }
    val sfxVolume: Flow<Float> = dataStore.data.map { it[SFX_VOLUME] ?: 0.5f }
    val isVibrationEnabled: Flow<Boolean> = dataStore.data.map { it[IS_VIBRATION_ENABLED] ?: true }
    val isScreenOnEnabled: Flow<Boolean> = dataStore.data.map { it[IS_SCREEN_ON_ENABLED] ?: true }
    val defaultTimerMinutes: Flow<Int> = dataStore.data.map { it[DEFAULT_TIMER_MINUTES] ?: 25 }
    val defaultTag: Flow<String> = dataStore.data.map {
        it[DEFAULT_TAG] ?: if (android.content.res.Resources.getSystem().configuration.locales[0].language == "ko") "#공부" else "#study"
    }
    val isBreakEnabled: Flow<Boolean> = dataStore.data.map { it[IS_BREAK_ENABLED] ?: true }
    val breakDurationMinutes: Flow<Int> = dataStore.data.map { it[BREAK_DURATION_MINUTES] ?: 5 }
    val isBreakTimerEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_BREAK_TIMER_ENABLED] ?: true }
    val isNotificationEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_NOTIFICATION_ENABLED] ?: true }
    val isStudyNotificationEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_STUDY_NOTIFICATION_ENABLED] ?: true }
    val isBreakNotificationEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_BREAK_NOTIFICATION_ENABLED] ?: true }
    val appLanguage: Flow<String> = dataStore.data.map {
        it[APP_LANGUAGE] ?: if (android.content.res.Resources.getSystem().configuration.locales[0].language == "ko") "ko" else "en"
    }
    val isHardcoreModeEnabled: Flow<Boolean> =
        dataStore.data.map { it[IS_HARDCORE_MODE_ENABLED] ?: false }

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

    suspend fun setStudyNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_STUDY_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setBreakNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_BREAK_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setAppLanguage(language: String) {
        dataStore.edit { it[APP_LANGUAGE] = language }
    }

    suspend fun setHardcoreModeEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_HARDCORE_MODE_ENABLED] = enabled }
    }

    val lastAdViewDate: Flow<String?> = dataStore.data.map { it[LAST_AD_VIEW_DATE] }
    val dailyAdViewCount: Flow<Int> = dataStore.data.map { it[DAILY_AD_VIEW_COUNT] ?: 0 }

    suspend fun updateAdViewCount(date: String, count: Int) {
        dataStore.edit { 
            it[LAST_AD_VIEW_DATE] = date
            it[DAILY_AD_VIEW_COUNT] = count
        }
    }

    suspend fun clearData() {
        dataStore.edit { it.clear() }
    }
}
