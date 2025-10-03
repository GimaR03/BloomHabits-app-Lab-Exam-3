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
    private lateinit var weeklySummaryText: TextView
    private lateinit var progressPercentage: TextView
    private lateinit var pieChartContainer: View
    private lateinit var memoriesSectionTitle: TextView
    private lateinit var addMemoryBtn: Button
    private var selectedDate: String? = null

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepOffset: Int = 0

    // Colors for pie chart
    private var completedColor: Int = 0
    private var pendingColor: Int = 0

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

        calendarView = root.findViewById(R.id.home_calendar)
        recyclerView = root.findViewById(R.id.habits_recycler)
        memoriesRecyclerView = root.findViewById(R.id.memories_recycler)
        todayHabitsText = root.findViewById(R.id.today_habits_text)
        weeklySummaryText = root.findViewById(R.id.weekly_summary_text)
        progressPercentage = root.findViewById(R.id.progress_percentage)
        pieChartContainer = root.findViewById(R.id.pie_chart_container)
        memoriesSectionTitle = root.findViewById(R.id.memories_section_title)
        addMemoryBtn = root.findViewById(R.id.add_memory_btn)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        memoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val addFab: FloatingActionButton = root.findViewById(R.id.add_habit_fab)

        // Initialize colors
        completedColor = ContextCompat.getColor(requireContext(), R.color.purple_200)
        pendingColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        selectedDate = dateFormat.format(Date())

        // Initialize HabitAdapter
        habitAdapter = HabitAdapter(
            emptyList(),
            onEdit = { habit ->
                // Navigate to AddHabitFragment for editing
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddHabitFragment(habit))
                    .addToBackStack(null)
                    .commit()
            },
            onDelete = { habit -> showDeleteConfirmation(habit) }
        )
        recyclerView.adapter = habitAdapter

        // Initialize MemoryAdapter
        memoryAdapter = MemoryAdapter(
            onEdit = { memory -> showMemoryDialog(memory) },
            onDelete = { memory -> showDeleteMemoryConfirmation(memory) }
        )
        memoriesRecyclerView.adapter = memoryAdapter

        addFab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddHabitFragment(null))
                .addToBackStack(null)
                .commit()
        }

        addMemoryBtn.setOnClickListener {
            showMemoryDialog()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(cal.time)
            updateHabits()
            updateMemories()
            updateWeeklyProgress()
        }

        setupPieChartBackground()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe habits changes
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            updateHabits()
            updateWeeklyProgress()
        }

        // Observe memories changes
        viewModel.memories.observe(viewLifecycleOwner) { memories ->
            updateMemories()
        }

        updateHabits()
        updateMemories()
        updateWeeklyProgress()
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

        // Show add memory button
        addMemoryBtn.visibility = View.VISIBLE
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

    private fun setupPieChartBackground() {
        pieChartContainer.post {
            val width = pieChartContainer.width
            val height = pieChartContainer.height
            if (width > 0 && height > 0) {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawPieChart(canvas, width, height, 0f)
                pieChartContainer.background = BitmapDrawable(resources, bitmap)
            }
        }
    }

    private fun drawPieChart(canvas: Canvas, width: Int, height: Int, progress: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) * 0.8f

        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        if (progress > 0) {
            val completedPaint = Paint().apply {
                color = completedColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val sweepAngle = 360f * (progress / 100f)
            canvas.drawArc(rect, -90f, sweepAngle, true, completedPaint)
        }

        val borderPaint = Paint().apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple_500)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
    }

    private fun updateWeeklyProgress() {
        val allHabits = viewModel.habits.value ?: emptyList()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        var totalPossibleHabits = 0
        var completedHabits = 0

        for (i in 0 until 7) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)

            val dayHabits = allHabits.count { habit ->
                habit.targetDateTime?.let { targetTime ->
                    dateFormat.format(Date(targetTime)) == date
                } ?: false
            }

            totalPossibleHabits += dayHabits
            completedHabits += (dayHabits * 0.7).toInt() // demo completion rate
        }

        val progress = if (totalPossibleHabits > 0) {
            (completedHabits * 100) / totalPossibleHabits
        } else 0

        progressPercentage.text = "$progress%"
        weeklySummaryText.text = "Completed $completedHabits out of $totalPossibleHabits habits this week"
        updatePieChart(progress.toFloat())
    }

    private fun updatePieChart(progress: Float) {
        pieChartContainer.post {
            val width = pieChartContainer.width
            val height = pieChartContainer.height
            if (width > 0 && height > 0) {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawPieChart(canvas, width, height, progress)
                pieChartContainer.background = BitmapDrawable(resources, bitmap)
            }
        }
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
            stepOffset = 0
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
            } else registerSensor()
        } else registerSensor()
    }
}