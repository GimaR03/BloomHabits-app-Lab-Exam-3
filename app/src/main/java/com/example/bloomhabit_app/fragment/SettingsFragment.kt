package com.example.bloomhabit_app.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.utils.HabitReminderReceiver
import com.example.bloomhabit_app.utils.SharedPrefsHelper

class SettingsFragment : Fragment() {

    private lateinit var prefsHelper: SharedPrefsHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SettingsFragment", "onCreateView started")
        try {
            val view = inflater.inflate(R.layout.fragment_settings, container, false)

            prefsHelper = SharedPrefsHelper(requireContext())

            val intervalEdit: EditText = view.findViewById(R.id.edit_reminder_interval)
            val saveIntervalButton: Button = view.findViewById(R.id.save_interval)
            val themeRadioGroup: RadioGroup = view.findViewById(R.id.theme_radio_group)
            val radioLight: RadioButton = view.findViewById(R.id.radio_light)
            val radioDark: RadioButton = view.findViewById(R.id.radio_dark)

            // Load saved interval
            intervalEdit.setText(prefsHelper.getReminderInterval().toString())

            // Load saved theme
            when (prefsHelper.getSelectedTheme()) {
                AppCompatDelegate.MODE_NIGHT_NO -> radioLight.isChecked = true
                AppCompatDelegate.MODE_NIGHT_YES -> radioDark.isChecked = true
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                    // If system theme is saved but we don't have the radio button, default to light
                    radioLight.isChecked = true
                }
                else -> radioLight.isChecked = true // Default to light mode
            }

            // Save reminder interval
            saveIntervalButton.setOnClickListener {
                try {
                    val intervalText = intervalEdit.text.toString().trim()
                    if (intervalText.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter an interval", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val interval = intervalText.toIntOrNull() ?: run {
                        Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (interval <= 0) {
                        Toast.makeText(requireContext(), "Interval must be positive", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (interval > 1440) { // 24 hours in minutes
                        Toast.makeText(requireContext(), "Interval cannot exceed 1440 minutes (24 hours)", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    prefsHelper.setReminderInterval(interval)
                    setReminder(interval)
                    Toast.makeText(requireContext(), "Reminder set for every $interval minutes", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error saving interval: ${e.message}")
                    Toast.makeText(requireContext(), "Error setting reminder", Toast.LENGTH_SHORT).show()
                }
            }

            // Theme selection
            themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                try {
                    when (checkedId) {
                        R.id.radio_light -> {
                            prefsHelper.saveSelectedTheme(AppCompatDelegate.MODE_NIGHT_NO)
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            Toast.makeText(requireContext(), "Light mode applied", Toast.LENGTH_SHORT).show()
                        }
                        R.id.radio_dark -> {
                            prefsHelper.saveSelectedTheme(AppCompatDelegate.MODE_NIGHT_YES)
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            Toast.makeText(requireContext(), "Dark mode applied", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error changing theme: ${e.message}")
                    Toast.makeText(requireContext(), "Error applying theme", Toast.LENGTH_SHORT).show()
                }
            }

            Log.d("SettingsFragment", "onCreateView completed successfully")
            return view
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Error in onCreateView: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun setReminder(intervalMinutes: Int) {
        try {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), HabitReminderReceiver::class.java)

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                flags
            )

            val intervalMillis = intervalMinutes * 60 * 1000L
            val triggerTime = System.currentTimeMillis() + intervalMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d("SettingsFragment", "Reminder set for $intervalMinutes minutes")
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Error setting alarm: ${e.message}")
            Toast.makeText(requireContext(), "Error setting reminder alarm", Toast.LENGTH_SHORT).show()
        }
    }
//ondestroyview
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("SettingsFragment", "onDestroyView")
    }
}