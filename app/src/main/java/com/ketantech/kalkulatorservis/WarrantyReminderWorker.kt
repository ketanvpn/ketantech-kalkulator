package com.ketantech.kalkulatorservis

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ketantech.kalkulatorservis.data.AppRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Worker harian yang mengecek nota-nota yang garansinya jatuh tempo dalam 3 hari
 * dan menampilkan notifikasi reminder.
 */
class WarrantyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = AppRepository(applicationContext)

        // Cek untuk tanggal H-3 dari sekarang (3 hari ke depan)
        val threeDaysAhead = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
        }.time

        val expiring = try {
            repo.getReceiptsExpiringOn(threeDaysAhead)
        } catch (e: Exception) {
            return Result.retry()
        }

        NotificationHelper.createChannel(applicationContext)
        for (receipt in expiring) {
            // notificationId = (id mod 100000) supaya unik per nota
            val notifId = (receipt.id % 100000L).toInt()
            NotificationHelper.showWarrantyReminder(
                applicationContext,
                receipt.receiptNumber,
                receipt.deviceName,
                notifId
            )
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "ketantech_warranty_reminder_daily"

        /** Jadwalkan worker harian (sekali sekitar pukul 09:00 — interval 24 jam). */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<WarrantyReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
