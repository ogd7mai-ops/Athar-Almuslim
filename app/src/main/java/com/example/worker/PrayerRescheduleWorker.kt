package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.receiver.PrayerAlarmReceiver
import java.util.concurrent.TimeUnit

class PrayerRescheduleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            PrayerAlarmReceiver.scheduleNextPrayerAlarm(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "PrayerRescheduleWorker"

        fun enqueuePeriodicWork(context: Context) {
            try {
                val workRequest = PeriodicWorkRequestBuilder<PrayerRescheduleWorker>(
                    6, TimeUnit.HOURS
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
