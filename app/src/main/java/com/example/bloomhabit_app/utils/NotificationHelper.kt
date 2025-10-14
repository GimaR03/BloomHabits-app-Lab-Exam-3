package com.example.bloomhabit_app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bloomhabit_app.MainActivity
import com.example.bloomhabit_app.R

//notification helper
object NotificationHelper {

    const val CHANNEL_ID = "habit_channel"
    const val CHANNEL_NAME = "Habit Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for habits"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHabitReminder(context: Context, habitName: String, habitTime: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
//displaynotification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("⏰ Habit Reminder")
            .setContentText("Time for: $habitName")
            .setSmallIcon(R.drawable.ic_habit_reminder)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's $habitTime - Time to work on your habit: \"$habitName\""))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}