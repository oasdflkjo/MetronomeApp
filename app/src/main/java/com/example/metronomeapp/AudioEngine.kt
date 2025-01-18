package com.example.metronomeapp

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.*

class AudioEngine(private val context: Context) {
    private var isPlaying = false
    private var volume = 1.0f
    private var bpm = 180
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        .build()

    private var clickSoundId: Int = 0

    init {
        clickSoundId = soundPool.load(context, R.raw.click, 1)
    }

    fun start() {
        if (!isPlaying) {
            isPlaying = true
            startMetronome()
        }
    }

    fun stop() {
        isPlaying = false
        job?.cancel()
        job = null
    }

    fun setBpm(newBpm: Int) {
        bpm = newBpm
        if (isPlaying) {
            stop()
            start()
        }
    }

    fun setVolume(volumePercent: Int) {
        volume = volumePercent / 100f
    }

    private fun startMetronome() {
        job = scope.launch(Dispatchers.Default) {
            val intervalMs = (60000.0 / bpm).toLong()
            var expectedTime = System.currentTimeMillis()

            while (isPlaying) {
                soundPool.play(clickSoundId, volume, volume, 1, 0, 1f)
                
                expectedTime += intervalMs
                val now = System.currentTimeMillis()
                val sleepTime = expectedTime - now
                
                if (sleepTime > 0) {
                    delay(sleepTime)
                } else {
                    expectedTime = now // Reset if we're falling behind
                }
            }
        }
    }

    fun release() {
        stop()
        scope.cancel()
        soundPool.release()
    }
} 