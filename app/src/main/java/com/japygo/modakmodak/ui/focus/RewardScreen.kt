package com.japygo.modakmodak.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White
import kotlinx.coroutines.delay
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment

@Composable
fun RewardScreen(
    navController: NavController,
    viewModel: FocusViewModel, 
    earnedCoins: Int,
    earnedExp: Int,
    duration: Int,
    streakDays: Int,
    isBreakEnabled: Boolean
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500)
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(500)
    )

    // Full screen dimmed background to simulate Dialog overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        // Dialog Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .alpha(alpha)
                .scale(scale)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.reward_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = FireOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                val context = androidx.compose.ui.platform.LocalContext.current
                val randomMessage = remember {
                    val messages = context.resources.getStringArray(R.array.reward_messages)
                    if (messages.isNotEmpty()) {
                        messages.random()
                    } else {
                        context.getString(R.string.reward_title)
                    }
                }

                Text(
                    text = randomMessage,
                    fontSize = 16.sp,
                    color = White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Reward Box (Styled like MilestoneRewardDialog)
                // Unified Reward Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceHighlight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unified Coin/Exp
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = FireOrange,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "+$earnedCoins", // or earnedExp, they are same
                        color = White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = {
                        if (isBreakEnabled) {
                            // Go to Break Screen (Loop: Reward -> Break -> Home)
                            navController.navigate("break/$duration/$earnedCoins/$earnedExp/$streakDays") {
                                // Pop Reward so back press doesn't come here? Or Keep it?
                                // If we pop "reward", then Break is top.
                                popUpTo("home") // Clear stack down to Home
                            }
                        } else {
                            // Go directly to Home (Loop: Reward -> Home)
                            navController.popBackStack("home", inclusive = false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(if (isBreakEnabled) R.string.common_continue else R.string.common_confirm),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FireOrange,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

