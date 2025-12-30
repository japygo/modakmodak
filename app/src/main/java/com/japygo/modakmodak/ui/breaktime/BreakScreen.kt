package com.japygo.modakmodak.ui.breaktime

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.Orange
import com.japygo.modakmodak.ui.theme.White

@Composable
fun BreakScreen(
    navController: NavController,
    viewModel: BreakViewModel,
    earnedCoins: Int,
    earnedExp: Int,
    streakDays: Int
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val randomMessage by viewModel.randomMessage.collectAsState()
    val isBreakTimerEnabled by viewModel.isBreakTimerEnabled.collectAsState()
    val isScreenOnEnabled by viewModel.isScreenOnEnabled.collectAsState()
    
    val studyDuration = remember {
         navController.currentBackStackEntry?.arguments?.getString("studyDuration")?.toIntOrNull() ?: 25
    }

    if (isScreenOnEnabled) {
        KeepScreenOn()
    }

    // Start timer on entry
    LaunchedEffect(Unit) {
        viewModel.startBreak()
    }

    // Auto-navigate to Reward Screen on finish
    LaunchedEffect(isFinished) {
        if (isFinished) {
            // End of Loop: Break -> Home
            navController.popBackStack("home", inclusive = false)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Coffee,
                contentDescription = null,
                tint = FireOrange,
                modifier = Modifier.size(64.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.break_title),
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(randomMessage),
                color = White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 32.dp),
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Timer Display or Break Text
            if (isBreakTimerEnabled) {
                val m = timeLeft / 60
                val s = timeLeft % 60
                Text(
                    text = "%02d:%02d".format(m, s),
                    color = White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = "RELAX",
                    color = White.copy(alpha = 0.1f),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Skip Button (Go to Reward)
            Button(
                onClick = {
                    // End of Loop: Break -> Home
                    navController.popBackStack("home", inclusive = false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Orange.copy(alpha = 0.3f),
                                FireOrange.copy(alpha = 0.3f),
                            ),
                        ),
                    ),
            ) {
                Text(
                    text = stringResource(R.string.break_skip_button),
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
