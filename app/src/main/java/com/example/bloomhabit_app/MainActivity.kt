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
import com.example.bloomhabit_app.databinding.ActivityMainBinding
import com.example.bloomhabit_app.fragment.AddHabitFragment
import com.example.bloomhabit_app.fragment.HomeFragment
import com.example.bloomhabit_app.fragment.ReportsFragment
import com.example.bloomhabit_app.fragment.SettingsFragment
import com.example.bloomhabit_app.utils.AlarmHelper
import com.example.bloomhabit_app.utils.NotificationHelper
import com.example.bloomhabit_app.utils.SharedPrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsHelper: SharedPrefsHelper
    private lateinit var alarmHelper: AlarmHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
                // Now you can set up alarms and notifications
                initializeAlarms()
            } else {
                Log.d("MainActivity", "Notification permission denied")
                showToast("Notifications disabled - you won't receive habit reminders")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme before setting content view
        prefsHelper = SharedPrefsHelper(this)
        applySavedTheme()

        // Initialize AlarmHelper
        alarmHelper = AlarmHelper(this)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MainActivity", "setContentView completed")

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        Log.d("MainActivity", "Notification channel created")

        // Request notification permission (Android 13+)
        requestNotificationPermission()

        // Set up bottom navigation listener
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
            Log.d("MainActivity", "Default fragment loaded")
        }

        Log.d("MainActivity", "onCreate completed successfully")
    }

    private fun initializeAlarms() {
        // This method can be used to reschedule all alarms if needed
        Log.d("MainActivity", "Alarms initialized")
    }

    private fun applySavedTheme() {
        val savedTheme = prefsHelper.getSelectedTheme()
        AppCompatDelegate.setDefaultNightMode(savedTheme)
        Log.d("MainActivity", "Applied theme: $savedTheme")
    }

    private fun requestNotificationPermission() {
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
                initializeAlarms()
            }
        } else {
            // For devices below Android 13, permissions are granted by default
            initializeAlarms()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_add -> {
                    loadFragment(AddHabitFragment())
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

        // Set navigation item reselected listener to handle same item clicks
        binding.bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Optional: Scroll to top or refresh home fragment
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is HomeFragment) {
                        // You can add refresh functionality here later if needed
                        Log.d("MainActivity", "Home fragment reselected")
                    }
                }
                R.id.nav_reports -> {
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is ReportsFragment) {
                        Log.d("MainActivity", "Reports fragment reselected")
                    }
                }
                // Handle other reselections if needed
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            Log.d("MainActivity", "Fragment loaded: ${fragment::class.java.simpleName}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading fragment: ${e.message}")
            e.printStackTrace()
            showToast("Error loading page")
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
            updateBottomNavSelection()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateBottomNavSelection() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (currentFragment) {
            is HomeFragment -> binding.bottomNav.selectedItemId = R.id.nav_home
            is ReportsFragment -> binding.bottomNav.selectedItemId = R.id.nav_reports
            is SettingsFragment -> binding.bottomNav.selectedItemId = R.id.nav_settings
            // AddHabitFragment doesn't change selection as it's temporary
        }
    }

    // Method to change theme dynamically
    fun changeTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        prefsHelper.saveSelectedTheme(themeMode)
        recreate()
    }

    // Get alarm helper for fragments to use
    fun getAlarmHelper(): AlarmHelper {
        return alarmHelper
    }
}