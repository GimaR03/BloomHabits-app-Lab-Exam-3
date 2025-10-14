package com.example.bloomhabit_app.model

data class Habit(
    val id: Int,
    val name: String,
    val category: String,
    val goalDescription: String,
    val progress: Int = 0,
    val reminderTime: Long? = null,
    val targetDateTime: Long? = null, // For calendar
    val isReminderEnabled: Boolean = false // Track if alarm is set
)