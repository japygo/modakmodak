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
        // BGM_VOLUME and SFX_VOLUME are reserved for UI sounds or background music outside Focus Mode.
        val BGM_VOLUME = floatPreferencesKey("bgm_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        
        // ASMR Volumes
        val FIRE_VOLUME = floatPreferencesKey("fire_volume")
        val RAIN_VOLUME = floatPreferencesKey("rain_volume")
        val CRICKETS_VOLUME = floatPreferencesKey("crickets_volume")
        val WIND_VOLUME = floatPreferencesKey("wind_volume")
        val STREAM_VOLUME = floatPreferencesKey("stream_volume")
        
        // ASMR Variations (0 = Off, 1~4 = Variation)
        val FIRE_VARIATION = intPreferencesKey("fire_variation")
        val RAIN_VARIATION = intPreferencesKey("rain_variation")
        val CRICKETS_VARIATION = intPreferencesKey("crickets_variation")
        val WIND_VARIATION = intPreferencesKey("wind_variation")
        val STREAM_VARIATION = intPreferencesKey("stream_variation")
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
    
    val fireVolume: Flow<Float> = dataStore.data.map { it[FIRE_VOLUME] ?: 0f }
    val rainVolume: Flow<Float> = dataStore.data.map { it[RAIN_VOLUME] ?: 0f }
    val cricketsVolume: Flow<Float> = dataStore.data.map { it[CRICKETS_VOLUME] ?: 0f }
    val windVolume: Flow<Float> = dataStore.data.map { it[WIND_VOLUME] ?: 0f }
    val streamVolume: Flow<Float> = dataStore.data.map { it[STREAM_VOLUME] ?: 0f }

    val fireVariation: Flow<Int> = dataStore.data.map { it[FIRE_VARIATION] ?: 0 }
    val rainVariation: Flow<Int> = dataStore.data.map { it[RAIN_VARIATION] ?: 0 }
    val cricketsVariation: Flow<Int> = dataStore.data.map { it[CRICKETS_VARIATION] ?: 0 }
    val windVariation: Flow<Int> = dataStore.data.map { it[WIND_VARIATION] ?: 0 }
    val streamVariation: Flow<Int> = dataStore.data.map { it[STREAM_VARIATION] ?: 0 }
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

    suspend fun setFireVolume(volume: Float) { dataStore.edit { it[FIRE_VOLUME] = volume } }
    suspend fun setRainVolume(volume: Float) { dataStore.edit { it[RAIN_VOLUME] = volume } }
    suspend fun setCricketsVolume(volume: Float) { dataStore.edit { it[CRICKETS_VOLUME] = volume } }
    suspend fun setWindVolume(volume: Float) { dataStore.edit { it[WIND_VOLUME] = volume } }
    suspend fun setStreamVolume(volume: Float) { dataStore.edit { it[STREAM_VOLUME] = volume } }

    suspend fun setFireVariation(variation: Int) { dataStore.edit { it[FIRE_VARIATION] = variation } }
    suspend fun setRainVariation(variation: Int) { dataStore.edit { it[RAIN_VARIATION] = variation } }
    suspend fun setCricketsVariation(variation: Int) { dataStore.edit { it[CRICKETS_VARIATION] = variation } }
    suspend fun setWindVariation(variation: Int) { dataStore.edit { it[WIND_VARIATION] = variation } }
    suspend fun setStreamVariation(variation: Int) { dataStore.edit { it[STREAM_VARIATION] = variation } }

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
