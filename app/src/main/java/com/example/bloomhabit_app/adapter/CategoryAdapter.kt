package com.example.bloomhabit_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bloomhabit_app.R
import com.example.bloomhabit_app.model.CategoryItem

class CategoryAdapter(context: Context, private val items: List<CategoryItem>) :
    ArrayAdapter<CategoryItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_category_spinner, parent, false)

        val icon: ImageView = view.findViewById(R.id.category_icon)
        val name: TextView = view.findViewById(R.id.category_name)

        item?.let {
            icon.setImageResource(it.iconRes)
            name.text = it.name
        }

        return view
    }
}
