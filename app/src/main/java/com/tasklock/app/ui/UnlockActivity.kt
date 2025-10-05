package com.tasklock.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.tasklock.app.R
import com.tasklock.app.data.LessonRepository
import com.tasklock.app.service.KioskService

class UnlockActivity : AppCompatActivity() {

    private lateinit var repository: LessonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        repository = LessonRepository.getInstance(this)

        val unlockInput: TextInputEditText = findViewById(R.id.unlock_code)
        val unlockButton: Button = findViewById(R.id.unlock_button)

        unlockButton.setOnClickListener {
            val code = unlockInput.text?.toString().orEmpty()
            if (code == repository.getUnlockCode()) {
                repository.setLessonCompleted(false)
                stopService(Intent(this, KioskService::class.java))
                openSettings()
            } else {
                Toast.makeText(this, R.string.invalid_code, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        // Prevent leaving without code
    }
}
