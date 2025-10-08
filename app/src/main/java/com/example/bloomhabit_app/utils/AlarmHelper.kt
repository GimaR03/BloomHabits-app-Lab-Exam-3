package com.example.bloomhabit_app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.bloomhabit_app.receiver.HabitAlarmReceiver

class AlarmHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setHabitAlarm(habitId: Int, habitName: String, reminderTime: Long) {
        try {
            val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
                putExtra("habit_id", habitId)
                putExtra("habit_name", habitName)
                putExtra("reminder_time", reminderTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habitId, // Use habitId as request code for uniqueness
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Set the exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }

            Log.d("AlarmHelper", "Alarm set for habit: '$habitName' at ${java.util.Date(reminderTime)}")

        } catch (e: Exception) {
            Log.e("AlarmHelper", "Error setting alarm for habit '$habitName': ${e.message}")
            e.printStackTrace()
        }
    }

    fun cancelHabitAlarm(habitId: Int) {
        try {
            val intent = Intent(context, HabitAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habitId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d("AlarmHelper", "Alarm cancelled for habit ID: $habitId")
        } catch (e: Exception) {
            Log.e("AlarmHelper", "Error cancelling alarm for habit ID $habitId: ${e.message}")
        }
    }

    fun isAlarmSet(habitId: Int): Boolean {
        return try {
            val intent = Intent(context, HabitAlarmReceiver::class.java)
            PendingIntent.getBroadcast(
                context,
                habitId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) != null
        } catch (e: Exception) {
            Log.e("AlarmHelper", "Error checking alarm status: ${e.message}")
            false
        }
    }
}