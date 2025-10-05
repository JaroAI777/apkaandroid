package com.tasklock.app.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import com.tasklock.app.R
import com.tasklock.app.data.LessonRepository
import com.tasklock.app.util.NotificationId

class LessonPlaybackService : Service(), MediaPlayer.OnCompletionListener {

    private val mediaPlayer = MediaPlayer()
    private val binder = LessonBinder()
    private lateinit var repository: LessonRepository
    private var listener: LessonListener? = null
    private var surfaceHolder: SurfaceHolder? = null

    override fun onCreate() {
        super.onCreate()
        repository = LessonRepository.getInstance(applicationContext)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()
        )
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener { _, _, _ ->
            stopForeground(true)
            listener?.onLessonError()
            stopSelf()
            true
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onUnbind(intent: Intent?): Boolean {
        listener = null
        surfaceHolder = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        repository.setLessonCompleted(true)
        listener?.onLessonCompleted()
        stopForeground(true)
        stopSelf()
    }

    fun attachSurface(holder: SurfaceHolder) {
        surfaceHolder = holder
        if (mediaPlayer.isPlaying) {
            mediaPlayer.setDisplay(holder)
        }
    }

    fun playLesson(listener: LessonListener) {
        this.listener = listener
        val lessonUri = repository.getLessonUri()
        mediaPlayer.reset()
        surfaceHolder?.let { mediaPlayer.setDisplay(it) }
        mediaPlayer.setOnPreparedListener {
            startForeground(NotificationId.LESSON_FOREGROUND, buildNotification())
            it.start()
            repository.setLessonCompleted(false)
            listener.onLessonStarted(it.duration)
        }
        mediaPlayer.setDataSource(this, Uri.parse(lessonUri))
        mediaPlayer.prepareAsync()
    }

    fun stopLesson() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        stopForeground(true)
    }

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun currentPosition(): Int = mediaPlayer.currentPosition

    fun duration(): Int = mediaPlayer.duration

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationId.LESSON_CHANNEL)
            .setContentTitle(getString(R.string.notification_lesson_title))
            .setContentText(getString(R.string.notification_lesson_message))
            .setSmallIcon(R.drawable.ic_lock)
            .setOngoing(true)
            .build()
    }

    inner class LessonBinder : Binder() {
        fun getService(): LessonPlaybackService = this@LessonPlaybackService
    }

    interface LessonListener {
        fun onLessonStarted(duration: Int)
        fun onLessonCompleted()
        fun onLessonError()
    }
}
