package com.tasklock.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.tasklock.app.service.KioskService
import com.tasklock.app.util.NotificationId

class TaskLockApplication : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        KioskService.ensureRunning(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        KioskService.ensureRunning(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val kioskChannel = NotificationChannel(
                NotificationId.KIOSK_CHANNEL,
                getString(R.string.notification_channel_kiosk),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(kioskChannel)

            val lessonChannel = NotificationChannel(
                NotificationId.LESSON_CHANNEL,
                getString(R.string.notification_channel_lesson),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(lessonChannel)
        }
    }
}
