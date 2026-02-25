package com.japygo.modakmodak.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
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
    private var isExternallyPaused = false
    private var isReceiverRegistered = false

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (isPlayingAll) {
                    isExternallyPaused = true
                    abandonAudioFocus()
                    pausePlayersOnly()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (isPlayingAll) {
                    isExternallyPaused = true
                    pausePlayersOnly()
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (isPlayingAll && isExternallyPaused) {
                    isExternallyPaused = false
                    playPlayersOnly()
                }
            }
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (isPlayingAll) {
                    isExternallyPaused = true
                    pausePlayersOnly()
                }
            }
        }
    }

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    fun initialize() {
        if (isReceiverRegistered) return
        try {
            context.registerReceiver(
                noisyReceiver, 
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            )
            isReceiverRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (audioFocusRequest == null) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
        }
        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
    }

    fun playAll() {
        isPlayingAll = true
        isExternallyPaused = false
        if (requestAudioFocus()) {
            playPlayersOnly()
        }
    }

    private fun playPlayersOnly() {
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
        isExternallyPaused = false
        abandonAudioFocus()
        pausePlayersOnly()
    }

    private fun pausePlayersOnly() {
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
        isExternallyPaused = false
        abandonAudioFocus()
        players.values.forEach {
            try {
                it.pause()
                it.seekTo(0)
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
                    // Do not handle AudioFocus per player, let AsmrManager handle it centrally.
                    it.setAudioAttributes(audioAttributes, false)
                    it.setHandleAudioBecomingNoisy(false) // We handle this manually now
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

                if (isPlayingAll && !isExternallyPaused && volume > 0f) {
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
                    if (isPlayingAll && !isExternallyPaused && !player.isPlaying) {
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
        isExternallyPaused = false
        abandonAudioFocus()
        players.values.forEach {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        players.clear()
        
        try {
            if (isReceiverRegistered) {
                context.unregisterReceiver(noisyReceiver)
                isReceiverRegistered = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // we keep the recorded variations and volumes so that if the user starts another focus session
        // the viewmodel will quickly push state, but usually the viewmodel re-triggers flows anyway.
    }

    private fun getResIdForVariation(type: SoundType, variation: Int): Int {
        val name = "${type.name.lowercase()}_v$variation"
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }
}
