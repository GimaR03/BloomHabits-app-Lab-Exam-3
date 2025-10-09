package com.example.bloomhabit_app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bloomhabit_app.model.DailyFeeling
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.model.Memory
import com.example.bloomhabit_app.utils.AlarmHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val _habits = MutableLiveData<List<Habit>>(emptyList())
    val habits: LiveData<List<Habit>> = _habits

    private val _memories = MutableLiveData<List<Memory>>(emptyList())
    val memories: LiveData<List<Memory>> = _memories

    private val _dailyFeelings = MutableLiveData<List<DailyFeeling>>(emptyList())
    val dailyFeelings: LiveData<List<DailyFeeling>> = _dailyFeelings

    private val sharedPreferences =
        application.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadHabitsData()
        loadMemoriesData()
        loadFeelingsData()
    }

    // Feeling methods
    fun addOrUpdateFeeling(feeling: DailyFeeling) {
        val list = _dailyFeelings.value?.toMutableList() ?: mutableListOf()

        // Remove existing feeling for the same date
        list.removeAll { it.date == feeling.date }

        // Add new feeling
        list.add(feeling)
        _dailyFeelings.value = list.toList()
        saveFeelingsData()
    }

    fun getFeelingForDate(date: String): DailyFeeling? {
        return _dailyFeelings.value?.find { it.date == date }
    }

    // Updated Habit methods with targetDateTime support
    fun addHabit(
        name: String,
        category: String,
        goalDescription: String,
        targetDateTime: Long? = null,
        reminderTime: Long? = null,
        isReminderEnabled: Boolean = false
    ) {
        val list = _habits.value?.toMutableList() ?: mutableListOf()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        val habit = Habit(
            id = id,
            name = name,
            category = category,
            goalDescription = goalDescription,
            progress = 0,
            reminderTime = reminderTime,
            targetDateTime = targetDateTime,
            isReminderEnabled = isReminderEnabled
        )
        list.add(habit)
        _habits.value = list.toList()
        saveHabitsData()
    }

    // Updated editHabit method with all parameters
    fun editHabit(
        habit: Habit,
        name: String,
        category: String,
        goalDescription: String,
        targetDateTime: Long?,
        reminderTime: Long?,
        isReminderEnabled: Boolean = false
    ) {
        val list = _habits.value?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val updated = habit.copy(
                name = name,
                category = category,
                goalDescription = goalDescription,
                reminderTime = reminderTime,
                targetDateTime = targetDateTime,
                isReminderEnabled = isReminderEnabled
            )
            list[index] = updated
            _habits.value = list.toList()
            saveHabitsData()
        }
    }

    // Overloaded method for simpler editing (optional)
    fun editHabit(
        habit: Habit,
        name: String,
        category: String,
        goalDescription: String
    ) {
        editHabit(
            habit = habit,
            name = name,
            category = category,
            goalDescription = goalDescription,
            targetDateTime = habit.targetDateTime,
            reminderTime = habit.reminderTime,
            isReminderEnabled = habit.isReminderEnabled
        )
    }

    fun deleteHabit(habit: Habit) {
        val list = _habits.value?.toMutableList() ?: return
        list.removeAll { it.id == habit.id }
        _habits.value = list.toList()
        saveHabitsData()
    }

    // Method to delete habit with alarm cleanup
    fun deleteHabitWithAlarm(habit: Habit, alarmHelper: AlarmHelper) {
        // Cancel alarm first
        if (habit.isReminderEnabled) {
            alarmHelper.cancelHabitAlarm(habit.id)
        }
        // Then delete habit
        deleteHabit(habit)
    }

    // Method to update habit progress
    fun updateHabitProgress(habit: Habit, progress: Int) {
        val list = _habits.value?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val updated = habit.copy(progress = progress)
            list[index] = updated
            _habits.value = list.toList()
            saveHabitsData()
        }
    }

    // Method to get habit by ID
    fun getHabitById(id: Int): Habit? {
        return _habits.value?.find { it.id == id }
    }

    // Memory methods
    fun addMemory(memory: Memory) {
        val list = _memories.value?.toMutableList() ?: mutableListOf()
        list.add(memory)
        _memories.value = list.toList()
        saveMemoriesData()
    }

    fun updateMemory(updatedMemory: Memory) {
        val list = _memories.value?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == updatedMemory.id }
        if (index != -1) {
            list[index] = updatedMemory
            _memories.value = list.toList()
            saveMemoriesData()
        }
    }

    fun deleteMemory(memory: Memory) {
        val list = _memories.value?.toMutableList() ?: return
        list.removeAll { it.id == memory.id }
        _memories.value = list.toList()
        saveMemoriesData()
    }

    // Data persistence methods
    private fun saveHabitsData() {
        val list: List<Habit> = _habits.value ?: emptyList()
        val json = gson.toJson(list)
        sharedPreferences.edit().putString("habits", json).apply()
    }

    private fun loadHabitsData() {
        val json = sharedPreferences.getString("habits", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Habit>>() {}.type
            val list: List<Habit> = gson.fromJson(json, type)
            _habits.value = list
        } else {
            _habits.value = emptyList()
        }
    }

    private fun saveMemoriesData() {
        val list: List<Memory> = _memories.value ?: emptyList()
        val json = gson.toJson(list)
        sharedPreferences.edit().putString("memories", json).apply()
    }

    private fun loadMemoriesData() {
        val json = sharedPreferences.getString("memories", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Memory>>() {}.type
            val list: List<Memory> = gson.fromJson(json, type)
            _memories.value = list
        } else {
            _memories.value = emptyList()
        }
    }

    private fun saveFeelingsData() {
        val list: List<DailyFeeling> = _dailyFeelings.value ?: emptyList()
        val json = gson.toJson(list)
        sharedPreferences.edit().putString("feelings", json).apply()
    }

    private fun loadFeelingsData() {
        val json = sharedPreferences.getString("feelings", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<DailyFeeling>>() {}.type
            val list: List<DailyFeeling> = gson.fromJson(json, type)
            _dailyFeelings.value = list
        } else {
            _dailyFeelings.value = emptyList()
        }
    }

    /**
     * Returns a map keyed by "yyyy.MM.dd" -> list of habits scheduled on that date.
     */
    fun groupHabitsByDate(): Map<String, List<Habit>> {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return _habits.value.orEmpty()
            .filter { it.targetDateTime != null }
            .groupBy { sdf.format(Date(it.targetDateTime!!)) }
            .toSortedMap(compareByDescending { it })
    }

    /**
     * Get habits for a specific date
     */
    fun getHabitsForDate(date: String): List<Habit> {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return _habits.value.orEmpty().filter { habit ->
            habit.targetDateTime?.let { targetTime ->
                sdf.format(Date(targetTime)) == date
            } ?: false
        }
    }

    /**
     * Get habits by category
     */
    fun getHabitsByCategory(category: String): List<Habit> {
        return _habits.value.orEmpty().filter { it.category == category }
    }

    /**
     * Get all unique categories
     */
    fun getAllCategories(): List<String> {
        return _habits.value.orEmpty().map { it.category }.distinct()
    }
}