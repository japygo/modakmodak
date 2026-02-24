package com.japygo.modakmodak.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.DeepNavy
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmrMixerBottomSheet(
    viewModel: FocusViewModel,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = DeepNavy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = White.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                stringResource(R.string.asmr_mixer_title),
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Fire
            val fireVar by viewModel.fireVariation.collectAsState()
            val fireVol by viewModel.fireVolume.collectAsState()
            SoundMixerRow(
                title = stringResource(R.string.asmr_sound_fire),
                currentVariation = fireVar,
                currentVolume = fireVol,
                onVariationChange = { viewModel.updateFireVariation(it) },
                onVolumeChange = { viewModel.updateFireVolume(it) }
            )

            // Rain
            val rainVar by viewModel.rainVariation.collectAsState()
            val rainVol by viewModel.rainVolume.collectAsState()
            SoundMixerRow(
                title = stringResource(R.string.asmr_sound_rain),
                currentVariation = rainVar,
                currentVolume = rainVol,
                onVariationChange = { viewModel.updateRainVariation(it) },
                onVolumeChange = { viewModel.updateRainVolume(it) }
            )

            // Crickets
            val cricketsVar by viewModel.cricketsVariation.collectAsState()
            val cricketsVol by viewModel.cricketsVolume.collectAsState()
            SoundMixerRow(
                title = stringResource(R.string.asmr_sound_crickets),
                currentVariation = cricketsVar,
                currentVolume = cricketsVol,
                onVariationChange = { viewModel.updateCricketsVariation(it) },
                onVolumeChange = { viewModel.updateCricketsVolume(it) }
            )

            // Wind
            val windVar by viewModel.windVariation.collectAsState()
            val windVol by viewModel.windVolume.collectAsState()
            SoundMixerRow(
                title = stringResource(R.string.asmr_sound_wind),
                currentVariation = windVar,
                currentVolume = windVol,
                onVariationChange = { viewModel.updateWindVariation(it) },
                onVolumeChange = { viewModel.updateWindVolume(it) }
            )

            // Stream
            val streamVar by viewModel.streamVariation.collectAsState()
            val streamVol by viewModel.streamVolume.collectAsState()
            SoundMixerRow(
                title = stringResource(R.string.asmr_sound_stream),
                currentVariation = streamVar,
                currentVolume = streamVol,
                onVariationChange = { viewModel.updateStreamVariation(it) },
                onVolumeChange = { viewModel.updateStreamVolume(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SoundMixerRow(
    title: String,
    currentVariation: Int,
    currentVolume: Float,
    onVariationChange: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                VariationButton(text = stringResource(R.string.asmr_variation_off), isSelected = currentVariation == 0) {
                    onVariationChange(0)
                }
                for (i in 1..4) {
                    VariationButton(text = "$i", isSelected = currentVariation == i) {
                        onVariationChange(i)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentVolume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            enabled = currentVariation != 0, // Disable slider if off
            colors = SliderDefaults.colors(
                thumbColor = if (currentVariation == 0) Color.Gray else FireOrange,
                activeTrackColor = if (currentVariation == 0) Color.Gray.copy(alpha = 0.5f) else FireOrange,
                inactiveTrackColor = White.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
    }
}

@Composable
fun VariationButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) FireOrange else Color.White.copy(alpha = 0.1f)
    val textColor = if (isSelected) White else White.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
