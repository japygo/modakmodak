package com.japygo.modakmodak.ui.focus

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.data.repository.ModakRepository
import com.japygo.modakmodak.data.repository.SettingsRepository
import com.japygo.modakmodak.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusViewModel(
    private val repository: ModakRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    val isBreakEnabled: StateFlow<Boolean> = settingsRepository.isBreakEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isNotificationEnabled: StateFlow<Boolean> = settingsRepository.isNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isScreenOnEnabled: StateFlow<Boolean> = settingsRepository.isScreenOnEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val user = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isFocusing = MutableStateFlow(false)
    val isFocusing: StateFlow<Boolean> = _isFocusing.asStateFlow()

    // 0 = Initial, 1 = Focusing, 2 = Success, 3 = Failed
    private val _sessionState = MutableStateFlow(0)
    val sessionState: StateFlow<Int> = _sessionState.asStateFlow()

    private var timer: CountDownTimer? = null
    private val _initialDuration = MutableStateFlow(1) // Avoid divide by zero
    val initialDuration: StateFlow<Int> = _initialDuration.asStateFlow()

    private var currentTag: String? = null

    fun startTimer(durationMinutes: Int = 25, tag: String? = null) {
        _isFocusing.value = true
        _sessionState.value = 1
        currentTag = tag
        val totalSeconds = durationMinutes * 60
        _initialDuration.value = totalSeconds
        _timeLeft.value = totalSeconds

        val durationMillis = durationMinutes * 60 * 1000L

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _timeLeft.value = 0
                _isFocusing.value = false
                _sessionState.value = 2 // Success
                logSession(true, _initialDuration.value)

                // Fetch latest setting directly from repository to avoid StateFlow staleness in background
                viewModelScope.launch {
                    if (settingsRepository.isNotificationEnabled.first()) {
                        val lang = settingsRepository.appLanguage.first()
                        notificationHelper.showStudyFinishedNotification(lang)
                    }
                }
            }
        }.start()
    }

    fun stopTimer() {
        val elapsed = _initialDuration.value - _timeLeft.value
        timer?.cancel()
        _isFocusing.value = false
        _sessionState.value = 3 // Failed (Given up)

        // 최소 1초 이상 집중했을 때만 기록
        if (elapsed > 0) {
            logSession(false, elapsed)
        }
    }

    private fun logSession(success: Boolean, durationSeconds: Int) {
        viewModelScope.launch {
            val durationMinutes = durationSeconds / 60
            // 실패 시에도 집중한 1분당 1코인 지급 (사용자 요청 반영)
            val coins = durationMinutes
            repository.addCoins(coins)
            if (success) repository.addExp(durationMinutes * 2)
            repository.logSession(durationSeconds, success, coins, currentTag)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
