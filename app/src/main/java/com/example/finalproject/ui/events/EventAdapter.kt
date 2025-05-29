package com.example.finalproject.ui.events
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.ui.events.EventItem

class EventAdapter(
    private val onDelete: (EventItem) -> Unit,
    private val onItemClick: (EventItem) -> Unit
) : RecyclerView.Adapter<EventAdapter.VH>() {

    //  실제 화면에 보여질 데이터
    private val items = mutableListOf<EventItem>()

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.eventTitle)
        private val date  = v.findViewById<TextView>(R.id.eventDate)
        private val strip = v.findViewById<View>(R.id.colorStrip)
        private val option= v.findViewById<ImageButton>(R.id.optionBtn)

        init {
            option.setOnClickListener {
                PopupMenu(v.context, option).apply {
                    menu.add("삭제하기")
                    setOnMenuItemClickListener {
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION)
                            onDelete(items[pos])
                        true
                    }
                }.show()
            }
            v.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val e = items[pos]
                    Log.d("EventItemClick",
                        "mode:${e.mode}, id:${e.id}, title:${e.title}, date:${e.date}, imagePath:${e.imagePath}")

                    onItemClick(e)
                }
            }
        }
        // 색 통일하고 싶으면
        fun bind(e: EventItem) {
            title.text = e.title
            date.text  = e.date
            runCatching { strip.setBackgroundColor(e.color.toColorInt()) }
                .onFailure { strip.setBackgroundColor(0xFFCCCCCC.toInt()) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(
            LayoutInflater.from(p.context)
            .inflate(R.layout.item_event, p, false))

    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size

    fun setItems(newItems: List<EventItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}