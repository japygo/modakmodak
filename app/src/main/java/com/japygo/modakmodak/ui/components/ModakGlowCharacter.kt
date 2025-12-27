package com.japygo.modakmodak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.japygo.modakmodak.utils.LevelUtils

/**
 * Configuration for the glow effect based on fire level.
 */
data class GlowConfig(
    val layerCount: Int,        // Number of gradient layers
    val glowMultiplier: Float,  // Multiplier for the glow size
    val gradientStops: Int,     // Number of color stops in each layer's gradient
    val baseAlpha: Float        // Base transparency for the glow
)

/**
 * Returns the specific glow configuration for a given fire level.
 * Optimized for "subtle and soft" firelight feel.
 */
private fun getGlowConfigForLevel(level: Int): GlowConfig {
    return when (level) {
        1 -> GlowConfig(layerCount = 1, glowMultiplier = 1.4f, gradientStops = 5, baseAlpha = 0.22f)
        2 -> GlowConfig(layerCount = 2, glowMultiplier = 1.7f, gradientStops = 5, baseAlpha = 0.25f)
        3 -> GlowConfig(layerCount = 2, glowMultiplier = 2.0f, gradientStops = 5, baseAlpha = 0.28f)
        4 -> GlowConfig(layerCount = 3, glowMultiplier = 2.4f, gradientStops = 5, baseAlpha = 0.30f)
        5 -> GlowConfig(layerCount = 3, glowMultiplier = 2.8f, gradientStops = 5, baseAlpha = 0.32f)
        else -> GlowConfig(layerCount = 1, glowMultiplier = 1.4f, gradientStops = 5, baseAlpha = 0.22f)
    }
}

/**
 * Dynamically creates a list of colors for a radial gradient.
 * Optimized for extremely soft and silverish spreading.
 */
private fun createGradientColors(
    fireColor: Color,
    baseAlpha: Float
): List<Color> {
    return listOf(
        fireColor.copy(alpha = baseAlpha),
        fireColor.copy(alpha = baseAlpha * 0.4f),
        fireColor.copy(alpha = baseAlpha * 0.15f),
        fireColor.copy(alpha = baseAlpha * 0.03f),
        Color.Transparent
    )
}

/**
 * A combined component that renders the Modak character with a level-based glow effect.
 */
@Composable
fun ModakGlowCharacter(
    level: Int,
    exp: Int,
    fireColor: Color,
    modifier: Modifier = Modifier,
    extraScale: Float = 1.0f,
    baseCharacterSize: Dp = 240.dp
) {
    val currentLevel = level.coerceIn(1, 5)
    val levelProgress = remember(exp) { LevelUtils.getLevelProgress(exp) }
    val glowConfig = remember(currentLevel) { getGlowConfigForLevel(currentLevel) }

    // Calculate scales: Stable base scale for character size growth
    val baseScale = 0.5f + (currentLevel * 0.08f) + (levelProgress * 0.08f)
    val finalScale = baseScale * extraScale
    
    // Total glow size fixed at its clear, steady state
    val glowSize = baseCharacterSize * baseScale * glowConfig.glowMultiplier

    Box(
        modifier = modifier.size(glowSize),
        contentAlignment = Alignment.Center,
    ) {
        repeat(glowConfig.layerCount) { index ->
            val layerScale = 1.0f - (index * 0.15f)
            // Use stable base alpha without pulsing
            val layerAlpha = glowConfig.baseAlpha / glowConfig.layerCount
            
            val gradientColors = createGradientColors(
                fireColor = fireColor,
                baseAlpha = layerAlpha
            )
            
            Box(
                modifier = Modifier
                    .size(glowSize * layerScale)
                    .background(
                        brush = Brush.radialGradient(colors = gradientColors)
                    )
            )
        }

        ModakCharacter(
            flameColor = fireColor,
            scale = finalScale,
            clipToBounds = false,
            modifier = Modifier
                .size(baseCharacterSize)
                .padding(bottom = 20.dp),
        )
    }
}
