package com.example.bloomhabit_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bloomhabit_app.fragment.AddHabitFragment
import com.example.bloomhabit_app.fragment.HomeFragment
import com.example.bloomhabit_app.fragment.ReportsFragment
import com.example.bloomhabit_app.fragment.SettingsFragment
import com.example.bloomhabit_app.utils.NotificationHelper
import com.example.bloomhabit_app.utils.SharedPrefsHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var prefsHelper: SharedPrefsHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme before setting content view
        prefsHelper = SharedPrefsHelper(this)
        applySavedTheme()

        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "setContentView completed")

        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottom_nav)
        Log.d("MainActivity", "Bottom nav initialized")

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        Log.d("MainActivity", "Notification channel created")

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Log.d("MainActivity", "Notification permission requested")
            } else {
                Log.d("MainActivity", "Notification permission already granted")
            }
        }

        // Set up bottom navigation listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_add -> {
                    loadFragment(AddHabitFragment(null))
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            Log.d("MainActivity", "Default fragment loaded")
        }

        Log.d("MainActivity", "onCreate completed successfully")
    }

    private fun applySavedTheme() {
        val savedTheme = prefsHelper.getSelectedTheme()
        AppCompatDelegate.setDefaultNightMode(savedTheme)
        Log.d("MainActivity", "Applied theme: $savedTheme")
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            Log.d("MainActivity", "Fragment loaded: ${fragment::class.java.simpleName}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading fragment: ${e.message}")
            e.printStackTrace()
        }
    }
}