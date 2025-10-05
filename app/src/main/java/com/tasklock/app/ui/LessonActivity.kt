package com.tasklock.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tasklock.app.R
import com.tasklock.app.data.LessonRepository
import com.tasklock.app.service.LessonPlaybackService
import com.tasklock.app.service.LessonPlaybackService.LessonBinder
import kotlin.math.max

class LessonActivity : AppCompatActivity(),
    ServiceConnection,
    LessonPlaybackService.LessonListener,
    SurfaceHolder.Callback {

    private lateinit var repository: LessonRepository
    private lateinit var timerView: TextView
    private lateinit var surfaceView: SurfaceView
    private val handler = Handler(Looper.getMainLooper())

    private var lessonService: LessonPlaybackService? = null
    private var durationMs: Int = 0
    private var startTimestamp: Long = 0L
    private var bound = false
    private var playbackStarted = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!playbackStarted || durationMs == 0) return
            val elapsed = System.currentTimeMillis() - startTimestamp
            val remaining = max(0, durationMs - elapsed.toInt())
            timerView.text = if (remaining > 0) {
                getString(R.string.lesson_time_remaining, remaining / 1000)
            } else {
                getString(R.string.lesson_completed)
            }
            if (remaining > 0) {
                handler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        repository = LessonRepository.getInstance(this)
        timerView = findViewById(R.id.timer)
        surfaceView = findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(this)

        findViewById<Button>(R.id.call_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            startActivity(intent)
        }
        findViewById<Button>(R.id.sms_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            startActivity(intent)
        }

        startLockTaskSafe()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, LessonPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(this)
            bound = false
        }
        handler.removeCallbacks(timerRunnable)
    }

    override fun onResume() {
        super.onResume()
        startLockTaskSafe()
        if (repository.isLessonCompleted()) {
            proceedToUnlock()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LessonBinder
        lessonService = binder.getService()
        bound = true
        surfaceView.holder.let { lessonService?.attachSurface(it) }
        if (lessonService?.isPlaying() == true) {
            durationMs = lessonService?.duration() ?: 0
            playbackStarted = true
            startTimestamp = System.currentTimeMillis() - (lessonService?.currentPosition() ?: 0)
            handler.post(timerRunnable)
        } else if (!repository.isLessonCompleted()) {
            lessonService?.playLesson(this)
        } else {
            onLessonCompleted()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        lessonService = null
        bound = false
    }

    override fun onLessonStarted(duration: Int) {
        durationMs = duration
        playbackStarted = true
        startTimestamp = System.currentTimeMillis()
        handler.post(timerRunnable)
    }

    override fun onLessonCompleted() {
        playbackStarted = false
        handler.removeCallbacks(timerRunnable)
        timerView.text = getString(R.string.lesson_completed)
        stopLockTaskSafe()
        proceedToUnlock()
    }

    override fun onLessonError() {
        playbackStarted = false
        handler.removeCallbacks(timerRunnable)
        Toast.makeText(this, R.string.toast_invalid_uri, Toast.LENGTH_LONG).show()
        stopLockTaskSafe()
        proceedToUnlock()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        lessonService?.attachSurface(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // no-op
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // no-op
    }

    private fun proceedToUnlock() {
        if (!repository.isLessonCompleted()) {
            repository.setLessonCompleted(true)
        }
        val intent = Intent(this, UnlockActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun startLockTaskSafe() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startLockTask()
            }
        } catch (e: IllegalStateException) {
            // ignored when not allowed
        }
    }

    private fun stopLockTaskSafe() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopLockTask()
            }
        } catch (e: IllegalStateException) {
            // ignore
        }
    }
}
