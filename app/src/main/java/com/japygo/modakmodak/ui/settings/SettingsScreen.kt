package com.japygo.modakmodak.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.components.ModakBottomBar
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
) {
    val isScreenOnEnabled by viewModel.isScreenOnEnabled.collectAsState()
    val isBreakEnabled by viewModel.isBreakEnabled.collectAsState()
    val breakDurationMinutes by viewModel.breakDurationMinutes.collectAsState()
    val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var editingPreset by remember {
        mutableStateOf<com.japygo.modakmodak.data.entity.TimerPreset?>(
            null,
        )
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        color = White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        bottomBar = { ModakBottomBar(navController, "settings") },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Timer Presets (Most frequently used)
            item {
                val presets by viewModel.timerPresets.collectAsState()
                TimerPresetsSection(
                    presets = presets,
                    onAddClick = { showAddPresetDialog = true },
                    onDeleteClick = { viewModel.deletePreset(it) },
                    onEditClick = { editingPreset = it },
                )
            }

            // 2. Focus Settings
            item { SettingSectionTitle(stringResource(R.string.settings_section_focus)) }

            item {
                ToggleSettingItem(
                    title = stringResource(R.string.settings_toggle_screen_on),
                    icon = Icons.Default.ScreenLockRotation,
                    checked = isScreenOnEnabled,
                    onCheckedChange = { viewModel.toggleScreenOn(it) },
                )
            }

            // 3. Break Settings
            item { SettingSectionTitle(stringResource(R.string.settings_section_break)) }

            item {
                ToggleSettingItem(
                    title = stringResource(R.string.settings_toggle_break_screen),
                    icon = Icons.Default.Coffee,
                    checked = isBreakEnabled,
                    onCheckedChange = { viewModel.toggleBreak(it) },
                )
            }

            if (isBreakEnabled) {
                item {
                    DurationSettingItem(
                        title = stringResource(R.string.settings_break_duration),
                        minutes = breakDurationMinutes,
                        onValueChange = { viewModel.updateBreakDuration(it.toInt()) },
                    )
                }
            }

            // 4. Notifications
            item { SettingSectionTitle(stringResource(R.string.settings_section_sound)) }

            item {
                ToggleSettingItem(
                    title = stringResource(R.string.settings_toggle_notifications),
                    icon = Icons.Default.Notifications,
                    checked = isNotificationEnabled,
                    onCheckedChange = { viewModel.toggleNotification(it) },
                )
            }

            // 5. Language
            item { SettingSectionTitle(stringResource(R.string.settings_section_language)) }

            item {
                LanguageSelectorItem(
                    currentLanguage = appLanguage,
                    onLanguageSelected = { viewModel.updateAppLanguage(it) },
                )
            }

            // 6. Account & Info
            item { SettingSectionTitle(stringResource(R.string.settings_section_account)) }

            item {
                DangerSettingItem(
                    title = stringResource(R.string.settings_reset_data),
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteDialog = true },
                )
            }

            item { SettingSectionTitle(stringResource(R.string.settings_section_info)) }

            item {
                InfoItem(
                    title = stringResource(R.string.settings_version_label),
                    value = "1.0.0",
                    icon = Icons.Default.Info,
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_reset_dialog_title)) },
            text = { Text(stringResource(R.string.settings_reset_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                ) {
                    Text(stringResource(R.string.common_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = White,
            textContentColor = TextSecondary,
        )
    }

    if (showAddPresetDialog) {
        TimerPresetDialog(
            title = stringResource(R.string.settings_add_preset_title),
            onDismiss = { showAddPresetDialog = false },
            onConfirm = { tag, minutes ->
                viewModel.addPreset(tag, minutes)
                showAddPresetDialog = false
            },
        )
    }

    if (editingPreset != null) {
        TimerPresetDialog(
            title = stringResource(R.string.settings_edit_preset_title),
            initialTag = editingPreset!!.tag,
            initialMinutes = editingPreset!!.durationMinutes,
            onDismiss = { editingPreset = null },
            onConfirm = { tag, minutes ->
                viewModel.updatePreset(editingPreset!!.copy(tag = tag, durationMinutes = minutes))
                editingPreset = null
            },
        )
    }
}

@Composable
fun TimerPresetsSection(
    presets: List<com.japygo.modakmodak.data.entity.TimerPreset>,
    onAddClick: () -> Unit,
    onDeleteClick: (com.japygo.modakmodak.data.entity.TimerPreset) -> Unit,
    onEditClick: (com.japygo.modakmodak.data.entity.TimerPreset) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingSectionTitle(stringResource(R.string.settings_section_presets))
            IconButton(onClick = onAddClick) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.settings_add_preset_title),
                    tint = FireOrange,
                )
            }
        }

        if (presets.isEmpty()) {
            Text(
                stringResource(R.string.settings_presets_empty),
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        presets.forEach { preset ->
            val hours = preset.durationMinutes / 60
            val minutes = preset.durationMinutes % 60
            val timeString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark,
                ),
                border = if (preset.isSelected) BorderStroke(
                    1.dp,
                    FireOrange.copy(alpha = 0.5f),
                ) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                tint = FireOrange,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.settings_preset_label),
                                color = FireOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClick(preset) },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = SurfaceHighlight.copy(alpha = if (preset.isSelected) 0.3f else 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditClick(preset) },
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Label,
                                    contentDescription = null,
                                    tint = if (preset.isSelected) FireOrange else TextSecondary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(preset.tag, color = White, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = SurfaceHighlight.copy(alpha = if (preset.isSelected) 0.3f else 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditClick(preset) },
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (preset.isSelected) FireOrange else TextSecondary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(timeString, color = White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerPresetDialog(
    title: String = stringResource(R.string.settings_preset_dialog_default_title),
    initialTag: String = "#",
    initialMinutes: Int = 25,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    val context = LocalContext.current
    var tag by remember { mutableStateOf(initialTag.removePrefix("#")) }
    var hours by remember { mutableIntStateOf(initialMinutes / 60) }
    var minutes by remember { mutableIntStateOf(initialMinutes % 60) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = White) },
        text = {
            Column {
                TextField(
                    value = tag,
                    onValueChange = { tag = it.replace("#", "") },
                    label = { Text(stringResource(R.string.settings_preset_dialog_tag_label)) },
                    prefix = { Text("#", color = FireOrange, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedContainerColor = SurfaceHighlight.copy(alpha = 0.1f),
                        unfocusedContainerColor = SurfaceHighlight.copy(alpha = 0.1f),
                    ),
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    stringResource(R.string.settings_preset_dialog_duration_label),
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TimeUnitPicker(
                        label = stringResource(R.string.settings_preset_dialog_hrs),
                        value = hours,
                        range = 0..99,
                        isInfinite = false,
                        onValueChange = { hours = it },
                    )
                    Text(
                        ":",
                        color = White,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    TimeUnitPicker(
                        label = stringResource(R.string.settings_preset_dialog_mins),
                        value = minutes,
                        range = 0..59,
                        isInfinite = true,
                        onValueChange = { minutes = it },
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val total = hours * 60 + minutes
                    if (total <= 0) {
                        errorText = context.getString(R.string.settings_preset_dialog_error_min)
                    } else {
                        val finalTag = if (tag.isBlank()) "#Focus" else "#" + tag.trim()
                        onConfirm(finalTag, total)
                    }
                },
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
    )
}

@Composable
fun TimeUnitPicker(
    label: String,
    value: Int,
    range: IntRange,
    isInfinite: Boolean = true,
    onValueChange: (Int) -> Unit,
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
                .width(70.dp),
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
                            text = String.format("%02d", num),
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

@Composable
fun LanguageSelectorItem(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LanguageButton(
            text = stringResource(R.string.settings_language_en),
            isSelected = currentLanguage == "en",
            onClick = { onLanguageSelected("en") },
            modifier = Modifier.weight(1f),
        )
        LanguageButton(
            text = stringResource(R.string.settings_language_ko),
            isSelected = currentLanguage == "ko",
            onClick = { onLanguageSelected("ko") },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun LanguageButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) FireOrange.copy(alpha = 0.2f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, FireOrange) else BorderStroke(
            1.dp,
            SurfaceHighlight,
        ),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = if (isSelected) FireOrange else White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        color = FireOrange,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
fun DurationSettingItem(
    title: String,
    minutes: Int,
    onValueChange: (Float) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, color = White, fontSize = 16.sp)
                }
                Text(
                    "${minutes}m",
                    color = FireOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Slider(
                value = minutes.toFloat(),
                onValueChange = onValueChange,
                valueRange = 1f..60f,
                steps = 59,
                colors = SliderDefaults.colors(
                    thumbColor = FireOrange,
                    activeTrackColor = FireOrange,
                    inactiveTrackColor = TextSecondary.copy(alpha = 0.3f),
                ),
            )
        }
    }
}

@Composable
fun ToggleSettingItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = White, fontSize = 16.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = FireOrange,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceDark,
                ),
            )
        }
    }
}

@Composable
fun DangerSettingItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                title,
                color = Color.Red.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun InfoItem(
    title: String,
    value: String,
    icon: ImageVector,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = White, fontSize = 16.sp)
            }
            Text(value, color = TextSecondary, fontSize = 16.sp)
        }
    }
}
