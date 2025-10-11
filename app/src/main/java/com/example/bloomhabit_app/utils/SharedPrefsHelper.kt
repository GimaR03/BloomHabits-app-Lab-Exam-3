package com.example.bloomhabit_app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class SharedPrefsHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val DEFAULT_THEME = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        private const val DEFAULT_REMINDER_INTERVAL = 60

        // Theme constants
        const val THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        const val THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES
        const val THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    // Theme methods COMMIT VERSION: Set reminder interval with commit (returns success status)
    fun saveSelectedTheme(themeMode: Int) {
        sharedPreferences.edit().putInt(KEY_SELECTED_THEME, themeMode).apply()
    }

    fun getSelectedTheme(): Int {
        return sharedPreferences.getInt(KEY_SELECTED_THEME, DEFAULT_THEME)
    }

    // Reminder interval methods
    fun setReminderInterval(intervalMinutes: Int) {
        sharedPreferences.edit().putInt(KEY_REMINDER_INTERVAL, intervalMinutes).apply()
    }

    fun getReminderInterval(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_INTERVAL, DEFAULT_REMINDER_INTERVAL)
    }

    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}