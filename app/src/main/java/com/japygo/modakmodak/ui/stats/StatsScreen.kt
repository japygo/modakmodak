package com.japygo.modakmodak.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.data.entity.StudyLog
import com.japygo.modakmodak.ui.components.ModakBottomBar
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel,
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val totalTime by viewModel.totalTimeThisMonth.collectAsState()
    val successRate by viewModel.monthSuccessRate.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val currentMonthLogs by viewModel.currentMonthStats.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BackgroundDark,
        topBar = {
            StatsTopBar(
                month = currentMonth,
                onPrev = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
            )
        },
        bottomBar = { ModakBottomBar(navController, "stats") },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                SummaryCard(totalTime = totalTime, successRate = successRate)
            }

            item {
                Text(
                    stringResource(R.string.stats_heatmap_title),
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                HeatmapCalendar(
                    month = currentMonth,
                    heatmapData = heatmapData,
                    selectedDate = selectedDate,
                    onDateClick = { viewModel.selectDate(it) },
                )
            }

            item {
                Text(
                    stringResource(R.string.stats_logs_title),
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            items(currentMonthLogs) { log ->
                LogItemCard(log)
            }

            item { Spacer(Modifier.height(50.dp)) }
        }
    }
}

@Composable
fun StatsTopBar(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern(stringResource(R.string.stats_date_format))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Prev", tint = TextSecondary)
        }
        Text(month.format(formatter), color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next", tint = TextSecondary)
        }
    }
}

@Composable
fun SummaryCard(totalTime: String, successRate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.stats_summary_total_focus),
                color = TextSecondary,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(totalTime, color = FireOrange, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                stringResource(R.string.stats_summary_success_rate),
                color = TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(successRate, color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HeatmapCalendar(
    month: YearMonth,
    heatmapData: Map<LocalDate, DailyStats>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
) {
    // ... (Existing calculation logic unchanged) ...
    val daysInMonth = month.lengthOfMonth()
    val startOffset = month.atDay(1).dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp),
    ) {
        // Weekday Headers
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(
                R.string.day_mon, R.string.day_tue, R.string.day_wed,
                R.string.day_thu, R.string.day_fri, R.string.day_sat, R.string.day_sun,
            ).forEach { resId ->
                Text(
                    stringResource(resId),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val days = (1..daysInMonth).map { month.atDay(it) }
        val offset = month.atDay(1).dayOfWeek.value - 1 // 0=Mon
        val totalSlots = daysInMonth + offset
        val rows = (totalSlots + 6) / 7

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    if (index >= offset && index < offset + daysInMonth) {
                        val date = days[index - offset]
                        val stats = heatmapData[date]
                        HeatmapCell(
                            date = date,
                            stats = stats,
                            isSelected = date == selectedDate,
                            onClick = { onDateClick(date) },
                        )
                    } else {
                        // Crucial: Use a Spacer of the SAME size as a Cell to maintain alignment
                        // HeatmapCell is 40.dp outer box
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun getFireColor(seconds: Int, hasLog: Boolean = true): Color {
    return when {
        seconds == 0 && hasLog -> Color(0xFF5f6368) // Gray for Failed
        seconds == 0 -> Color.Transparent // No log
        seconds < 1800 -> Color(0xFFFFF59D) // Level 1 (1~30m): Very Pale Yellow (Yellow 200) -> Fainter
        seconds < 3600 -> Color(0xFFFFCC80) // Level 2 (30m~1h): Pale Orange (Orange 200)
        seconds < 7200 -> Color(0xFFFFB74D) // Level 3 (1h~2h): Soft Orange (Orange 300)
        seconds < 14400 -> Color(0xFFFF7043) // Level 4 (2h~4h): Deep Orange (DeepOrange 400)
        else -> Color(0xFFD32F2F) // Level 5 (4h+): Red (Red 700)
    }
}

@Composable
fun HeatmapCell(date: LocalDate, stats: DailyStats?, isSelected: Boolean, onClick: () -> Unit) {
    val seconds = stats?.totalSeconds ?: 0
    val hasLog = stats?.hasLog ?: false

    val fireColor = getFireColor(seconds, hasLog)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Circle background for the "coal" or base
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SurfaceHighlight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            if (hasLog) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = fireColor,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                // Text only visible if no log
                Text(
                    "${date.dayOfMonth}",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun LogItemCard(log: StudyLog) {
    val date = Instant.ofEpochMilli(log.date).atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textColor = getFireColor(if (log.isSuccess) log.durationSeconds else 0, true)

            val title = log.tag.takeIf { !it.isNullOrBlank() }
                ?: stringResource(R.string.stats_log_default_title)
            Text(
                text = title + if (!log.isSuccess) stringResource(R.string.stats_log_given_up_suffix) else "",
                color = textColor,
                fontWeight = FontWeight.Bold,
            )
            Text(date.format(formatter), color = TextSecondary, fontSize = 12.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "+ ${log.earnedCoin}",
                color = FireOrange,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = FireOrange,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
