package com.example.metronomeapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MetronomeService : Service() {
    private lateinit var audioEngine: AudioEngine
    private val CHANNEL_ID = "MetronomeServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioEngine = AudioEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val bpm = intent.getIntExtra("BPM", 180)
                val volume = intent.getIntExtra("VOLUME", 100)
                audioEngine.setBpm(bpm)
                audioEngine.setVolume(volume)
                audioEngine.start()
                startForeground(NOTIFICATION_ID, createNotification(bpm))
            }
            "STOP" -> {
                audioEngine.stop()
                stopForeground(true)
                stopSelf()
            }
            "UPDATE_BPM" -> {
                val bpm = intent.getIntExtra("BPM", 180)
                audioEngine.setBpm(bpm)
                updateNotification(bpm)
            }
            "UPDATE_VOLUME" -> {
                val volume = intent.getIntExtra("VOLUME", 100)
                audioEngine.setVolume(volume)
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Metronome Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the metronome running in background"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(bpm: Int) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Metronome Running")
        .setContentText("BPM: $bpm")
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setOngoing(true)
        .setContentIntent(createPendingIntent())
        .build()

    private fun updateNotification(bpm: Int) {
        val notification = createNotification(bpm)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
} 