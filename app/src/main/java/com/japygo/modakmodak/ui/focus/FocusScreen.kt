package com.japygo.modakmodak.ui.focus

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.DeepNavy
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.White

// Alias Primary to FireOrange for consistency with plan if needed, 
// or simpler: use FireOrange directly.
// The Plan said BackgroundDark (#231C0F).

@Composable
fun FocusScreen(
    navController: NavController,
    viewModel: FocusViewModel,
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isFocusing by viewModel.isFocusing.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val isBreakEnabled by viewModel.isBreakEnabled.collectAsState()

    // 네비게이션 인자로 전달받은 시간은 NavGraph에서 viewModel.startTimer(duration)을 통해 시작됩니다.
    // 기존의 중복된 auto-start 로직은 삭제합니다.

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

    // Handle Finish
    LaunchedEffect(sessionState) {
        if (sessionState == 2) {
            // Success -> Go to Break Screen or Home based on settings
            if (isBreakEnabled) {
                // Get current set duration from NavGraph or ViewModel
                val duration = navController.currentBackStackEntry?.arguments?.getString("duration")
                    ?.toIntOrNull() ?: 25
                navController.navigate("break/$duration") {
                    popUpTo("home") // Clear focus screen from stack
                }
            } else {
                navController.popBackStack()
            }
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
            Spacer(modifier = Modifier.height(60.dp))

            // Tag
            Surface(
                color = Color(0xFF3A2E1E),
                shape = RoundedCornerShape(50),
                border = null, // Border logic if needed
                modifier = Modifier.padding(bottom = 30.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val rawTag = navController.currentBackStackEntry?.arguments?.getString("tag")
                        ?: stringResource(R.string.focus_default_tag)
                    val currentTag = Uri.decode(rawTag)
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = FireOrange,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        currentTag,
                        color = White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Fire Visual Placeholder (Bonfire)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(FireOrange.copy(alpha = 0.2f), Color.Transparent),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Inner Core - Modak Character
                Image(
                    painter = painterResource(id = R.drawable.default_modak),
                    contentDescription = "Focus Modak",
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (isFocusing) 1.05f else 1.0f), // Breathing effect
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Timer
            Text(
                text = formatTime(timeLeft),
                color = White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp,
            )
            Text(
                text = if (isFocusing) stringResource(R.string.focusing_text) else stringResource(R.string.focus_paused_text),
                color = FireOrange,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Give Up Button (Long Press with Progress)
            var isHolding by remember { mutableStateOf(false) }
            val progress by animateFloatAsState(
                targetValue = if (isHolding) 1f else 0f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = if (isHolding) 3000 else 300),
                label = "GiveUpProgress",
                finishedListener = {
                    if (isHolding) {
                        // Finished while holding -> Success
                        viewModel.stopTimer()
                        navController.popBackStack()
                    }
                },
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 50.dp)
                    .width(280.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
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
                // Progress Fill Layer
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress) // Fill based on progress
                        .background(FireOrange.copy(alpha = 0.5f)),
                )

                // Button Content
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
