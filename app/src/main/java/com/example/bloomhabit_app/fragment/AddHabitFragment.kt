package com.example.bloomhabit_app.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.adapter.CategoryAdapter
import com.example.bloomhabit_app.model.CategoryItem
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.utils.AlarmHelper
import com.example.bloomhabit_app.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddHabitFragment(private val editHabit: Habit? = null) : Fragment() {

    private val viewModel: HabitViewModel by activityViewModels()
    private lateinit var alarmHelper: AlarmHelper

    private var targetDateTime: Long? = null
    private var isReminderEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("AddHabitFragment", "onCreateView started")
        try {
            val view = inflater.inflate(R.layout.fragment_add_habit, container, false)

            alarmHelper = AlarmHelper(requireContext())

            // Initialize views with correct IDs from your XML
            val nameEdit: EditText = view.findViewById(R.id.habit_name_edit)
            val goalEdit: EditText = view.findViewById(R.id.goal_description_edit)
            val categorySpinner: Spinner = view.findViewById(R.id.category_spinner)
            val btnSetReminder: Button = view.findViewById(R.id.reminder_time_button)
            val saveButton: Button = view.findViewById(R.id.save_button)
            val reminderSwitch: SwitchCompat = view.findViewById(R.id.reminder_switch)

            // Using your custom PNG images for categories
            val categories = listOf(
                CategoryItem("Health & Wellness", R.drawable.health),
                CategoryItem("Mental Health", R.drawable.mentalhealth),
                CategoryItem("Personal Growth", R.drawable.personalgrowth),
                CategoryItem("Productivity", R.drawable.productivity),
                CategoryItem("Sport", R.drawable.sport),
                CategoryItem("Social Health", R.drawable.socialhealth),
                CategoryItem("Household Chores", R.drawable.household),
                CategoryItem("Better Sleep", R.drawable.sleep),
                CategoryItem("Other", R.drawable.other)
            )

            categorySpinner.adapter = CategoryAdapter(requireContext(), categories)

            // Load edits if in edit mode
            if (editHabit != null) {
                nameEdit.setText(editHabit.name)
                goalEdit.setText(editHabit.goalDescription)
                val index = categories.indexOfFirst { it.name == editHabit.category }
                if (index >= 0) categorySpinner.setSelection(index)

                targetDateTime = editHabit.targetDateTime
                isReminderEnabled = editHabit.isReminderEnabled
                reminderSwitch.isChecked = isReminderEnabled

                // Set initial button states
                btnSetReminder.isEnabled = isReminderEnabled

                targetDateTime?.let {
                    btnSetReminder.text = SimpleDateFormat("yyyy.MM.dd, hh:mm a", Locale.getDefault())
                        .format(Date(it))
                } ?: run {
                    btnSetReminder.text = "Select Reminder Time"
                }

                // Update button text for edit mode
                saveButton.text = "Update Habit"
            }

            // Set up reminder switch
            reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
                isReminderEnabled = isChecked
                btnSetReminder.isEnabled = isChecked

                if (!isChecked) {
                    targetDateTime = null
                    btnSetReminder.text = "Select Reminder Time"
                }
            }

            // Set date and time
            btnSetReminder.setOnClickListener {
                if (!isReminderEnabled) {
                    Toast.makeText(requireContext(), "Please enable reminder first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(requireContext(), { _, y, m, d ->
                        TimePickerDialog(requireContext(), { _, h, min ->
                            cal.set(y, m, d, h, min, 0)

                            // If the selected time is in the past, set it for tomorrow
                            if (cal.timeInMillis <= System.currentTimeMillis()) {
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                            }

                            targetDateTime = cal.timeInMillis
                            btnSetReminder.text = SimpleDateFormat("yyyy.MM.dd, hh:mm a", Locale.getDefault())
                                .format(cal.time)
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                } catch (e: Exception) {
                    Log.e("AddHabitFragment", "Date picker error: ${e.message}")
                }
            }

            saveButton.setOnClickListener {
                try {
                    val name = nameEdit.text.toString().trim()
                    val goalDescription = goalEdit.text.toString().trim()
                    val selectedCategory = (categorySpinner.selectedItem as? CategoryItem)?.name ?: "Other"

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter habit name", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (goalDescription.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter goal description", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (isReminderEnabled && targetDateTime == null) {
                        Toast.makeText(requireContext(), "Please set date and time for reminder", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // For reminderTime, use targetDateTime when reminder is enabled, otherwise null
                    val reminderTime = if (isReminderEnabled) targetDateTime else null

                    if (editHabit == null) {
                        // Add new habit
                        viewModel.addHabit(
                            name = name,
                            category = selectedCategory,
                            goalDescription = goalDescription,
                            targetDateTime = targetDateTime,
                            reminderTime = reminderTime,
                            isReminderEnabled = isReminderEnabled
                        )

                        // Set alarm if reminder is enabled - get the latest habit ID
                        if (isReminderEnabled && targetDateTime != null) {
                            val habits = viewModel.habits.value ?: emptyList()
                            val newHabitId = (habits.maxOfOrNull { it.id } ?: 0) + 1
                            alarmHelper.setHabitAlarm(newHabitId, name, targetDateTime!!)
                        }

                        Toast.makeText(requireContext(), "Habit saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        // Edit existing habit
                        // Cancel existing alarm first
                        alarmHelper.cancelHabitAlarm(editHabit.id)

                        // Set new alarm if reminder is enabled
                        if (isReminderEnabled && targetDateTime != null) {
                            alarmHelper.setHabitAlarm(editHabit.id, name, targetDateTime!!)
                        }

                        viewModel.editHabit(
                            habit = editHabit,
                            name = name,
                            category = selectedCategory,
                            goalDescription = goalDescription,
                            targetDateTime = targetDateTime,
                            reminderTime = reminderTime,
                            isReminderEnabled = isReminderEnabled
                        )
                        Toast.makeText(requireContext(), "Habit updated successfully", Toast.LENGTH_SHORT).show()
                    }

                    // Go back to home screen
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    Log.e("AddHabitFragment", "Save error: ${e.message}")
                    Toast.makeText(requireContext(), "Error saving habit", Toast.LENGTH_SHORT).show()
                }
            }

            Log.d("AddHabitFragment", "onCreateView completed successfully")
            return view
        } catch (e: Exception) {
            Log.e("AddHabitFragment", "Error in onCreateView: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}