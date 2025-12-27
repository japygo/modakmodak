package com.japygo.modakmodak.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.DeepNavy
import com.japygo.modakmodak.ui.theme.FireOrange
import kotlinx.coroutines.launch

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // App Icon
            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier.size(108.dp),
                factory = { context ->
                    android.widget.ImageView(context).apply {
                        setImageResource(R.mipmap.ic_launcher_round)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Text
            Text(
                text = stringResource(id = R.string.app_name),
                color = FireOrange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
