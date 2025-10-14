package com.example.bloomhabit_app.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.adapter.HabitAdapter
import com.example.bloomhabit_app.adapter.MemoryAdapter
import com.example.bloomhabit_app.model.DailyFeeling
import com.example.bloomhabit_app.model.Habit
import com.example.bloomhabit_app.model.Memory
import com.example.bloomhabit_app.viewmodel.HabitViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), SensorEventListener {

    private val viewModel: HabitViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var memoriesRecyclerView: RecyclerView
    private lateinit var calendarView: CalendarView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var memoryAdapter: MemoryAdapter
    private lateinit var todayHabitsText: TextView
    private lateinit var progressPercentage: TextView
    private lateinit var pieChartContainer: View
    private lateinit var categorySummaryText: TextView
    private lateinit var memoriesSectionTitle: TextView
    private lateinit var addMemoryBtn: Button
    private lateinit var feelingContainer: View
    private lateinit var selectedFeelingEmoji: TextView
    private lateinit var selectedFeelingText: TextView
    private lateinit var todayDateText: TextView
    private var selectedDate: String? = null

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    // Colors for category pie chart - FIXED COLOR DEFINITIONS
    private val categoryColors = listOf(
        Color.parseColor("#4CAF50"), // Green - Health & Wellness
        Color.parseColor("#2196F3"), // Blue - Mental Health
        Color.parseColor("#9C27B0"), // Purple - Personal Growth
        Color.parseColor("#FF9800"), // Orange - Productivity
        Color.parseColor("#F44336"), // Red - Sport
        Color.parseColor("#FFEB3B"), // Yellow - Social Health
        Color.parseColor("#795548"), // Brown - Household Chores
        Color.parseColor("#3F51B5"), // Indigo - Better Sleep
        Color.parseColor("#9E9E9E")  // Gray - Other
    )

    private val categoryNames = listOf(
        "Health & Wellness",
        "Mental Health",
        "Personal Growth",
        "Productivity",
        "Sport",
        "Social Health",
        "Household Chores",
        "Better Sleep",
        "Other"
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            registerSensor()
        } else {
            Toast.makeText(
                requireContext(),
                "Step permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize all views
        calendarView = root.findViewById(R.id.home_calendar)
        recyclerView = root.findViewById(R.id.habits_recycler)
        memoriesRecyclerView = root.findViewById(R.id.memories_recycler)
        todayHabitsText = root.findViewById(R.id.today_habits_text)
        progressPercentage = root.findViewById(R.id.progress_percentage)
        pieChartContainer = root.findViewById(R.id.pie_chart_container)
        categorySummaryText = root.findViewById(R.id.category_summary_text)
        memoriesSectionTitle = root.findViewById(R.id.memories_section_title)
        addMemoryBtn = root.findViewById(R.id.add_memory_btn)
        feelingContainer = root.findViewById(R.id.feeling_container)
        selectedFeelingEmoji = root.findViewById(R.id.selected_feeling_emoji)
        selectedFeelingText = root.findViewById(R.id.selected_feeling_text)
        todayDateText = root.findViewById(R.id.today_date_text)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        memoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val addFab: FloatingActionButton = root.findViewById(R.id.add_habit_fab)

        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        selectedDate = dateFormat.format(Date())

        // Set today's date
        todayDateText.text = dayFormat.format(Date())

        // Initialize adapters
        habitAdapter = HabitAdapter(
            emptyList(),
            onEdit = { habit ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddHabitFragment(habit))
                    .addToBackStack(null)
                    .commit()
            },
            onDelete = { habit -> showDeleteConfirmation(habit) }
        )
        recyclerView.adapter = habitAdapter

        memoryAdapter = MemoryAdapter(
            onEdit = { memory -> showMemoryDialog(memory) },
            onDelete = { memory -> showDeleteMemoryConfirmation(memory) }
        )
        memoriesRecyclerView.adapter = memoryAdapter

        // Set up click listeners
        addFab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddHabitFragment(null))
                .addToBackStack(null)
                .commit()
        }

        addMemoryBtn.setOnClickListener {
            showMemoryDialog()
        }

        feelingContainer.setOnClickListener {
            showFeelingSelectionDialog()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(cal.time)
            updateHabits()
            updateMemories()
            updateFeeling()
            updateCategoryChart()
        }

        // Initialize chart after view is created
        pieChartContainer.post {
            updateCategoryChart()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe data changes
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            updateHabits()
            updateCategoryChart()
        }

        viewModel.memories.observe(viewLifecycleOwner) { memories ->
            updateMemories()
        }

        viewModel.dailyFeelings.observe(viewLifecycleOwner) { feelings ->
            updateFeeling()
        }

        updateHabits()
        updateMemories()
        updateFeeling()
        updateCategoryChart()
    }

    private fun updateHabits() {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val currentDate = selectedDate ?: dateFormat.format(Date())
        val allHabits = viewModel.habits.value ?: emptyList()

        val filtered = allHabits.filter { habit ->
            habit.targetDateTime?.let { targetTime ->
                dateFormat.format(Date(targetTime)) == currentDate
            } ?: false
        }

        habitAdapter.submitList(filtered)
        todayHabitsText.text = "Today's Habits: ${filtered.size}"
    }

    private fun updateMemories() {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val currentDate = selectedDate ?: dateFormat.format(Date())
        val allMemories = viewModel.memories.value ?: emptyList()

        val filtered = allMemories.filter { it.date == currentDate }

        memoryAdapter.submitList(filtered)

        // Update memories section title
        val today = dateFormat.format(Date())
        val title = if (currentDate == today) {
            "Today's Memories"
        } else {
            "Memories for $currentDate"
        }
        memoriesSectionTitle.text = title
    }

    private fun updateFeeling() {
        val currentDate = selectedDate ?: return
        val feeling = viewModel.getFeelingForDate(currentDate)

        if (feeling != null) {
            selectedFeelingEmoji.text = feeling.emoji
            selectedFeelingText.text = feeling.feelingName
        } else {
            selectedFeelingEmoji.text = "😊"
            selectedFeelingText.text = "Tap to select your feeling"
        }
    }

    private fun updateCategoryChart() {
        pieChartContainer.post {
            val width = pieChartContainer.width
            val height = pieChartContainer.height
            if (width > 0 && height > 0) {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawCategoryChart(canvas, width, height)
                pieChartContainer.background = BitmapDrawable(resources, bitmap)
            }
        }
    }
//draw darck drawing link
    private fun drawCategoryChart(canvas: Canvas, width: Int, height: Int) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) * 0.8f
        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Get today's habits and calculate category percentages
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val currentDate = selectedDate ?: dateFormat.format(Date())
        val allHabits = viewModel.habits.value ?: emptyList()

        val todayHabits = allHabits.filter { habit ->
            habit.targetDateTime?.let { targetTime ->
                dateFormat.format(Date(targetTime)) == currentDate
            } ?: false
        }

        if (todayHabits.isEmpty()) {
            // Draw empty state
            val emptyPaint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(centerX, centerY, radius, emptyPaint)

            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("No habits", centerX, centerY, textPaint)
            categorySummaryText.text = "No habits scheduled for today"
            progressPercentage.text = "0%"
            return
        }

        // Calculate category distribution
        val categoryCount = todayHabits.groupBy { it.category }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }

        val totalHabits = todayHabits.size.toFloat()

        // Draw white background first in screen
        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw pie chart segments with different colors
        var startAngle = -90f // Start from top (12 o'clock position)

        categoryCount.forEachIndexed { index, (category, count) ->
            val sweepAngle = 360f * (count / totalHabits)

            // Get color based on category name or use index as fallback
            val colorIndex = categoryNames.indexOf(category)
            val color = if (colorIndex != -1 && colorIndex < categoryColors.size) {
                categoryColors[colorIndex]
            } else {
                // Fallback: use index modulo colors length
                categoryColors[index % categoryColors.size]
            }

            val segmentPaint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // Draw the segment
            canvas.drawArc(rect, startAngle, sweepAngle, true, segmentPaint)
            startAngle += sweepAngle
        }

        // Draw border around the pie chart
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Update category summary text and percentage
        updateCategorySummaryText(categoryCount, totalHabits.toInt())
    }

    private fun updateCategorySummaryText(categoryCount: List<Pair<String, Int>>, totalHabits: Int) {
        if (totalHabits == 0) {
            categorySummaryText.text = "No habits scheduled for today"
            progressPercentage.text = "0%"
            return
        }

        val summary = StringBuilder("Today's Categories:\n")
        categoryCount.forEach { (category, count) ->
            val percentage = (count * 100) / totalHabits
            summary.append("• $category: $percentage% ($count)\n")
        }

        categorySummaryText.text = summary.toString().trim()
        progressPercentage.text = "${totalHabits} Habits"
    }

    private fun showFeelingSelectionDialog() {
        val selectedDate = selectedDate ?: return
        val dialog = FeelingSelectionDialogFragment.newInstance(selectedDate) { feeling ->
            viewModel.addOrUpdateFeeling(feeling)
            Toast.makeText(requireContext(), "Feeling saved", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "feeling_dialog")
    }

    private fun showMemoryDialog(memory: Memory? = null) {
        val selectedDate = selectedDate ?: return
        val dialog = MemoryDialogFragment.newInstance(selectedDate, memory) { newMemory ->
            if (memory == null) {
                viewModel.addMemory(newMemory)
                Toast.makeText(requireContext(), "Memory added", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updateMemory(newMemory)
                Toast.makeText(requireContext(), "Memory updated", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show(parentFragmentManager, "memory_dialog")
    }

    private fun showDeleteMemoryConfirmation(memory: Memory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Memory")
            .setMessage("Are you sure you want to delete this memory?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMemory(memory)
                Toast.makeText(
                    requireContext(),
                    "Memory deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete ${habit.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHabit(habit)
                Toast.makeText(
                    requireContext(),
                    "Habit ${habit.name} deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (hasStepsHabit()) requestPermission()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun hasStepsHabit(): Boolean {
        return viewModel.habits.value?.any { it.name.contains("steps", true) } ?: false
    }

    private fun registerSensor() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                registerSensor()
            }
        } else {
            registerSensor()
        }
    }

    // Refresh method for MainActivity
    fun refreshData() {
        updateHabits()
        updateMemories()
        updateFeeling()
        updateCategoryChart()
    }
}