package com.japygo.modakmodak.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.data.entity.StudyLog
import com.japygo.modakmodak.data.repository.ModakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class DailyStats(
    val totalSeconds: Int,
    val hasLog: Boolean,
)

class StatsViewModel(
    private val repository: ModakRepository,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Loading range: 2 years back for heatmap context
    private val startTimestamp = _currentMonth.map {
        it.minusMonths(12).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private val _logs = MutableStateFlow<List<StudyLog>>(emptyList())
    val logs: StateFlow<List<StudyLog>> = _logs.asStateFlow()

    init {
        // Initial load
        loadLogs()
    }

    fun loadLogs() {
        val start = YearMonth.now().minusMonths(11).atDay(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val end = System.currentTimeMillis()

        viewModelScope.launch {
            repository.getLogsForRange(start, end).collect {
                _logs.value = it
            }
        }
    }

    val heatmapData: StateFlow<Map<LocalDate, DailyStats>> = _logs.map { logs ->
        logs.groupBy {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry ->
            val totalSeconds = entry.value.filter { it.isSuccess }.sumOf { it.durationSeconds }
            val hasLog = entry.value.isNotEmpty()
            DailyStats(totalSeconds, hasLog)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    val currentMonthStats = combine(_logs, _currentMonth, _selectedDate) { logs, month, selected ->
        logs.filter {
            val date = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            // If date selected, filter by date. Else filter by month.
            if (selected != null) {
                date == selected
            } else {
                YearMonth.from(date) == month
            }
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) {
            _selectedDate.value = null // Toggle off
        } else {
            _selectedDate.value = date
        }
    }

    val totalTimeThisMonth = currentMonthStats.map { logs ->
        val totalSeconds = logs.sumOf { it.durationSeconds }
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        "${hours}h ${minutes}m"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0h 0m")

    val monthSuccessRate = currentMonthStats.map { logs ->
        if (logs.isEmpty()) {
            "0%"
        } else {
            val successCount = logs.count { it.isSuccess }
            val rate = (successCount.toFloat() / logs.size) * 100
            "${rate.toInt()}%"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0%")

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        // Limit to current month? Or allow future?
        if (_currentMonth.value.isBefore(YearMonth.now())) {
            _currentMonth.value = _currentMonth.value.plusMonths(1)
        }
    }
}
