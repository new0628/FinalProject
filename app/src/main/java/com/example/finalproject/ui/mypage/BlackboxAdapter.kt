package com.example.finalproject.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.ui.events.EventItem

class BlackboxAdapter(
    private val onClick: (EventItem) -> Unit
) : RecyclerView.Adapter<BlackboxAdapter.VH>() {

    private val items = mutableListOf<EventItem>()

    fun submitList(list: List<EventItem>) {
        items.clear(); items += list
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvMode  = v.findViewById<TextView>(R.id.eventTitle)   // 재활용
        private val tvExtra = v.findViewById<TextView>(R.id.eventDate)

        init {
            v.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    // 로그 출력 추가
                    val item = items[pos]
                    android.util.Log.d("BlackboxItemClick", """
                        ▶ id: ${item.id}
                        ▶ mode: ${item.mode}
                        ▶ title: ${item.title}
                        ▶ date: ${item.date}
                        ▶ imagePath: ${item.imagePath}
                    """.trimIndent())
                    onClick(items[pos])
                }
            }
        }
        fun bind(e: EventItem) {
            tvMode.text  = e.title           // 예: 주행시 / 주차시
            tvExtra.text = "${e.mode} • ${e.date}"
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_event, p, false))

    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
}