package com.example.bloomhabit_app.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.model.Memory

class MemoryDialogFragment(
    private val memory: Memory? = null,
    private val onSave: (Memory) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_memory, null)
        val titleEditText = view.findViewById<EditText>(R.id.memory_title_edit)
        val descriptionEditText = view.findViewById<EditText>(R.id.memory_description_edit)
        val moodSpinner = view.findViewById<Spinner>(R.id.mood_spinner)

        // Set up mood spinner
        val moods = arrayOf("Good", "Bad", "Neutral")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, moods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moodSpinner.adapter = adapter

        memory?.let {
            titleEditText.setText(it.title)
            descriptionEditText.setText(it.description)
            val moodPosition = moods.indexOf(it.mood)
            if (moodPosition >= 0) {
                moodSpinner.setSelection(moodPosition)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle(if (memory == null) "Add Memory" else "Edit Memory")
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val selectedMood = moodSpinner.selectedItem.toString()

                if (title.isNotEmpty()) {
                    val newMemory = memory?.copy(
                        title = title,
                        description = description,
                        mood = selectedMood
                    ) ?: Memory(
                        title = title,
                        description = description,
                        mood = selectedMood,
                        date = arguments?.getString("selectedDate") ?: ""
                    )
                    onSave(newMemory)
                } else {
                    Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        fun newInstance(selectedDate: String, memory: Memory? = null, onSave: (Memory) -> Unit): MemoryDialogFragment {
            val fragment = MemoryDialogFragment(memory, onSave)
            val args = Bundle()
            args.putString("selectedDate", selectedDate)
            fragment.arguments = args
            return fragment
        }
    }
}