package com.example.bloomhabit_app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bloomhabit_app.model.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val _habits = MutableLiveData<List<Habit>>(emptyList())
    val habits: LiveData<List<Habit>> = _habits

    private val sharedPreferences =
        application.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadData()
    }

    fun addHabit(name: String, category: String, goalDescription: String, targetDateTime: Long?) {
        val list = _habits.value?.toMutableList() ?: mutableListOf()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        // set both reminderTime and targetDateTime to the chosen time (you can separate them later)
        val habit = Habit(id, name, category, goalDescription, 0, targetDateTime, targetDateTime)
        list.add(habit)
        _habits.value = list.toList()
        saveData()
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
            saveData()
        }
    }

    fun deleteHabit(habit: Habit) {
        val list = _habits.value?.toMutableList() ?: return
        list.removeAll { it.id == habit.id }
        _habits.value = list.toList()
        saveData()
    }

    private fun saveData() {
        val list: List<Habit> = _habits.value ?: emptyList()
        val json = gson.toJson(list)
        sharedPreferences.edit().putString("habits", json).apply()
    }

    private fun loadData() {
        val json = sharedPreferences.getString("habits", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Habit>>() {}.type
            val list: List<Habit> = gson.fromJson(json, type)
            _habits.value = list
        } else {
            _habits.value = emptyList()
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
}
