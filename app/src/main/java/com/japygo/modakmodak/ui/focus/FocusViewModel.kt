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

enum class FailureReason {
    NONE,
    MANUAL,
    BACKGROUND
}

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

    val isHardcoreModeEnabled: StateFlow<Boolean> = settingsRepository.isHardcoreModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val user = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAdLoaded: StateFlow<Boolean> = com.japygo.modakmodak.utils.AdMobManager.isFocusAdLoaded
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isFocusing = MutableStateFlow(false)
    val isFocusing: StateFlow<Boolean> = _isFocusing.asStateFlow()

    // 0 = Initial, 1 = Focusing, 2 = Success, 3 = Failed
    private val _sessionState = MutableStateFlow(0)
    val sessionState: StateFlow<Int> = _sessionState.asStateFlow()

    private val _failureReason = MutableStateFlow(FailureReason.NONE)
    val failureReason: StateFlow<FailureReason> = _failureReason.asStateFlow()

    private var timer: CountDownTimer? = null
    private val _initialDuration = MutableStateFlow(1) // Avoid divide by zero
    val initialDuration: StateFlow<Int> = _initialDuration.asStateFlow()

    private var currentTag: String? = null

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var sessionHardcoreMode = false

    fun startTimer(durationMinutes: Int = 25, tag: String? = null) {
        viewModelScope.launch {
            sessionHardcoreMode = settingsRepository.isHardcoreModeEnabled.first()
            _isFocusing.value = true
            _isPaused.value = false
            _sessionState.value = 1
            currentTag = tag
            val totalSeconds = durationMinutes * 60
            _initialDuration.value = totalSeconds
            _timeLeft.value = totalSeconds
            startCountDown(totalSeconds * 1000L)
        }
    }

    private fun startCountDown(durationMillis: Long) {
        timer?.cancel()
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
                    val isMasterEnabled = settingsRepository.isNotificationEnabled.first()
                    val isStudyEnabled = settingsRepository.isStudyNotificationEnabled.first()
                    
                    if (isMasterEnabled && isStudyEnabled) {
                        val lang = settingsRepository.appLanguage.first()
                        notificationHelper.showStudyFinishedNotification(lang)
                    }
                }
            }
        }.start()
    }

    fun pauseTimer() {
        if (_isFocusing.value && !_isPaused.value) {
            timer?.cancel()
            _isPaused.value = true
        }
    }

    fun resumeTimer() {
        if (_isFocusing.value && _isPaused.value) {
            startCountDown(_timeLeft.value * 1000L)
            _isPaused.value = false
        }
    }

    fun onAppBackgrounded() {
        if (_isFocusing.value && _sessionState.value == 1) {
            if (sessionHardcoreMode) {
                // Hardcore Mode: Immediate Fail
                stopTimer(FailureReason.BACKGROUND) 
                // stopTimer sets sessionState to 3 (Failed)
            } else {
                // Normal Mode: Pause
                pauseTimer()
            }
        }
    }

    fun onAppForegrounded() {
        // Normal Mode: Logic handled by user interaction (Resume Button)
        // Hardcore Mode: If failed, state is 3, UI shows Fail Screen.
    }

    fun stopTimer(reason: FailureReason = FailureReason.MANUAL) {
        val elapsed = _initialDuration.value - _timeLeft.value
        timer?.cancel()
        _isFocusing.value = false
        _isPaused.value = false
        _sessionState.value = 3 // Failed (Given up)
        _failureReason.value = reason

        // 최소 1초 이상 집중했을 때만 기록
        if (elapsed > 0) {
            logSession(false, elapsed)
        }
    }

    private val _sessionResult = MutableStateFlow<ModakRepository.SessionResult?>(null)
    val sessionResult: StateFlow<ModakRepository.SessionResult?> = _sessionResult.asStateFlow()

    fun navigateToNextParam(navController: androidx.navigation.NavController, result: ModakRepository.SessionResult, duration: Int) {
        viewModelScope.launch {
            val breakEnabled = isBreakEnabled.first()
            // Always go to RewardScreen first, pass necessary params including isBreakEnabled
            // Route format: reward/{earnedCoins}/{earnedExp}/{duration}/{streakDays}/{isBreakEnabled}
            navController.navigate("reward/${result.earnedCoins}/${result.earnedExp}/$duration/${result.streakDays}/$breakEnabled") {
                popUpTo("home") 
            }
        }
    }

    private fun logSession(success: Boolean, durationSeconds: Int) {
        viewModelScope.launch {
            val durationMinutes = durationSeconds / 60
            // 실패 시에도 집중한 1분당 1코인 지급 (사용자 요청 반영)
            val coins = durationMinutes
            // Repository now handles Coin/Exp addition based on Hardcore Mode
            // Repository now handles Coin/Exp addition based on Hardcore Mode
            val result = repository.logSession(durationSeconds, success, coins, currentTag, sessionHardcoreMode)
            
            // Always set result to show earned coins even on failure
            _sessionResult.value = result
        }
    }

    fun doubleReward(amount: Int) {
        viewModelScope.launch {
            repository.addCoins(amount)
            repository.updateLastLogReward(amount)
            // Update session result for UI
            val current = _sessionResult.value
            if (current != null) {
                _sessionResult.value = current.copy(earnedCoins = current.earnedCoins + amount)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
