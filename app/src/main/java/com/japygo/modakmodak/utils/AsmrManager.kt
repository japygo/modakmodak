package com.japygo.modakmodak.utils

import android.content.Context
import android.media.MediaPlayer
import com.japygo.modakmodak.R

enum class SoundType {
    FIRE, RAIN, CRICKETS, WIND, STREAM
}

class AsmrManager(private val context: Context) {
    private val players = mutableMapOf<SoundType, MediaPlayer>()
    
    // Track current variations and volumes
    private val currentVariations = mutableMapOf<SoundType, Int>()
    private val currentVolumes = mutableMapOf<SoundType, Float>()

    private var isPlayingAll = false

    fun initialize() {
        // No-op for now. 
        // Players are lazily loaded in setVariation to save memory instead of preloading all.
    }

    fun playAll() {
        isPlayingAll = true
        players.values.forEach { 
            try {
                if (!it.isPlaying) it.start()
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
                val player = MediaPlayer.create(context, resId)
                player.isLooping = true
                
                // Restore volume
                val volume = currentVolumes[type] ?: 0f
                player.setVolume(volume, volume)
                
                players[type] = player
                
                if (isPlayingAll) {
                    player.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVolume(type: SoundType, volume: Float) {
        currentVolumes[type] = volume
        try {
            players[type]?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        isPlayingAll = false
        players.values.forEach { 
            try {
                if (it.isPlaying) it.stop()
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
