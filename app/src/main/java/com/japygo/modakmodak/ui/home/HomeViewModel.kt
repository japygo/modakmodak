package com.japygo.modakmodak.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.data.entity.TimerPreset
import com.japygo.modakmodak.data.entity.User
import com.japygo.modakmodak.data.repository.ModakRepository
import com.japygo.modakmodak.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ModakRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val user: StateFlow<User?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timerPresets: StateFlow<List<TimerPreset>> = repository.timerPresetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 이번 세션에만 적용할 독립적인 상태 (DB와 별개)
    private val _sessionTag = MutableStateFlow(
        if (java.util.Locale.getDefault().language == "ko") "#공부" else "#study",
    )
    val sessionTag = _sessionTag.asStateFlow()

    private val _sessionDurationMinutes = MutableStateFlow(25)
    val sessionDurationMinutes = _sessionDurationMinutes.asStateFlow()

    private var hasInitialized = false

    init {
        // 마지막으로 사용했던 설정을 불러와 세션 초기값으로 설정
        viewModelScope.launch {
            _sessionTag.value = settingsRepository.defaultTag.first()
            _sessionDurationMinutes.value = settingsRepository.defaultTimerMinutes.first()
        }
    }

    // 프리셋 선택 (이번 세션에만 적용 및 저장)
    fun selectPresetForSession(preset: TimerPreset) {
        _sessionTag.value = preset.tag
        _sessionDurationMinutes.value = preset.durationMinutes
        saveLastUsed(preset.tag, preset.durationMinutes)
    }

    // 커스텀 수정 (이번 세션에만 적용 및 저장)
    fun updateSessionSettings(tag: String, minutes: Int) {
        _sessionTag.value = tag
        _sessionDurationMinutes.value = minutes
        saveLastUsed(tag, minutes)
    }

    private fun saveLastUsed(tag: String, minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultTag(tag)
            settingsRepository.setDefaultTimerMinutes(minutes)
        }
    }

}
