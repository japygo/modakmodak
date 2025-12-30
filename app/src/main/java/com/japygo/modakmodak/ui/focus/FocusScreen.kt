package com.japygo.modakmodak.ui.focus

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.components.ModakCharacter
import com.japygo.modakmodak.ui.components.ModakGlowCharacter
import com.japygo.modakmodak.ui.theme.DeepNavy
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.White
import com.japygo.modakmodak.ui.theme.BackgroundDark

// Alias Primary to FireOrange for consistency with plan if needed, 
// or simpler: use FireOrange directly.
// The Plan said BackgroundDark (#231C0F).
import androidx.compose.ui.platform.LocalContext

@Composable
fun FocusScreen(
    navController: NavController,
    viewModel: FocusViewModel,
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isFocusing by viewModel.isFocusing.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val isBreakEnabled by viewModel.isBreakEnabled.collectAsState()
    val initialDuration by viewModel.initialDuration.collectAsState()
    val isScreenOnEnabled by viewModel.isScreenOnEnabled.collectAsState()
    val isHardcoreModeEnabled by viewModel.isHardcoreModeEnabled.collectAsState() // Note: This is Flow from VM, but session might have fixed mode. 
    // Ideally VM exposes "isSessionHardcore". But for now assume settings flow is close enough or add specific flow.
    // Let's rely on VM state if possible, but VM didn't expose "sessionHardcoreMode" as public StateFlow.
    // However, for the *Indicator*, the settings value is fine (usually won't change mid-session unless user hacks).

    val user by viewModel.user.collectAsState()
    
    // Parse user's fire color
    val fireColor = remember(user?.fireColor) {
        try {
            val hex = user?.fireColor ?: "#FFFF9500"
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            FireOrange
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isScreenOnEnabled && (isFocusing || isPaused)) {
        KeepScreenOn()
    }

    // Status bar and Navigation bar color adjustment handled by Theme (Transparent)



    var showExitDialog by remember { mutableStateOf(false) }

    // 시스템 뒤로가기 제어
    BackHandler(enabled = isFocusing) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color(0xFF2D2416),
            title = {
                Text(
                    stringResource(R.string.focus_exit_dialog_title),
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    stringResource(R.string.focus_exit_dialog_text),
                    color = White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.stopTimer()
                        navController.popBackStack()
                    },
                ) {
                    Text(
                        stringResource(R.string.common_stop),
                        color = FireOrange,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                ) {
                    Text(stringResource(R.string.common_continue), color = White.copy(alpha = 0.5f))
                }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }

    val sessionResult by viewModel.sessionResult.collectAsState()
    
    // Handle Finish
    LaunchedEffect(sessionState, sessionResult) {
        if (sessionState == 2 && sessionResult != null) {
            val result = sessionResult!!
            val duration = navController.currentBackStackEntry?.arguments?.getString("duration")?.toIntOrNull() ?: 25
            
            // Use ViewModel to decide path (Break -> Reward OR Reward)
            viewModel.navigateToNextParam(navController, result, duration)
        }
    }

    Scaffold(
        containerColor = DeepNavy,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Main Timer Section with Circle
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (initialDuration > 0) timeLeft.toFloat() / initialDuration.toFloat() else 0f
                
                // Circular Progress
                // Rotate to make it disappear clockwise (fill retracts to 12 o'clock)
                androidx.compose.material3.CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .size(320.dp)
                        .rotate(360f * (1f - progress)),
                    color = fireColor,
                    trackColor = White.copy(alpha = 0.1f),
                    strokeWidth = 12.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                )

                // Time Text inside Circle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime(timeLeft),
                        color = if (isPaused) White.copy(alpha = 0.5f) else White,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2).sp,
                    )
                    

                }
            }

            // Bottom section for information and button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // Mid section: Tag and Status Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val currentTag = remember {
                         val raw = navController.currentBackStackEntry?.arguments?.getString("tag")
                         if (raw != null) Uri.decode(raw) else null
                    } ?: stringResource(R.string.focus_default_tag)
                    
                    Surface(
                        color = SurfaceHighlight.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = FireOrange.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                currentTag,
                                color = White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (isFocusing || sessionState == 3) stringResource(R.string.focusing_text) else stringResource(R.string.focus_paused_text),
                        color = FireOrange,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                    )
                }

                // Spacer to push button to bottom
                Spacer(modifier = Modifier.weight(1f))

                if (isPaused) {
                    // Resume Button (Simple Click)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(FireOrange)
                            .clickable { viewModel.resumeTimer() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.common_continue), // "Continue" or "Resume"
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    // Give Up Button (Hold)
                    var isHolding by remember { mutableStateOf(false) }
                    val progress by animateFloatAsState(
                        targetValue = if (isHolding) 1f else 0f,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = if (isHolding) 3000 else 300),
                        label = "GiveUpProgress",
                        finishedListener = {
                            if (isHolding) {
                                viewModel.stopTimer()
                                navController.popBackStack()
                            }
                        },
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF2D2416))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHolding = true
                                        try {
                                            awaitRelease()
                                        } finally {
                                            isHolding = false
                                        }
                                    },
                                )
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(FireOrange.copy(alpha = 0.5f)),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = White.copy(alpha = 0.5f),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.focus_give_up_button),
                                color = White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                
                // Bottom margin - Increased to move button up
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

    val failureReason by viewModel.failureReason.collectAsState()

    // ... (rest of code)
    
        // Hardcore Failure Overlay
        if (sessionState == 3 && isHardcoreModeEnabled && failureReason == FailureReason.BACKGROUND) {
            val progress = if (initialDuration > 0) timeLeft.toFloat() / initialDuration.toFloat() else 0f
            val elapsedRatio = 1f - progress
            
            val failMessage = when {
                elapsedRatio < 0.3f -> stringResource(R.string.focus_fail_msg_short)
                elapsedRatio < 0.7f -> stringResource(R.string.focus_fail_msg_medium)
                else -> stringResource(R.string.focus_fail_msg_long)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {}, // Block interaction
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.focus_fail_title),
                        color = Color(0xFFFF3B30),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        failMessage,
                        color = White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp),
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // MainAction Button (Go Back) - Text Only Style
                    TextButton(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = FireOrange
                        )
                    ) {
                        Text(
                            stringResource(R.string.focus_fail_button_back), 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
