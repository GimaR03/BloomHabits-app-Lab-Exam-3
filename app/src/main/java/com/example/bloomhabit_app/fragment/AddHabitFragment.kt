package com.example.bloomhabit_app.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.adapter.CategoryAdapter
import com.example.bloomhabit_app.model.CategoryItem
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddHabitFragment(private val editHabit: Habit?) : Fragment() {

    private val viewModel: HabitViewModel by activityViewModels()
    private var targetDateTime: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("AddHabitFragment", "onCreateView started")
        try {
            val view = inflater.inflate(R.layout.fragment_add_habit, container, false)

            val nameEdit: EditText = view.findViewById(R.id.edit_habit_name)
            val goalEdit: EditText = view.findViewById(R.id.edit_habit_goal)
            val categorySpinner: Spinner = view.findViewById(R.id.spinner_category)
            val btnSetReminder: Button = view.findViewById(R.id.btn_set_reminder)
            val tvReminder: TextView = view.findViewById(R.id.tv_reminder)
            val saveButton: Button = view.findViewById(R.id.save_habit)

            // FIXED: Using Android system icons that always exist
            val categories = listOf(
                CategoryItem("Health & Wellness", android.R.drawable.ic_secure),
                CategoryItem("Mental Health", android.R.drawable.ic_menu_edit),
                CategoryItem("Personal Growth", android.R.drawable.ic_menu_my_calendar),
                CategoryItem("Productivity", android.R.drawable.ic_menu_agenda),
                CategoryItem("Sport", android.R.drawable.ic_menu_compass),
                CategoryItem("Social Health", android.R.drawable.ic_menu_share),
                CategoryItem("Household Chores", android.R.drawable.ic_menu_save),
                CategoryItem("Better Sleep", android.R.drawable.ic_menu_day),
                CategoryItem("Other", android.R.drawable.ic_menu_more)
            )

            categorySpinner.adapter = CategoryAdapter(requireContext(), categories)

            // Load edits if in edit mode
            if (editHabit != null) {
                nameEdit.setText(editHabit.name)
                goalEdit.setText(editHabit.goalDescription)
                val index = categories.indexOfFirst { it.name == editHabit.category }
                if (index >= 0) categorySpinner.setSelection(index)
                targetDateTime = editHabit.targetDateTime
                targetDateTime?.let {
                    tvReminder.text = SimpleDateFormat("yyyy.MM.dd, hh:mm a", Locale.getDefault())
                        .format(Date(it))
                }
            }

            // Set date and time
            btnSetReminder.setOnClickListener {
                try {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(requireContext(), { _, y, m, d ->
                        TimePickerDialog(requireContext(), { _, h, min ->
                            cal.set(y, m, d, h, min, 0)
                            targetDateTime = cal.timeInMillis
                            tvReminder.text = SimpleDateFormat("yyyy.MM.dd, hh:mm a", Locale.getDefault())
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

                    if (targetDateTime == null) {
                        Toast.makeText(requireContext(), "Please set date and time", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (editHabit == null) {
                        // Add new habit
                        viewModel.addHabit(name, selectedCategory, goalDescription, targetDateTime)
                        Toast.makeText(requireContext(), "Habit saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        // Edit existing habit
                        viewModel.editHabit(editHabit, name, selectedCategory, goalDescription, targetDateTime)
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