package com.example.bloomhabit_app.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddHabitFragment(private val editHabit: Habit?) : Fragment() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_habit, container, false)

        val nameEdit: EditText = view.findViewById(R.id.edit_habit_name)
        val goalEdit: EditText = view.findViewById(R.id.edit_habit_goal)
        val categorySpinner: Spinner = view.findViewById(R.id.spinner_category)
        val reminderText: TextView = view.findViewById(R.id.reminder_text)
        val setReminderButton: Button = view.findViewById(R.id.set_reminder)
        val saveButton: Button = view.findViewById(R.id.save_habit)

        // Categories
        val categories = arrayOf("Health & wellness", "Mental Health", "Personal Growth", "Productivity", "Sport", "Social Health", "Household chores", "Better Sleep", "Other")
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)

        var reminderTime: Long = 0L

        if (editHabit != null) {
            nameEdit.setText(editHabit.name)
            goalEdit.setText(editHabit.goal)
            categorySpinner.setSelection(categories.indexOf(editHabit.category))
            reminderTime = editHabit.reminderTime
            if (reminderTime > 0) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                reminderText.text = "Reminder set for ${dateFormat.format(Date(reminderTime))}"
            }
        }

        setReminderButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                val timePicker = TimePickerDialog(requireContext(), { _, hour, min ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, min)
                    reminderTime = calendar.timeInMillis
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    reminderText.text = "Reminder set for ${dateFormat.format(Date(reminderTime))}"
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
                timePicker.show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        saveButton.setOnClickListener {
            val name = nameEdit.text.toString()
            val goal = goalEdit.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (name.isEmpty() || goal.isEmpty()) {
                Toast.makeText(requireContext(), "Invalid input: Name and goal required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editHabit == null) {
                viewModel.addHabit(name, category, goal, reminderTime)
            } else {
                viewModel.editHabit(editHabit, name, category, goal, reminderTime)
            }

            parentFragmentManager.popBackStack()
        }

        return view
    }
}