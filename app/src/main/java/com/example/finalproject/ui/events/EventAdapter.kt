package com.example.finalproject.ui.events

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R

class EventAdapter(private val items: MutableList<EventItem>,
    private val onDeleteClicked: (position: Int) -> Unit) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val originalItems = mutableListOf<EventItem>()

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.eventTitle)
        val dateText: TextView = itemView.findViewById(R.id.eventDate)
        val colorStrip: View = itemView.findViewById(R.id.colorStrip)
        private val optionBtn: ImageButton = itemView.findViewById(R.id.optionBtn)


        init {
            optionBtn.setOnClickListener {
                val popup = PopupMenu(itemView.context, optionBtn)
                popup.menu.add("삭제하기")
                popup.setOnMenuItemClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onDeleteClicked(adapterPosition)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = items[position]
        holder.titleText.text = event.title
        holder.dateText.text = event.date

        try {
            holder.colorStrip.setBackgroundColor(android.graphics.Color.parseColor(event.color))
        } catch (e: IllegalArgumentException) {
            holder.colorStrip.setBackgroundColor(android.graphics.Color.GRAY)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addEventItem(event: EventItem) {
        Log.d("EventAdapter", "addEventItem: $event")
        originalItems.add(0, event)
        items.add(0, event)
        notifyItemInserted(0)
    }

    fun clearAll() {
        items.clear()
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        if (position in items.indices) {
            val deleted = items.removeAt(position)
            originalItems.remove(deleted)
            notifyItemRemoved(position)
        }
    }

    fun setItems(newItems: List<EventItem>) {
        originalItems.clear()
        originalItems.addAll(newItems)
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filtered = if (query.isEmpty()) {
            originalItems
        } else {
            originalItems.filter { it.title.contains(query, ignoreCase = true) }
        }
        Log.d("EventAdapter", "필터링 결과: ${filtered.size}개")
        items.clear()
        items.addAll(filtered)
        notifyDataSetChanged()
    }
}