package com.japygo.modakmodak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.ads.nativead.NativeAd
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White
import com.japygo.modakmodak.utils.AdMobManager

@Composable
fun ExitConfirmationDialog(
    nativeAd: NativeAd?,
    onDismiss: () -> Unit,
    onExit: () -> Unit,
    onReview: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.exit_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Native Ad Area
                if (nativeAd != null) {
                    AdMobManager.NativeAdView(
                        nativeAd = nativeAd,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                     Text(
                        text = androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.exit_dialog_ad_loading),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons: Review, Go Back, Exit (All in one row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     TextButton(onClick = onExit) {
                         Text(
                             text = androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.exit_dialog_exit),
                             color = Color.Red.copy(alpha = 0.8f),
                             fontWeight = FontWeight.Bold
                         )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.exit_dialog_go_back), 
                            color = TextSecondary
                        )
                    }
                    TextButton(onClick = onReview) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.exit_dialog_review), 
                            color = FireOrange
                        )
                    }
                }
            }
        }
    }
}
