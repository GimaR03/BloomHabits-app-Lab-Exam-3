package com.example.bloomhabit_app.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.adapter.HabitAdapter
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.viewmodel.HabitViewModel

class ReportsFragment : Fragment() {

    private val viewModel: HabitViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var totalHabitsText: TextView
    private lateinit var categoriesText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ReportsFragment", "onCreateView started")
        try {
            val view = inflater.inflate(R.layout.fragment_reports, container, false)

            recyclerView = view.findViewById(R.id.reports_recycler)
            totalHabitsText = view.findViewById(R.id.total_habits_text)
            categoriesText = view.findViewById(R.id.categories_text)

            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Adapter with edit/delete actions for ALL habits
            habitAdapter = HabitAdapter(
                emptyList(),
                onEdit = { habit ->
                    try {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AddHabitFragment(habit))
                            .addToBackStack(null)
                            .commit()
                        Log.d("ReportsFragment", "Opening edit screen for: ${habit.name}")
                    } catch (e: Exception) {
                        Log.e("ReportsFragment", "Error opening AddHabitFragment: ${e.message}")
                    }
                },
                onDelete = { habit ->
                    showDeleteConfirmation(habit)
                }
            )
            recyclerView.adapter = habitAdapter

            Log.d("ReportsFragment", "onCreateView completed successfully")
            return view
        } catch (e: Exception) {
            Log.e("ReportsFragment", "Error in onCreateView: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe all habits
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            updateReports(habits)
        }
    }

    private fun updateReports(habits: List<Habit>) {
        try {
            // Show ALL habits in reports (not filtered by date)
            habitAdapter.submitList(habits.sortedBy { it.name })

            // Update statistics
            updateCategoryStatistics(habits)

            Log.d("ReportsFragment", "Showing ${habits.size} habits")
        } catch (e: Exception) {
            Log.e("ReportsFragment", "Error updating reports: ${e.message}")
        }
    }

    private fun updateCategoryStatistics(habits: List<Habit>) {
        val totalHabits = habits.size
        val categories = habits.map { it.category }.distinct()

        totalHabitsText.text = "Total Habits: $totalHabits"

        if (totalHabits > 0) {
            val categoryCount = habits.groupBy { it.category }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }

            val categoryStats = categoryCount.joinToString(" • ") { (category, count) ->
                val percentage = (count * 100) / totalHabits
                "$category: $percentage%"
            }
            categoriesText.text = "Categories: $categoryStats"
        } else {
            categoriesText.text = "Categories: None"
        }
    }

    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete ${habit.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHabit(habit)
                Toast.makeText(requireContext(), "Habit ${habit.name} deleted", Toast.LENGTH_SHORT).show()
                Log.d("ReportsFragment", "Habit deleted: ${habit.name}")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}