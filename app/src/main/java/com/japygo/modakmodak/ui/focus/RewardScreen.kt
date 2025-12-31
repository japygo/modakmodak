package com.japygo.modakmodak.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayCircle

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
    var displayedCoins by remember { mutableStateOf(earnedCoins) }

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
                    // Animated Coin Text
                    val animatedCoins by animateIntAsState(
                        targetValue = displayedCoins,
                        animationSpec = tween(durationMillis = 1000),
                        label = "coinAnimation"
                    )

                    Text(
                        text = "+$animatedCoins", // or earnedExp, they are same
                        color = White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Confirm/Continue Button
                    androidx.compose.material3.Button(
                        onClick = {
                            if (isBreakEnabled) {
                                navController.navigate("break/$duration/$earnedCoins/$earnedExp/$streakDays") {
                                    popUpTo("home")
                                }
                            } else {
                                navController.popBackStack("home", inclusive = false)
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SurfaceHighlight.copy(alpha = 0.2f)
                        )
                    ) {
                         Text(
                            text = stringResource(R.string.reward_button_receive),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    // AdMob Double Reward Button
                    var isAdWatched by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val activity = context.findActivity()
                    val scope = androidx.compose.runtime.rememberCoroutineScope()

                    // BackHandler to prevent accidental exit
                    BackHandler {
                        // Do nothing to force user to click confirm? 
                        // Or show toast "Please click Confirm"?
                        // User request: "확인을 눌러야 홈으로 하게 수정해줘" -> Block back button.
                    }

                    val isAdLoaded by viewModel.isAdLoaded.collectAsState()

                    if (!isAdWatched && activity != null) {
                        androidx.compose.material3.Button(
                            onClick = {
                                 if (isAdLoaded) {
                                     com.japygo.modakmodak.utils.AdMobManager.showRewardedAd(
                                        activity = activity!!,
                                        type = com.japygo.modakmodak.utils.AdMobManager.AdType.FOCUS,
                                        onUserEarnedReward = { 
                                            viewModel.doubleReward(earnedCoins)
                                            isAdWatched = true
                                            displayedCoins *= 2 // Update UI instantly
                                        },
                                        onAdDismissed = { },
                                        onAdFailed = {
                                            android.widget.Toast.makeText(context, context.getString(R.string.ad_load_failed), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                     )
                                 } else {
                                     android.widget.Toast.makeText(context, context.getString(R.string.ad_shop_ads_loading), android.widget.Toast.LENGTH_SHORT).show()
                                 }
                            },
                            enabled = true, // Always enabled, show toast if not ready
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = FireOrange
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp) 
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayCircle,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.ad_reward_double_coins_btn),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
