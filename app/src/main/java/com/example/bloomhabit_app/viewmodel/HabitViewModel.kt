package com.example.bloomhabit_app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.model.Memory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val _habits = MutableLiveData<List<Habit>>(emptyList())
    val habits: LiveData<List<Habit>> = _habits

    private val _memories = MutableLiveData<List<Memory>>(emptyList())
    val memories: LiveData<List<Memory>> = _memories

    private val sharedPreferences =
        application.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadHabitsData()
        loadMemoriesData()
    }

    // Habit methods
    fun addHabit(name: String, category: String, goalDescription: String, targetDateTime: Long?) {
        val list = _habits.value?.toMutableList() ?: mutableListOf()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        // set both reminderTime and targetDateTime to the chosen time (you can separate them later)
        val habit = Habit(id, name, category, goalDescription, 0, targetDateTime, targetDateTime)
        list.add(habit)
        _habits.value = list.toList()
        saveHabitsData()
    }

    fun editHabit(
        habit: Habit,
        name: String,
        category: String,
        goalDescription: String,
        targetDateTime: Long?
    ) {
        val list = _habits.value?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val updated = habit.copy(
                name = name,
                category = category,
                goalDescription = goalDescription,
                reminderTime = targetDateTime,
                targetDateTime = targetDateTime
            )
            list[index] = updated
            _habits.value = list.toList()
            saveHabitsData()
        }
    }

    fun deleteHabit(habit: Habit) {
        val list = _habits.value?.toMutableList() ?: return
        list.removeAll { it.id == habit.id }
        _habits.value = list.toList()
        saveHabitsData()
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

    /**
     * Returns a map keyed by "yyyy.MM.dd" -> list of habits scheduled on that date.
     * Dates are in the device locale.
     */
    fun groupHabitsByDate(): Map<String, List<Habit>> {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return _habits.value.orEmpty()
            .filter { it.targetDateTime != null }
            .groupBy { sdf.format(Date(it.targetDateTime!!)) }
            .toSortedMap(compareByDescending { it }) // most recent date first
    }

    /**
     * Returns a map keyed by "yyyy.MM.dd" -> list of memories for that date.
     */
    fun groupMemoriesByDate(): Map<String, List<Memory>> {
        return _memories.value.orEmpty()
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it }) // most recent date first
    }

    /**
     * Get memories for a specific date
     */
    fun getMemoriesForDate(date: String): List<Memory> {
        return _memories.value.orEmpty().filter { it.date == date }
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
}