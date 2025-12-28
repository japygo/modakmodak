package com.japygo.modakmodak.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.japygo.modakmodak.BuildConfig
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.japygo.modakmodak.ui.theme.FireOrange


@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel,
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val totalTime by viewModel.totalTimeThisMonth.collectAsState()
    val successRate by viewModel.monthSuccessRate.collectAsState()
    val hardcoreCount by viewModel.hardcoreStats.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val currentMonthLogs by viewModel.currentMonthStats.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Debug Dialog State
    var showDebugDialog by remember { mutableStateOf(false) }

    if (showDebugDialog) {
        AlertDialog(
            onDismissRequest = { showDebugDialog = false },
            title = { Text("DEBUG: Stats Controller", color = White) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section: Data Management
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Data Management",
                            color = FireOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = { 
                                viewModel.debugInjectLogs()
                                showDebugDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SurfaceHighlight
                            )
                        ) {
                            Text("Add Test Logs (Rainbow)", color = White)
                        }

                        Button(
                            onClick = { 
                                viewModel.debugClearLogs()
                                showDebugDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3E2723) // Very Dark Red/Brown background
                            )
                        ) {
                            Text("Clear All Logs", color = Color(0xFFFF5252)) // Red-ish text
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDebugDialog = false }) { Text("Close") }
            },
            containerColor = SurfaceDark
        )
    }

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
                onDebugTrigger = { showDebugDialog = true },
                onMonthSelected = { viewModel.setMonth(it) }
            )
        },
        bottomBar = { ModakBottomBar(navController, "stats") },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            val availableTags by viewModel.availableTags.collectAsState()
            val selectedTags by viewModel.selectedTags.collectAsState()
            
            TagFilterRow(
                availableTags = availableTags,
                selectedTags = selectedTags,
                onTagToggle = { viewModel.toggleTag(it) },
                onClearTags = { viewModel.clearTags() }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // item { TagFilterRow... } removed from here

                item {
                    SummaryCard(
                        totalTime = totalTime, 
                        successRate = successRate,
                        hardcoreCount = hardcoreCount
                    )
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

            if (currentMonthLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.stats_log_empty),
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(currentMonthLogs) { log ->
                    LogItemCard(log)
                }
            }

            item { Spacer(Modifier.height(50.dp)) }
        }
    }
}
}

@Composable
fun StatsTopBar(
    month: YearMonth, 
    onPrev: () -> Unit, 
    onNext: () -> Unit,
    onDebugTrigger: () -> Unit = {},
    onMonthSelected: (YearMonth) -> Unit
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = month,
            onDismiss = { showMonthPicker = false },
            onConfirm = { 
                onMonthSelected(it)
                showMonthPicker = false
            }
        )
    }

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
        Text(
            text = month.format(formatter), 
            color = White, 
            fontSize = 22.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showMonthPicker = true },
                        onLongPress = if (BuildConfig.DEBUG) { 
                            { onDebugTrigger() } 
                        } else null
                    )
                }
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next", tint = TextSecondary)
        }
    }
}

@Composable
fun SummaryCard(totalTime: String, successRate: Int, hardcoreCount: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Total Focus
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                stringResource(R.string.stats_summary_total_focus),
                color = TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(totalTime, color = FireOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(TextSecondary.copy(alpha = 0.2f))
        )

        // Success Rate
        val successColor = if (successRate == 100) {
            FireOrange
        } else {
            // Interpolate between TextSecondary (Gray) and FireOrange
            // Simple approach: standard White/Gray for lower, Orange tint for higher
            // Or just Gray -> White -> Orange. 
            // User requested: "Lower -> Faint/Gray, 100% -> Orange"
            // Let's keep it simple: < 50% Gray, 50-99% White, 100% Orange for now, 
            // OR use Color Utils to blend. Since we don't have lerp readily available without setup,
            // let's use weighted steps.
             when {
                successRate == 100 -> FireOrange
                successRate >= 80 -> Color(0xFFFFCC80) // Pale Orange
                successRate >= 50 -> White
                else -> TextSecondary
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.stats_summary_success_rate),
                color = TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("$successRate%", color = successColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (hardcoreCount != null) {
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(TextSecondary.copy(alpha = 0.2f))
            )

            // Hardcore Success
            // Color Logic: More attempts -> Redder
            val hardcoreColor = when {
                hardcoreCount >= 20 -> Color(0xFFD32F2F) // Red (Level 5)
                hardcoreCount >= 10 -> Color(0xFFFF7043) // Deep Orange (Level 4)
                hardcoreCount >= 5 -> Color(0xFFFFB74D) // Soft Orange (Level 3)
                hardcoreCount >= 3 -> Color(0xFFFFCC80) // Pale Orange (Level 2)
                else -> Color(0xFFFFF59D) // Pale Yellow (Level 1)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.stats_summary_hardcore_success),
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Icon removed, Text Centered
                Text("$hardcoreCount", color = hardcoreColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
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
    val endTime = Instant.ofEpochMilli(log.date).atZone(ZoneId.systemDefault())
    val startTime = endTime.minusSeconds(log.durationSeconds.toLong())
    
    val datePattern = stringResource(R.string.stats_log_date_format)
    val timePattern = stringResource(R.string.stats_log_time_format)
    
    val dateFormatter = DateTimeFormatter.ofPattern(datePattern)
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern)

    val startDateStr = startTime.format(dateFormatter)
    val endDateStr = endTime.format(dateFormatter)
    val startTimeStr = startTime.format(timeFormatter)
    val endTimeStr = endTime.format(timeFormatter)
    
    val timeDisplay = if (startDateStr == endDateStr) {
        "$startDateStr $startTimeStr - $endTimeStr"
    } else {
        "$startDateStr $startTimeStr - $endDateStr $endTimeStr"
    }

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
                text = title,
                color = textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(timeDisplay, color = TextSecondary, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceHighlight.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = FireOrange,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${log.earnedCoin}",
                    color = FireOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TagFilterRow(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onClearTags: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            val isAllSelected = selectedTags.isEmpty()
            androidx.compose.material3.FilterChip(
                selected = isAllSelected,
                onClick = onClearTags,
                label = { Text(stringResource(R.string.filter_all)) },
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FireOrange,
                    selectedLabelColor = White,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                ),
                border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isAllSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 0.dp
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }

        items(availableTags) { tag ->
            val isSelected = selectedTags.contains(tag)
            androidx.compose.material3.FilterChip(
                selected = isSelected,
                onClick = { onTagToggle(tag) },
                label = { Text(tag) },
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FireOrange,
                    selectedLabelColor = White,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                ),
                border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 0.dp
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    var selectedYear by remember { mutableIntStateOf(currentMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth.monthValue) }

    // Year range: 2000 to Current Year
    val now = YearMonth.now()
    val currentYear = now.year
    val startYear = 2000
    val endYear = currentYear
    val yearRange = startYear..endYear

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.stats_select_month_title), 
                color = White,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Year Picker
                StatsWheelPicker(
                    label = stringResource(R.string.stats_year),
                    value = selectedYear,
                    range = yearRange,
                    isInfinite = false,
                    onValueChange = { selectedYear = it },
                    format = { String.format("%04d", it) } 
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                // Month Picker
                StatsWheelPicker(
                    label = stringResource(R.string.stats_month),
                    value = selectedMonth,
                    range = 1..12,
                    isInfinite = true,
                    onValueChange = { selectedMonth = it },
                    format = { String.format("%02d", it) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                   val selected = YearMonth.of(selectedYear, selectedMonth)
                   // Clamp to current month if future
                   if (selected.isAfter(now)) {
                       onConfirm(now)
                   } else {
                       onConfirm(selected)
                   }
                }
            ) {
                Text(stringResource(R.string.common_confirm), color = FireOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = TextSecondary)
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
    )
}


@Composable
fun StatsWheelPicker(
    label: String,
    value: Int,
    range: IntRange,
    isInfinite: Boolean = true,
    onValueChange: (Int) -> Unit,
    format: (Int) -> String = { it.toString() }
) {
    val itemsCount = range.last - range.first + 1
    val totalCount = if (isInfinite) 1000 * itemsCount else itemsCount
    val initialIndex = if (isInfinite) {
        totalCount / 2 + (value - range.first)
    } else {
        value - range.first
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val isScrolling = listState.isScrollInProgress
    LaunchedEffect(isScrolling) {
        if (!isScrolling) {
            val selectedIndex = listState.firstVisibleItemIndex
            val actualValue = range.first + (selectedIndex % itemsCount)
            onValueChange(actualValue)

            if (isInfinite && (selectedIndex < totalCount / 4 || selectedIndex > totalCount * 3 / 4)) {
                listState.scrollToItem(totalCount / 2 + (actualValue - range.first))
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(120.dp)
                .width(80.dp), // Slightly wider than time picker
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(SurfaceHighlight.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(totalCount) { index ->
                    val num = range.first + (index % itemsCount)
                    val isSelected = listState.firstVisibleItemIndex == index

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = format(num),
                            color = if (isSelected) FireOrange else TextSecondary.copy(alpha = 0.5f),
                            fontSize = if (isSelected) 24.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
        Text(
            label,
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
