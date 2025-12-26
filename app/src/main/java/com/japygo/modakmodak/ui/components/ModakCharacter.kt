package com.japygo.modakmodak.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.value.ScaleXY
import com.japygo.modakmodak.R

/**
 * Modak character with customizable flame and face colors
 *
 * @param modifier Modifier for the animation
 * @param flameColor Color for the outer flames (default: original color from Lottie file)
 * @param faceColor Color for the face/body (inner_flame layers) (default: original color)
 * @param eyeColor Color for the eyes (default: original color)
 * @param scale Scale factor for the entire character size (default: 1.0f = original size)
 * @param clipToBounds Whether to clip the animation to its bounds (default: false for larger flames)
 */
@Composable
fun ModakCharacter(
    modifier: Modifier = Modifier,
    flameColor: Color? = null,
    faceColor: Color? = null,
    eyeColor: Color? = null,
    scale: Float = 1.0f,
    clipToBounds: Boolean = false,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.default_modak),
    )

    // Build dynamic properties list only for non-null colors
    val dynamicPropertiesList = buildList {
        // Flame colors (outer_flame layers)
        flameColor?.let { color ->
            val argb = color.toArgb()
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "outer_flame_1", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "outer_flame_2", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "outer_flame_3", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "outer_flame_4", "**", "fill_1")
                )
            )
        }

        // Face/body colors (inner_flame layers)
        faceColor?.let { color ->
            val argb = color.toArgb()
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "inner_flame_1", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "inner_flame_2", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "inner_flame_3", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "inner_flame_4", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("body", "inner_flame_5", "**", "fill_1")
                )
            )
        }

        // Eye colors
        eyeColor?.let { color ->
            val argb = color.toArgb()
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("left_eye", "**", "fill_1")
                )
            )
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.COLOR,
                    value = argb,
                    keyPath = arrayOf("right_eye", "**", "fill_1")
                )
            )
        }

        // Character scale (only if not default)
        // Scale the entire body composition which includes flames, face, and all elements
        if (scale != 1.0f) {
            val scaleXY = ScaleXY(scale, scale)
            add(
                rememberLottieDynamicProperty(
                    property = LottieProperty.TRANSFORM_SCALE,
                    value = scaleXY,
                    keyPath = arrayOf("body")
                )
            )
        }
    }

    val dynamicProperties = if (dynamicPropertiesList.isNotEmpty()) {
        rememberLottieDynamicProperties(*dynamicPropertiesList.toTypedArray())
    } else {
        null
    }

    val finalModifier = if (clipToBounds) {
        modifier.clipToBounds()
    } else {
        modifier
    }

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        dynamicProperties = dynamicProperties,
        clipToCompositionBounds = false,
        modifier = finalModifier,
    )
}
