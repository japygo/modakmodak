package com.japygo.modakmodak.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.data.entity.TimerPreset
import com.japygo.modakmodak.data.repository.ModakRepository
import com.japygo.modakmodak.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val modakRepository: ModakRepository,
) : ViewModel() {

    val bgmVolume: StateFlow<Float> = settingsRepository.bgmVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val sfxVolume: StateFlow<Float> = settingsRepository.sfxVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val fireVolume: StateFlow<Float> = settingsRepository.fireVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
    val rainVolume: StateFlow<Float> = settingsRepository.rainVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
    val cricketsVolume: StateFlow<Float> = settingsRepository.cricketsVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
    val windVolume: StateFlow<Float> = settingsRepository.windVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
    val streamVolume: StateFlow<Float> = settingsRepository.streamVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val fireVariation: StateFlow<Int> = settingsRepository.fireVariation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val rainVariation: StateFlow<Int> = settingsRepository.rainVariation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val cricketsVariation: StateFlow<Int> = settingsRepository.cricketsVariation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val windVariation: StateFlow<Int> = settingsRepository.windVariation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val streamVariation: StateFlow<Int> = settingsRepository.streamVariation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isVibrationEnabled: StateFlow<Boolean> = settingsRepository.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isScreenOnEnabled: StateFlow<Boolean> = settingsRepository.isScreenOnEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isHardcoreModeEnabled: StateFlow<Boolean> = settingsRepository.isHardcoreModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val defaultTimerMinutes: StateFlow<Int> = settingsRepository.defaultTimerMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val defaultTag: StateFlow<String> = settingsRepository.defaultTag
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            if (android.content.res.Resources.getSystem().configuration.locales[0].language == "ko") "#공부" else "#study",
        )

    val isBreakEnabled: StateFlow<Boolean> = settingsRepository.isBreakEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val breakDurationMinutes: StateFlow<Int> = settingsRepository.breakDurationMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val isBreakTimerEnabled: StateFlow<Boolean> = settingsRepository.isBreakTimerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)



    val appLanguage: StateFlow<String> = settingsRepository.appLanguage
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            if (android.content.res.Resources.getSystem().configuration.locales[0].language == "ko") "ko" else "en",
        )

    // Selection logic for default preset
    val timerPresets: StateFlow<List<TimerPreset>> = modakRepository.timerPresetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateBgmVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.setBgmVolume(volume)
        }
    }

    fun updateSfxVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.setSfxVolume(volume)
        }
    }

    fun updateFireVolume(volume: Float) = viewModelScope.launch { settingsRepository.setFireVolume(volume) }
    fun updateRainVolume(volume: Float) = viewModelScope.launch { settingsRepository.setRainVolume(volume) }
    fun updateCricketsVolume(volume: Float) = viewModelScope.launch { settingsRepository.setCricketsVolume(volume) }
    fun updateWindVolume(volume: Float) = viewModelScope.launch { settingsRepository.setWindVolume(volume) }
    fun updateStreamVolume(volume: Float) = viewModelScope.launch { settingsRepository.setStreamVolume(volume) }

    fun updateFireVariation(variation: Int) = viewModelScope.launch { settingsRepository.setFireVariation(variation) }
    fun updateRainVariation(variation: Int) = viewModelScope.launch { settingsRepository.setRainVariation(variation) }
    fun updateCricketsVariation(variation: Int) = viewModelScope.launch { settingsRepository.setCricketsVariation(variation) }
    fun updateWindVariation(variation: Int) = viewModelScope.launch { settingsRepository.setWindVariation(variation) }
    fun updateStreamVariation(variation: Int) = viewModelScope.launch { settingsRepository.setStreamVariation(variation) }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }

    fun toggleScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScreenOnEnabled(enabled)
        }
    }

    fun toggleHardcoreMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHardcoreModeEnabled(enabled)
            if (enabled) {
                // Hardcore mode requires screen on to prevent accidental failures
                settingsRepository.setScreenOnEnabled(true)
            }
        }
    }

    fun updateDefaultTimer(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultTimerMinutes(minutes)
        }
    }

    fun updateDefaultTag(tag: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultTag(tag)
        }
    }

    fun toggleBreak(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBreakEnabled(enabled)
        }
    }

    fun updateBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setBreakDurationMinutes(minutes)
        }
    }

    fun toggleBreakTimer(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBreakTimerEnabled(enabled)
        }
    }

    val isNotificationEnabled: StateFlow<Boolean> = settingsRepository.isNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isStudyNotificationEnabled: StateFlow<Boolean> = settingsRepository.isStudyNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isBreakNotificationEnabled: StateFlow<Boolean> = settingsRepository.isBreakNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isDailyReminderEnabled: StateFlow<Boolean> = modakRepository.userFlow
        .map { it?.enableDailyReminder ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ... (rest of vals)

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(enabled)
        }
    }

    fun toggleStudyNotification(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStudyNotificationEnabled(enabled)
        }
    }

    fun toggleBreakNotification(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBreakNotificationEnabled(enabled)
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            modakRepository.setDailyReminder(enabled)
        }
    }

    fun updateAppLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(language)
        }
    }

    fun addPreset(tag: String, minutes: Int) {
        if (minutes <= 0) return // Validation
        viewModelScope.launch {
            modakRepository.addTimerPreset(TimerPreset(tag = tag, durationMinutes = minutes))
        }
    }

    fun updatePreset(preset: TimerPreset) {
        if (preset.durationMinutes <= 0) return // Validation
        viewModelScope.launch {
            modakRepository.updateTimerPreset(preset)
        }
    }

    fun deletePreset(preset: TimerPreset) {
        viewModelScope.launch {
            modakRepository.deleteTimerPreset(preset)
        }
    }

    fun resetData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            modakRepository.resetAllData()
            onComplete()
        }
    }
}
