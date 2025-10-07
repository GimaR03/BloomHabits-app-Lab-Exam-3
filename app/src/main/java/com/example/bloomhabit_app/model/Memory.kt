package com.example.bloomhabit_app.model

import java.util.*

data class Memory(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val date: String, // Format: "yyyy.MM.dd"
    val mood: String = "Neutral", // Good, Bad, Neutral
    val createdAt: Long = System.currentTimeMillis()
)