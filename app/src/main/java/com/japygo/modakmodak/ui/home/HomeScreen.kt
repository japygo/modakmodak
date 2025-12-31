package com.japygo.modakmodak.ui.home

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.BuildConfig
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.components.ModakBottomBar
import com.japygo.modakmodak.ui.components.ModakCharacter
import com.japygo.modakmodak.ui.components.ModakGlowCharacter
import com.japygo.modakmodak.ui.components.ModakCoinBadge
import com.japygo.modakmodak.ui.components.ModakTopBar
import com.japygo.modakmodak.ui.splash.SplashScreen
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.Orange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White
import com.japygo.modakmodak.utils.LevelUtils

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
) {
    val user by viewModel.user.collectAsState()
    val timerPresets by viewModel.timerPresets.collectAsState()
    val sessionTag by viewModel.sessionTag.collectAsState()
    val sessionDuration by viewModel.sessionDurationMinutes.collectAsState()

    // Preload Lottie Composition
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.default_modak)
    )

    // Splash Screen State
    var minTimeElapsed by remember { mutableStateOf(false) }
    // Initialize based on whether splash was already shown this session
    var isSplashActive by remember { mutableStateOf(!viewModel.isSplashAlreadyShown) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        // Pre-load ads for smoother experience
        com.japygo.modakmodak.utils.AdMobManager.loadRewardedAd(context, com.japygo.modakmodak.utils.AdMobManager.AdType.FOCUS)
        com.japygo.modakmodak.utils.AdMobManager.loadRewardedAd(context, com.japygo.modakmodak.utils.AdMobManager.AdType.SHOP)

        if (!viewModel.isSplashAlreadyShown) {
            kotlinx.coroutines.delay(2000) // 2 seconds delay
            minTimeElapsed = true
        } else {
            minTimeElapsed = true // Already shown, condition met immediately
        }
    }



    // Hide splash when User is loaded AND Lottie is ready AND minimum time elapsed
    LaunchedEffect(user, lottieComposition, minTimeElapsed) {
        if ((user != null) && (lottieComposition != null) && minTimeElapsed) {
            if (isSplashActive) {
                 isSplashActive = false
                 viewModel.isSplashAlreadyShown = true
            }
        }
    }

    var showPresetSelector by remember { mutableStateOf(false) }

    // Get fire color from user state
    val fireColor = remember(user?.fireColor) {
        try {
            Color(android.graphics.Color.parseColor(user?.fireColor ?: "#FFFF9500"))
        } catch (e: Exception) {
            FireOrange
        }
    }

    val currentLevel = user?.fireLevel ?: 1

    // Full screen container
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Background Image Layer (The "Wall")

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.home_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            // Dimming Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            // Top Gradient (Black to Transparent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Bottom Gradient (Transparent to Black)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BackgroundDark
                            )
                        )
                    )
            )
        }

        // 2. Scaffold (The "Furniture") - Transparent to show background
        Scaffold(
            modifier = Modifier.navigationBarsPadding(), // Only pad bottom nav bar from system nav
            containerColor = Color.Transparent,
            topBar = { 
                ModakTopBar(
                    coins = user?.currentCoin ?: 0,
                    level = currentLevel,
                    exp = user?.fireExp ?: 0,
                    fireColorHex = user?.fireColor ?: "#FFFF9500",
                    showLevel = true,
                    modifier = Modifier.statusBarsPadding() // Pad top bar from status bar
                ) 
            },
            bottomBar = { ModakBottomBar(navController, "home") },
                ) { innerPadding ->
            var showDebugDialog by remember { mutableStateOf(false) }
            val unclaimedMilestones by viewModel.unclaimedMilestones.collectAsState()
            var showRewardDialog by remember { mutableStateOf(false) }

            // Debug Dialog
            if (showDebugDialog) {
                AlertDialog(
                    onDismissRequest = { showDebugDialog = false },
                    title = { Text("DEBUG: Controller", color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Level: $currentLevel, Exp: ${user?.fireExp}", color = Color.Gray)
                            Text("Streak: ${user?.streakDays}, Last: ${user?.lastStudyDate}", color = Color.Gray)
                            
                            Text("Exp Control:", color = FireOrange, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.debugAddExp(100) }) { Text("+100") }
                                Button(onClick = { viewModel.debugAddExp(500) }) { Text("+500") }
                            }

                            Text("Economy:", color = FireOrange, fontWeight = FontWeight.Bold)
                            Button(onClick = { viewModel.debugAddCoins(1000) }) { Text("Add 1000 Modak") }
                            
                            Text("Time Travel (Verify Penalty):", color = FireOrange, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.debugSetLastStudyDate(1) }) { Text("-1d") }
                                Button(onClick = { viewModel.debugSetLastStudyDate(8) }) { Text("-8d (P)") }
                                Button(onClick = { viewModel.debugSetLastStudyDate(30) }) { Text("-30d") }
                            }



                            Text("Set Streak (Pre-Bonus):", color = FireOrange, fontWeight = FontWeight.Bold)
                            // Using FlowRow for multiple buttons
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { viewModel.debugSetStreak(2) }) { Text("3 Days") }
                                Button(onClick = { viewModel.debugSetStreak(6) }) { Text("7 Days") }
                                Button(onClick = { viewModel.debugSetStreak(13) }) { Text("14 Days") }
                                Button(onClick = { viewModel.debugSetStreak(20) }) { Text("21 Days") }
                                Button(onClick = { viewModel.debugSetStreak(29) }) { Text("30 Days") }
                                Button(onClick = { viewModel.debugSetStreak(49) }) { Text("50 Days") }
                                Button(onClick = { viewModel.debugSetStreak(99) }) { Text("100 Days") }
                                Button(onClick = { viewModel.debugSetStreak(364) }) { Text("365 Days") }
                            }
                            Button(
                                onClick = { viewModel.debugSimulateSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) { 
                                Text("🔥 Simulate Session (Finish Now)") 
                            }

                            Text("Notification Test:", color = FireOrange, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.debugShowDailyReminder() }) { Text("Daily") }
                                Button(onClick = { viewModel.debugShowComebackNotification(3) }) { Text("3d") }
                                Button(onClick = { viewModel.debugShowComebackNotification(7) }) { Text("7d") }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDebugDialog = false }) { Text("Close") }
                    },
                    containerColor = SurfaceDark
                )
            }

            // Milestone Reward Dialog
            if (showRewardDialog && unclaimedMilestones.isNotEmpty()) {
                val milestoneToClaim = unclaimedMilestones.first()
                MilestoneRewardDialog(
                    streakDays = milestoneToClaim,
                    onClaim = {
                        viewModel.claimMilestone(milestoneToClaim)
                        // If multiple, dialog stays open for next? Or closes? 
                        // Let's close and let user click again if multiple.
                        showRewardDialog = false 
                    },
                    onDismiss = { showRewardDialog = false }
                )
            }

            // 3. Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // Content respects bars
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top section for character (aligned to where the clearing is)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            if (BuildConfig.DEBUG) {
                                detectTapGestures(onLongPress = { showDebugDialog = true })
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    // Adjust padding/offset to place character exactly in the clearing
                    // The clearing is roughly in the center but slightly lower in visual weight
                    Box(
                        modifier = Modifier.padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ModakGlowCharacter(
                            level = currentLevel,
                            exp = user?.fireExp ?: 0,
                            fireColor = fireColor,
                            lottieComposition = lottieComposition
                        )
                        
                        // Streak Text Removed as requested
                    }
                    
                    // Milestone Badge (If available)
                    if (unclaimedMilestones.isNotEmpty()) {
                         Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 24.dp)
                                .clickable { showRewardDialog = true }
                                .background(FireOrange.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .border(1.dp, FireOrange, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = FireOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.home_bonus_badge),
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Bottom section for timer and button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
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
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.home_timer_adjust_hint),
                        color = FireOrange,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { showPresetSelector = true },
                    )

                    Spacer(modifier = Modifier.height(60.dp))

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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Splash Screen Overlay
        AnimatedVisibility(
            visible = isSplashActive,
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.fillMaxSize()
        ) {
            SplashScreen()
        }
    }

    if (showPresetSelector) {
        val selectedPreset by viewModel.selectedPreset.collectAsState()
        // 커스텀 다이얼로그 초기값은 마지막 커스텀 설정값을 사용
        val lastCustomTag by viewModel.lastCustomTag.collectAsState()
        val lastCustomMinutes by viewModel.lastCustomDurationMinutes.collectAsState()
        
        PresetSelectionDialog(
            presets = timerPresets,
            selectedPreset = selectedPreset,
            currentTag = lastCustomTag, // 현재 세션값이 아닌, 마지막 커스텀 값을 전달
            currentMinutes = lastCustomMinutes, // 현재 세션값이 아닌, 마지막 커스텀 값을 전달
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
    selectedPreset: com.japygo.modakmodak.data.entity.TimerPreset?,
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
                val isCustomSelected = selectedPreset == null
                Surface(
                    onClick = { showCustomEdit = true },
                    color = if (isCustomSelected) FireOrange.copy(alpha = 0.15f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    border = if (isCustomSelected) BorderStroke(1.dp, FireOrange) else BorderStroke(
                        1.dp,
                        SurfaceHighlight.copy(alpha = 0.4f)
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.home_preset_dialog_custom),
                        color = if (isCustomSelected) FireOrange else TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Normal
                    )
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            presets.forEach { preset ->
                                val isSelected = selectedPreset?.id == preset.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onPresetSelect(preset) },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = preset.tag,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "(${preset.durationMinutes}m)",
                                                color = if (isSelected) FireOrange else TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FireOrange.copy(alpha = 0.2f),
                                        selectedLabelColor = FireOrange,
                                        containerColor = SurfaceHighlight.copy(alpha = 0.1f),
                                        labelColor = White,
                                    ),
                                    border = if (isSelected) BorderStroke(1.dp, FireOrange) else BorderStroke(
                                        1.dp,
                                        SurfaceHighlight.copy(alpha = 0.4f)
                                    ),
                                )
                            }
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
