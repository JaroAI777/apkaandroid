package com.tasklock.app.service

import android.app.Notification
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tasklock.app.R
import com.tasklock.app.device.AdminReceiver
import com.tasklock.app.ui.LessonActivity
import com.tasklock.app.util.NotificationId

class KioskService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NotificationId.KIOSK_FOREGROUND, buildNotification())
        configureLockTask()
        launchLessonIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        configureLockTask()
        launchLessonIfNeeded()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun configureLockTask() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = AdminReceiver.getComponentName(this)
        if (dpm.isAdminActive(component)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.setLockTaskPackages(component, arrayOf(packageName))
            }
        }
    }

    private fun launchLessonIfNeeded() {
        val lessonIntent = Intent(this, LessonActivity::class.java)
        lessonIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(lessonIntent)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationId.KIOSK_CHANNEL)
            .setContentTitle(getString(R.string.notification_kiosk_title))
            .setContentText(getString(R.string.notification_kiosk_message))
            .setSmallIcon(R.drawable.ic_lock)
            .setOngoing(true)
            .build()
    }

    companion object {
        fun ensureRunning(context: Context) {
            val intent = Intent(context, KioskService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
