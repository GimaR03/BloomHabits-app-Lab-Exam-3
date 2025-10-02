package com.example.bloomhabit_app.model

data class Habit(
    val id: Int,
    var name: String,
    var category: String,
    var goalDescription: String, // e.g., "Run 2Km"
    var progress: Int = 0,
    var reminderTime: Long? = null, // for notifications if you want
    var targetDateTime: Long? = null // scheduled date/time for grouping/display
)
