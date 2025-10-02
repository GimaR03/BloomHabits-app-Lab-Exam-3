package com.example.bloomhabit_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.model.Habit
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(
    private var habits: List<Habit>,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    fun submitList(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit)
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val habitName: TextView = itemView.findViewById(R.id.habit_name)
        private val habitGoal: TextView = itemView.findViewById(R.id.habit_goal)
        private val habitTime: TextView = itemView.findViewById(R.id.habit_time)
        private val habitCategory: TextView = itemView.findViewById(R.id.habit_category)
        private val editBtn: ImageButton = itemView.findViewById(R.id.edit_habit)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.delete_habit)
        private val icon: ImageView = itemView.findViewById(R.id.habit_icon)

        fun bind(habit: Habit) {
            habitName.text = habit.name
            habitGoal.text = habit.goalDescription
            habitCategory.text = habit.category

            // Format date and time
            habit.targetDateTime?.let {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                habitTime.text = dateFormat.format(Date(it))
            } ?: run {
                habitTime.text = "Not set"
            }

            // Set category-based icons using your custom PNG images
            setCategoryIcon(habit.category)

            // Button clicks
            editBtn.setOnClickListener { onEdit(habit) }
            deleteBtn.setOnClickListener { onDelete(habit) }
        }

        private fun setCategoryIcon(category: String) {
            val iconRes = when (category) {
                "Health & Wellness" -> R.drawable.health
                "Mental Health" -> R.drawable.mentalhealth
                "Personal Growth" -> R.drawable.personalgrowth
                "Productivity" -> R.drawable.productivity
                "Sport" -> R.drawable.sport
                "Social Health" -> R.drawable.socialhealth
                "Household Chores" -> R.drawable.household
                "Better Sleep" -> R.drawable.sleep
                else -> R.drawable.other
            }
            icon.setImageResource(iconRes)
        }
    }
}