package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SettingsRepository
import com.example.service.PrayerAzanService
import com.example.worker.PrayerRescheduleWorker

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED || 
            action == ACTION_PRAYER_ALARM) {
            
            val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
            val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "موقعك"
            val muezzinId = intent.getStringExtra(EXTRA_MUEZZIN_ID) ?: "makkah"

            if (action == ACTION_PRAYER_ALARM) {
                startAzanForegroundService(context, prayerName, cityName, muezzinId)
            }

            // Reschedule upcoming prayer using AlarmManager.setAlarmClock
            scheduleNextPrayerAlarm(context)

            // Enqueue WorkManager periodic work for extra safety
            PrayerRescheduleWorker.enqueuePeriodicWork(context)
        }
    }

    private fun startAzanForegroundService(
        context: Context,
        prayerName: String,
        cityName: String,
        muezzinId: String
    ) {
        try {
            val serviceIntent = Intent(context, PrayerAzanService::class.java).apply {
                action = PrayerAzanService.ACTION_START_AZAN
                putExtra(PrayerAzanService.EXTRA_PRAYER_NAME, prayerName)
                putExtra(PrayerAzanService.EXTRA_CITY_NAME, cityName)
                putExtra(PrayerAzanService.EXTRA_MUEZZIN_ID, muezzinId)
            }

            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
        const val PRAYER_NOTIFICATION_ID = 1001

        fun scheduleNextPrayerAlarm(context: Context) {
            try {
                val settingsRepo = SettingsRepository(context)
                val settings = settingsRepo.settings.value
                val prayerRepo = PrayerRepository()

                val schedule = prayerRepo.getPrayerSchedule(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    locationName = settings.selectedCityName
                )

                val nextPrayer = prayerRepo.getNextPrayerInfo(schedule)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // Show Intent to open main activity
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Alarm Pending Intent to trigger receiver
                val alarmIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_PRAYER_NAME, nextPrayer.prayerType.arName)
                    putExtra(EXTRA_CITY_NAME, settings.selectedCityName)
                    putExtra(EXTRA_MUEZZIN_ID, settings.selectedMuezzinId)
                }

                val alarmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    PRAYER_NOTIFICATION_ID,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Use AlarmManager.setAlarmClock() for Doze mode bypass
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        nextPrayer.prayerTimeMillis,
                        openAppPendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, alarmPendingIntent)
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextPrayer.prayerTimeMillis,
                        alarmPendingIntent
                    )
                }

                // Enqueue background periodic WorkManager to ensure schedule persistence
                PrayerRescheduleWorker.enqueuePeriodicWork(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
