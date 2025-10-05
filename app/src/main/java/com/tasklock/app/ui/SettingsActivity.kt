package com.tasklock.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.tasklock.app.R
import com.tasklock.app.data.LessonRepository
import com.tasklock.app.service.KioskService

class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: LessonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        repository = LessonRepository.getInstance(this)

        val lessonUrl: TextInputEditText = findViewById(R.id.lesson_url)
        val unlockCode: TextInputEditText = findViewById(R.id.settings_unlock_code)
        val saveButton: Button = findViewById(R.id.save_button)

        lessonUrl.setText(repository.getLessonUri())
        unlockCode.setText(repository.getUnlockCode())

        saveButton.setOnClickListener {
            val url = lessonUrl.text?.toString().orEmpty()
            val code = unlockCode.text?.toString().orEmpty()
            if (!isValidUri(url)) {
                Toast.makeText(this, R.string.toast_invalid_uri, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            repository.setLessonUri(url)
            if (code.isNotBlank()) {
                repository.setUnlockCode(code)
            }
            repository.setLessonCompleted(false)
            Toast.makeText(this, R.string.lesson_saved, Toast.LENGTH_SHORT).show()
            restartKiosk()
        }
    }

    private fun isValidUri(uri: String): Boolean {
        val parsed = Uri.parse(uri)
        return parsed.scheme != null && parsed.host != null
    }

    private fun restartKiosk() {
        val intent = Intent(this, KioskService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        startActivity(Intent(this, LessonActivity::class.java))
        finish()
    }
}
