package com.example.bloomhabit_app.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.model.DailyFeeling

class FeelingSelectionDialogFragment(
    private val onFeelingSelected: (DailyFeeling) -> Unit
) : DialogFragment() {

    private val feelings = listOf(
        "😢" to "Sad",
        "😴" to "Tired",
        "😖" to "Stressed",
        "😠" to "Angry",
        "😤" to "Frustrated",
        "😞" to "Lonely",
        "😟" to "Anxious",
        "😔" to "Disappointed",
        "😩" to "Overwhelmed",
        "🤒" to "Sick",
        "😕" to "Confused"
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("How are you feeling today?")

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_feeling_selection, null)

        val container = view.findViewById<ViewGroup>(R.id.feelings_container)

        feelings.forEach { (emoji, feelingName) ->
            val feelingView = inflater.inflate(R.layout.item_feeling, container, false)

            val emojiText = feelingView.findViewById<TextView>(R.id.feeling_emoji)
            val nameText = feelingView.findViewById<TextView>(R.id.feeling_name)

            emojiText.text = emoji
            nameText.text = feelingName

            feelingView.setOnClickListener {
                val selectedDate = arguments?.getString("selectedDate") ?: ""
                val dailyFeeling = DailyFeeling(selectedDate, emoji, feelingName)
                onFeelingSelected(dailyFeeling)
                dismiss()
            }

            container.addView(feelingView)
        }

        builder.setView(view)
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }

    companion object {
        fun newInstance(selectedDate: String, onFeelingSelected: (DailyFeeling) -> Unit): FeelingSelectionDialogFragment {
            val fragment = FeelingSelectionDialogFragment(onFeelingSelected)
            val args = Bundle()
            args.putString("selectedDate", selectedDate)
            fragment.arguments = args
            return fragment
        }
    }
}