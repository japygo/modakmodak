package com.japygo.modakmodak.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White

@Composable
fun MilestoneRewardDialog(
    streakDays: Int,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.default_modak) // Using level_up or specific confetti if available, fallback to default for now
    )

    // Calculate Reward based on day
    val (coinReward, expReward) = when (streakDays) {
        3 -> 100 to 50
        7 -> 300 to 150
        14 -> 700 to 350
        21 -> 1200 to 600
        30 -> 2000 to 1000
        50 -> 4000 to 2000
        100 -> 10000 to 5000
        365 -> 50000 to 25000
        else -> 0 to 0
    }

    // Select dynamic message
    val messageResId = when (streakDays) {
        3 -> R.string.bonus_msg_3
        7 -> R.string.bonus_msg_7
        14 -> R.string.bonus_msg_14
        21 -> R.string.bonus_msg_21
        30 -> R.string.bonus_msg_30
        50 -> R.string.bonus_msg_50
        100 -> R.string.bonus_msg_100
        365 -> R.string.bonus_msg_365
        else -> R.string.bonus_dialog_message
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Background Glow or Particles could go here
                
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animation Removed as requested
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.bonus_dialog_title, streakDays),
                        color = FireOrange,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = androidx.compose.ui.res.stringResource(messageResId),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Reward Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceHighlight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(androidx.compose.ui.res.stringResource(R.string.currency_unit), color = TextSecondary, fontSize = 12.sp)
                            Text("+$coinReward", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("EXP", color = TextSecondary, fontSize = 12.sp)
                            Text("+$expReward", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TextButton(
                        onClick = onClaim,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.bonus_dialog_claim),
                            color = FireOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
