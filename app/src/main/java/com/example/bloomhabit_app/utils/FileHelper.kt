package com.example.bloomhabit_app.utils

import android.content.Context
import java.io.File

class FileHelper(private val context: Context) {

    fun saveJson(fileName: String, json: String) {
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    fun loadJson(fileName: String): String? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.readText() else null
    }
}