package com.example.metronomeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private var currentBpm = 180
    private lateinit var bpmText: TextView
    private lateinit var volumeSlider: SeekBar
    private lateinit var playButton: ImageButton
    private var isPlaying = false
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "MetronomePrefs"
        private const val KEY_BPM = "bpm"
        private const val KEY_VOLUME = "volume"
        private const val DEFAULT_BPM = 180
        private const val DEFAULT_VOLUME = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load saved values
        currentBpm = prefs.getInt(KEY_BPM, DEFAULT_BPM)

        bpmText = findViewById(R.id.bpmText)
        volumeSlider = findViewById(R.id.volumeSlider)
        playButton = findViewById(R.id.playButton)

        // Set initial values
        updateBpmDisplay()
        volumeSlider.progress = prefs.getInt(KEY_VOLUME, DEFAULT_VOLUME)

        findViewById<ImageButton>(R.id.decrementButton).setOnClickListener {
            if (currentBpm > 40) {
                currentBpm--
                updateBpmDisplay()
                saveBpm()
                if (isPlaying) {
                    updateServiceBpm()
                }
            }
        }

        findViewById<ImageButton>(R.id.incrementButton).setOnClickListener {
            if (currentBpm < 240) {
                currentBpm++
                updateBpmDisplay()
                saveBpm()
                if (isPlaying) {
                    updateServiceBpm()
                }
            }
        }

        playButton.setOnClickListener {
            if (isPlaying) {
                stopMetronomeService()
                playButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                startMetronomeService()
                playButton.setImageResource(android.R.drawable.ic_media_pause)
            }
            isPlaying = !isPlaying
        }

        volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isPlaying) {
                    updateServiceVolume(progress)
                }
                saveVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveBpm() {
        prefs.edit().apply {
            putInt(KEY_BPM, currentBpm)
            apply()
        }
    }

    private fun saveVolume(volume: Int) {
        prefs.edit().apply {
            putInt(KEY_VOLUME, volume)
            apply()
        }
    }

    private fun startMetronomeService() {
        val serviceIntent = Intent(this, MetronomeService::class.java).apply {
            action = "START"
            putExtra("BPM", currentBpm)
            putExtra("VOLUME", volumeSlider.progress)
        }
        startService(serviceIntent)
    }

    private fun stopMetronomeService() {
        val serviceIntent = Intent(this, MetronomeService::class.java).apply {
            action = "STOP"
        }
        startService(serviceIntent)
    }

    private fun updateServiceBpm() {
        val serviceIntent = Intent(this, MetronomeService::class.java).apply {
            action = "UPDATE_BPM"
            putExtra("BPM", currentBpm)
        }
        startService(serviceIntent)
    }

    private fun updateServiceVolume(volume: Int) {
        val serviceIntent = Intent(this, MetronomeService::class.java).apply {
            action = "UPDATE_VOLUME"
            putExtra("VOLUME", volume)
        }
        startService(serviceIntent)
    }

    private fun updateBpmDisplay() {
        bpmText.text = currentBpm.toString()
    }
}