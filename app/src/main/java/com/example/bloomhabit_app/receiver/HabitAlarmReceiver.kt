package com.example.bloomhabit_app.receiver

import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bloomhabit_app.MainActivity
import com.example.bloomhabit_app.R

class HabitAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra("habit_id", -1)
        val habitName = intent.getStringExtra("habit_name") ?: "Your Habit"

        Log.d("HabitAlarmReceiver", "Alarm received for habit: $habitName (ID: $habitId)")

        // Show notification
        showNotification(context, habitId, habitName)
    }

    private fun showNotification(context: Context, habitId: Int, habitName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        createNotificationChannel(notificationManager)

        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_habits", true) // Optional: to open habits section
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("🌱 Habit Reminder")
            .setContentText("Time for: $habitName")
            .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration
            .setStyle(NotificationCompat.BigTextStyle().bigText("Don't forget to complete your habit: $habitName"))
            .build()

        // Show notification
        notificationManager.notify(habitId, notification)

        Log.d("HabitAlarmReceiver", "Notification shown for: $habitName")
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel already exists
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("HabitAlarmReceiver", "Notification channel created")
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "habit_reminder_channel"
        const val CHANNEL_NAME = "Habit Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for your habit reminders"
    }
}