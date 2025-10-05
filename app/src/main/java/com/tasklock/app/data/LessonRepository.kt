package com.tasklock.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFS = "tasklock.prefs"
private const val KEY_LESSON_URL = "lesson.url"
private const val KEY_UNLOCK_CODE = "unlock.code"
private const val KEY_LESSON_COMPLETED = "lesson.completed"

class LessonRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getLessonUri(): String {
        return prefs.getString(KEY_LESSON_URL, DEFAULT_LESSON_URL) ?: DEFAULT_LESSON_URL
    }

    fun setLessonUri(uri: String) {
        prefs.edit { putString(KEY_LESSON_URL, uri) }
    }

    fun getUnlockCode(): String {
        return prefs.getString(KEY_UNLOCK_CODE, DEFAULT_UNLOCK_CODE) ?: DEFAULT_UNLOCK_CODE
    }

    fun setUnlockCode(code: String) {
        prefs.edit { putString(KEY_UNLOCK_CODE, code) }
    }

    fun isLessonCompleted(): Boolean = prefs.getBoolean(KEY_LESSON_COMPLETED, false)

    fun setLessonCompleted(completed: Boolean) {
        prefs.edit { putBoolean(KEY_LESSON_COMPLETED, completed) }
    }

    companion object {
        private const val DEFAULT_LESSON_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val DEFAULT_UNLOCK_CODE = "1234"

        @Volatile
        private var INSTANCE: LessonRepository? = null

        fun getInstance(context: Context): LessonRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LessonRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
