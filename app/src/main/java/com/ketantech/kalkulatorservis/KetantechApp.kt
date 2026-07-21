package com.ketantech.kalkulatorservis

import android.app.Application

/**
 * Application class: inisialisasi channel notifikasi & schedule reminder harian.
 */
class KetantechApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        WarrantyReminderWorker.schedule(this)
    }
}
