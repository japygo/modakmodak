package com.japygo.modakmodak.ui.home

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.components.ModakBottomBar
import com.japygo.modakmodak.ui.components.ModakCharacter
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.Orange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
) {
    val user by viewModel.user.collectAsState()
    val timerPresets by viewModel.timerPresets.collectAsState()
    val sessionTag by viewModel.sessionTag.collectAsState()
    val sessionDuration by viewModel.sessionDurationMinutes.collectAsState()

    var showPresetSelector by remember { mutableStateOf(false) }

    // Get fire color based on level
    val fireColor = remember(user?.fireLevel) {
        getFireColorByLevel(user?.fireLevel ?: 1)
    }

    // TODO: Replace with actual logic based on logs added
    val characterScale = 1.0f
    // Background minimum size is 250.dp (at scale 1.0f), scales up when character grows
    val backgroundSize = (250.dp * characterScale.coerceAtLeast(1.0f)).coerceAtMost(500.dp)

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BackgroundDark,
        topBar = { HomeTopBar(navController, user?.currentCoin ?: 0) },
        bottomBar = { ModakBottomBar(navController, "home") },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top 2/3 section for character and background
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                // Background size scales with character
                Box(
                    modifier = Modifier
                        .size(backgroundSize)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(fireColor.copy(alpha = 0.6f), Color.Transparent),
                            ),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Modak Character with Fire Animation
                    // Apply flame color based on user level
                    // Scale entire character proportionally
                    // clipToBounds = false allows animation to overflow when scaled without clipping
                    ModakCharacter(
                        flameColor = fireColor,
                        scale = characterScale,
                        clipToBounds = false,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(bottom = 20.dp),
                    )
                }
            }

            // Bottom 1/3 section for timer and button (fixed position)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Timer
                val hours = sessionDuration / 60
                val minutes = sessionDuration % 60
                val timeText = if (hours > 0) String.format(
                    "%02d:%02d:00",
                    hours,
                    minutes,
                ) else String.format("%02d:00", minutes)

                Text(
                    text = timeText,
                    color = Color.White,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showPresetSelector = true },
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.home_timer_adjust_hint),
                    color = Color.Gray,
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Start Button with tag inside
                Button(
                    onClick = {
                        val encodedTag = Uri.encode(sessionTag)
                        navController.navigate("focus/$sessionDuration/$encodedTag")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Orange, FireOrange),
                            ),
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.home_start_button, sessionTag),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    if (showPresetSelector) {
        PresetSelectionDialog(
            presets = timerPresets,
            currentTag = sessionTag,
            currentMinutes = sessionDuration,
            onDismiss = { showPresetSelector = false },
            onPresetSelect = { viewModel.selectPresetForSession(it) },
            onCustomConfirm = { tag, minutes ->
                viewModel.updateSessionSettings(tag, minutes)
                showPresetSelector = false
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetSelectionDialog(
    presets: List<com.japygo.modakmodak.data.entity.TimerPreset>,
    currentTag: String,
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onPresetSelect: (com.japygo.modakmodak.data.entity.TimerPreset) -> Unit,
    onCustomConfirm: (String, Int) -> Unit,
) {
    var showCustomEdit by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.home_preset_dialog_title),
                    color = White,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = { showCustomEdit = true }) {
                    Text(stringResource(R.string.home_preset_dialog_custom), color = FireOrange)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.home_preset_dialog_subtitle),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (presets.isEmpty()) {
                    Text(
                        stringResource(R.string.home_preset_dialog_empty),
                        color = White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        presets.forEach { preset ->
                            val isSelected =
                                preset.tag == currentTag && preset.durationMinutes == currentMinutes
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPresetSelect(preset) },
                                label = {
                                    Text(
                                        text = "${preset.tag} (${preset.durationMinutes}m)",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FireOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = FireOrange,
                                    containerColor = SurfaceHighlight.copy(alpha = 0.1f),
                                    labelColor = White,
                                ),
                                border = if (isSelected) BorderStroke(1.dp, FireOrange) else null,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.common_confirm),
                    color = FireOrange,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
    )

    if (showCustomEdit) {
        com.japygo.modakmodak.ui.settings.TimerPresetDialog(
            title = stringResource(R.string.home_custom_timer_title),
            initialTag = currentTag,
            initialMinutes = currentMinutes,
            onDismiss = { showCustomEdit = false },
            onConfirm = { tag, minutes ->
                onCustomConfirm(tag, minutes)
                showCustomEdit = false
            },
        )
    }
}

@Composable
fun HomeTopBar(navController: NavController, coins: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Coin chip moved to left
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20))
                .background(SurfaceHighlight.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = FireOrange,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$coins",
                color = FireOrange,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// HomeBottomBar component has been moved to ModakBottomBar.kt

/**
 * Returns the fire color based on the user's level
 * Level progression:
 * 1-5: Orange (beginner)
 * 6-10: Red-Orange (intermediate)
 * 11-15: Red (advanced)
 * 16-20: Purple-Red (expert)
 * 21+: Blue-Purple (master)
 */
private fun getFireColorByLevel(level: Int): Color {
    return when (level) {
        in 1..5 -> Color(0xFFFF9500) // Orange
        in 6..10 -> Color(0xFFFF6B35) // Red-Orange
        in 11..15 -> Color(0xFFFF3B30) // Red
        in 16..20 -> Color(0xFFFF2D92) // Purple-Red
        else -> Color(0xFFBF5AF2) // Blue-Purple (21+)
    }
}
