package com.japygo.modakmodak.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.White
import com.japygo.modakmodak.utils.LevelUtils

@Composable
fun ModakTopBar(
    coins: Int,
    level: Int,
    exp: Int,
    fireColorHex: String = "#FFFF9500",
    showLevel: Boolean = true
) {
    val fireColor = try {
        Color(android.graphics.Color.parseColor(fireColorHex))
    } catch (e: Exception) {
        Color(0xFFFF9500)
    }
    val progress = LevelUtils.getLevelProgress(exp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: Coin chip
        ModakCoinBadge(coins = coins)

        // Right: Level and Exp Bar
        if (showLevel) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${stringResource(R.string.home_level_prefix)}$level",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Custom thin progress bar
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SurfaceHighlight.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(fireColor)
                    )
                }
            }
        } else {
            // Spacer to keep the coin badge on the left when level is hidden
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}
