package com.japygo.modakmodak.utils

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

enum class SoundType {
    FIRE, RAIN, CRICKETS, WIND, STREAM
}

class AsmrManager(private val context: Context) {
    private val players = mutableMapOf<SoundType, ExoPlayer>()

    private val currentVariations = mutableMapOf<SoundType, Int>()
    private val currentVolumes = mutableMapOf<SoundType, Float>()

    private var isPlayingAll = false

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    fun initialize() {
        // No-op for now. 
        // Players are lazily loaded in setVariation to save memory instead of preloading all.
    }

    fun playAll() {
        isPlayingAll = true
        players.forEach { (type, player) ->
            try {
                val volume = currentVolumes[type] ?: 0f
                if (volume > 0f && !player.isPlaying) {
                    player.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pauseAll() {
        isPlayingAll = false
        players.values.forEach {
            try {
                if (it.isPlaying) it.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAll() {
        isPlayingAll = false
        players.values.forEach {
            try {
                if (it.isPlaying) {
                    it.pause()
                    it.seekTo(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVariation(type: SoundType, variation: Int) {
        if (currentVariations[type] == variation && players.containsKey(type)) {
            return // No change
        }

        currentVariations[type] = variation

        // Release old player if exists
        try {
            players[type]?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        players.remove(type)

        if (variation == 0) {
            return
        }

        val resId = getResIdForVariation(type, variation)
        if (resId != 0) {
            try {
                val player = ExoPlayer.Builder(context).build().also {
                    it.setAudioAttributes(audioAttributes, true)
                    it.setHandleAudioBecomingNoisy(true)
                }
                player.repeatMode = Player.REPEAT_MODE_ONE // Gapless loop natively

                val uri = "android.resource://${context.packageName}/$resId"
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
                player.prepare()

                // Restore volume
                val volume = currentVolumes[type] ?: 0f
                player.volume = volume

                players[type] = player

                if (isPlayingAll && volume > 0f) {
                    player.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVolume(type: SoundType, volume: Float) {
        currentVolumes[type] = volume
        try {
            val player = players[type]
            if (player != null) {
                player.volume = volume
                if (volume == 0f) {
                    if (player.isPlaying) {
                        player.pause()
                    }
                } else {
                    if (isPlayingAll && !player.isPlaying) {
                        player.play()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        isPlayingAll = false
        players.values.forEach {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        players.clear()
        // we keep the recorded variations and volumes so that if the user starts another focus session
        // the viewmodel will quickly push state, but usually the viewmodel re-triggers flows anyway.
    }

    private fun getResIdForVariation(type: SoundType, variation: Int): Int {
        val name = "${type.name.lowercase()}_v$variation"
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }
}
