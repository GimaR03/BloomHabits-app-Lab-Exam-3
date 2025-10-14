package com.example.bloomhabit_app.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bloomhabit_app.R

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Read habit info passed from AlarmManager
        val habitName = intent?.getStringExtra("habit_name") ?: "Habit Reminder"
        val habitGoal = intent?.getStringExtra("habit_goal") ?: "Don't forget your habit today!"

        // Build notification with habit details
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water) // ✅ ensure ic_water.xml exists in res/drawable
            .setContentTitle(habitName)
            .setContentText(habitGoal)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        // Prevent crash by checking POST_NOTIFICATIONS permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notificationId = System.currentTimeMillis().toInt() // unique ID per notification
            notificationManager.notify(notificationId, builder.build())
        }
    }
}
