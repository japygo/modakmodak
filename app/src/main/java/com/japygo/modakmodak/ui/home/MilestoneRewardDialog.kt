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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment

@Composable
fun MilestoneRewardDialog(
    streakDays: Int,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.default_modak) // Using level_up or specific confetti if available, fallback to default for now
    )

    // Reward calculated inside UI block for simplicity as it's just display now

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
                // Unified Reward Box
                val rewardAmount = when(streakDays) {
                    3 -> 100
                    7 -> 300
                    14 -> 700
                    21 -> 1200
                    30 -> 2000
                    50 -> 4000
                    100 -> 10000
                    365 -> 50000
                    else -> 0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceHighlight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = FireOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+$rewardAmount", color = White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
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
