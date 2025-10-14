package com.example.bloomhabit_app.model

data class DailyFeeling(
    val date: String, // Format: "yyyy.MM.dd" pattern
    val emoji: String,
    val feelingName: String,
    val timestamp: Long = System.currentTimeMillis()
)