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

    private val _selectedPreset = MutableStateFlow<TimerPreset?>(null)
    val selectedPreset = _selectedPreset.asStateFlow()

    private var hasInitialized = false

    init {
        // 마지막으로 사용했던 설정을 불러와 세션 초기값으로 설정
        viewModelScope.launch {
            val tag = settingsRepository.defaultTag.first()
            val minutes = settingsRepository.defaultTimerMinutes.first()
            _sessionTag.value = tag
            _sessionDurationMinutes.value = minutes

            // 현재 설정과 일치하는 프리셋이 있는지 확인하여 초기 선택 상태 설정
            val presets = timerPresets.value
            _selectedPreset.value = presets.find { it.tag == tag && it.durationMinutes == minutes }
        }
    }

    // 프리셋 선택 (이번 세션에만 적용 및 저장)
    fun selectPresetForSession(preset: TimerPreset) {
        _selectedPreset.value = preset
        _sessionTag.value = preset.tag
        _sessionDurationMinutes.value = preset.durationMinutes
        saveLastUsed(preset.tag, preset.durationMinutes)
    }

    // 커스텀 수정 (이번 세션에만 적용 및 저장)
    fun updateSessionSettings(tag: String, minutes: Int) {
        _selectedPreset.value = null // 커스텀 수정 시 프리셋 선택 해제
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

    // DEBUG: Direct control for testing
    fun debugAddExp(amount: Int) {
        viewModelScope.launch {
            repository.addExp(amount)
        }
    }

    fun debugSetExp(exactExp: Int) {
        viewModelScope.launch {
            val currentUser = user.value ?: User()
            val newLevel = when {
                exactExp < 300 -> 1
                exactExp < 1000 -> 2
                exactExp < 3000 -> 3
                exactExp < 8000 -> 4
                else -> 5
            }
            repository.addExp(exactExp - currentUser.fireExp)
        }
    }

}
