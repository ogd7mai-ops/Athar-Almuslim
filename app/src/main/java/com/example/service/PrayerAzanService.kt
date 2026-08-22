package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.AudioRepository
import com.example.data.repository.SettingsRepository
import com.example.receiver.PrayerAlarmReceiver

class PrayerAzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_AZAN

        if (action == ACTION_STOP_AZAN || action == ACTION_MUTE_AZAN) {
            stopAzanAndService()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val cityName = intent?.getStringExtra(EXTRA_CITY_NAME) ?: "موقعك"
        val muezzinId = intent?.getStringExtra(EXTRA_MUEZZIN_ID) ?: "makkah"

        createNotificationChannel()

        val notification = buildForegroundNotification(prayerName, cityName)
        startForeground(NOTIFICATION_ID, notification)

        playAzanAudio(muezzinId)

        return START_STICKY
    }

    private fun buildForegroundNotification(prayerName: String, cityName: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PrayerAzanService::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val muteIntent = Intent(this, PrayerAzanService::class.java).apply {
            action = ACTION_MUTE_AZAN
        }
        val mutePendingIntent = PendingIntent.getService(
            this,
            2,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان الآن موعد صلاة $prayerName 🕌")
            .setContentText("حسب توقيت $cityName - حيّ على الصلاة، حيّ على الفلاح")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف الأذان", stopPendingIntent)
            .addAction(android.R.drawable.ic_lock_silent_mode, "كتم الصوت", mutePendingIntent)
            .build()
    }

    private fun playAzanAudio(muezzinId: String) {
        stopAudio()

        try {
            // Set alarm volume to maximum for clear Azan sound
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let {
                val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                it.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
            }

            val rawUri = Uri.parse("android.resource://$packageName/${R.raw.adhan_alafasy}")

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@PrayerAzanService, rawUri)
                isLooping = false
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    stopAzanAndService()
                }
                setOnErrorListener { _, _, _ ->
                    fallbackPlayRawAzan()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackPlayRawAzan()
        }
    }

    private fun fallbackPlayRawAzan() {
        try {
            stopAudio()
            mediaPlayer = MediaPlayer.create(this, R.raw.adhan_alafasy)
            mediaPlayer?.let { mp ->
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mp.setOnCompletionListener { stopAzanAndService() }
                mp.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAzanAndService() {
        stopAudio()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Immediately reschedule next prayer alarm when user swipes app from recent tasks
        try {
            PrayerAlarmReceiver.scheduleNextPrayerAlarm(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "خدمة تشغيل الأذان في الخلفية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات وأصوات الأذان في موعد الصلاة"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(null, null) // Sound is played manually via USAGE_ALARM MediaPlayer
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "prayer_azan_foreground_channel"
        const val NOTIFICATION_ID = 2002

        const val ACTION_START_AZAN = "com.example.service.ACTION_START_AZAN"
        const val ACTION_STOP_AZAN = "com.example.service.ACTION_STOP_AZAN"
        const val ACTION_MUTE_AZAN = "com.example.service.ACTION_MUTE_AZAN"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
    }
}
