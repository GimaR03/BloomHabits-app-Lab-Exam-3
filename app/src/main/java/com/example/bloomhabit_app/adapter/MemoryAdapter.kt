package com.example.bloomhabit_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.model.Memory

class MemoryAdapter(
    private val onEdit: (Memory) -> Unit,
    private val onDelete: (Memory) -> Unit
) : ListAdapter<Memory, MemoryAdapter.MemoryViewHolder>(MemoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = getItem(position)
        holder.bind(memory)
    }

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.memory_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.memory_description)
        private val moodIcon: ImageView = itemView.findViewById(R.id.mood_icon)

        fun bind(memory: Memory) {
            titleTextView.text = memory.title
            descriptionTextView.text = memory.description

            // Set mood icon - using simple colors for now
            val moodColor = when (memory.mood) {
                "Good" -> R.color.green
                "Bad" -> R.color.red
                else -> R.color.gray
            }
            moodIcon.setBackgroundColor(itemView.context.getColor(moodColor))

            itemView.setOnClickListener {
                onEdit(memory)
            }

            itemView.setOnLongClickListener {
                onDelete(memory)
                true
            }
        }
    }
}

class MemoryDiffCallback : DiffUtil.ItemCallback<Memory>() {
    override fun areItemsTheSame(oldItem: Memory, newItem: Memory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Memory, newItem: Memory): Boolean {
        return oldItem == newItem
    }
}