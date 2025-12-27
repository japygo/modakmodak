package com.japygo.modakmodak.ui.breaktime

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.R
import com.japygo.modakmodak.data.repository.SettingsRepository
import com.japygo.modakmodak.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BreakViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    val isBreakTimerEnabled: StateFlow<Boolean> = settingsRepository.isBreakTimerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isNotificationEnabled: StateFlow<Boolean> = settingsRepository.isNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _randomMessage = MutableStateFlow(R.string.break_msg_level1_1)
    val randomMessage: StateFlow<Int> = _randomMessage.asStateFlow()

    private var studyDurationMinutes: Int = 0

    fun setStudyDuration(minutes: Int) {
        this.studyDurationMinutes = minutes
    }

    private val level1Messages = listOf(
        R.string.break_msg_level1_1,
        R.string.break_msg_level1_2,
        R.string.break_msg_level1_3,
        R.string.break_msg_level1_4
    )
    private val level2Messages = listOf(
        R.string.break_msg_level2_1,
        R.string.break_msg_level2_2,
        R.string.break_msg_level2_3,
        R.string.break_msg_level2_4
    )
    private val level3Messages = listOf(
        R.string.break_msg_level3_1,
        R.string.break_msg_level3_2,
        R.string.break_msg_level3_3,
        R.string.break_msg_level3_4
    )
    private val level4Messages = listOf(
        R.string.break_msg_level4_1,
        R.string.break_msg_level4_2,
        R.string.break_msg_level4_3,
        R.string.break_msg_level4_4
    )
    private val level5Messages = listOf(
        R.string.break_msg_level5_1,
        R.string.break_msg_level5_2,
        R.string.break_msg_level5_3,
        R.string.break_msg_level5_4
    )

    private var timer: CountDownTimer? = null

    fun startBreak() {
        viewModelScope.launch {
            val durationMinutes = settingsRepository.breakDurationMinutes.first()
            val durationMillis = durationMinutes * 60 * 1000L
            _timeLeft.value = durationMinutes * 60
            _isFinished.value = false

            // 학습량에 따른 메시지 선택
            _randomMessage.value = when {
                studyDurationMinutes < 30 -> level1Messages.random()
                studyDurationMinutes < 60 -> level2Messages.random()
                studyDurationMinutes < 120 -> level3Messages.random()
                studyDurationMinutes < 240 -> level4Messages.random()
                else -> level5Messages.random()
            }

            timer?.cancel()
            timer = object : CountDownTimer(durationMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timeLeft.value = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    _timeLeft.value = 0
                    _isFinished.value = true
                    
                    // Fetch latest setting directly from repository to avoid StateFlow staleness in background
                    viewModelScope.launch {
                        if (settingsRepository.isNotificationEnabled.first()) {
                            val lang = settingsRepository.appLanguage.first()
                            notificationHelper.showBreakFinishedNotification(lang)
                        }
                    }
                }
            }.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
