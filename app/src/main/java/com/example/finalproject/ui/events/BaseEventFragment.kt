package com.example.finalproject.ui.events


import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.MainActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


abstract class BaseEventFragment : Fragment() {

    protected lateinit var adapterToday: EventAdapter
    protected lateinit var adapterPast: EventAdapter
    private var recyclerToday: RecyclerView? = null
    private var recyclerPast: RecyclerView? = null

    fun addEvent(event: EventItem) {
        if (!::adapterToday.isInitialized || !::adapterPast.isInitialized) return
        if (event.date == getTodayDate()) {
            adapterToday.addEventItem(event)
            recyclerToday?.scrollToPosition(0)
        }
        else {
            adapterPast.addEventItem(event)
            recyclerPast?.scrollToPosition(0)
        }
    }
    private fun getTodayDate(): String {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return todayDate.format(Date())
    }

    protected fun setupRecyclerViews(todayView: RecyclerView, pastView: RecyclerView) {
        adapterToday = EventAdapter(mutableListOf()) { pos ->
            val item = adapterToday.getItem(pos)
            adapterToday.deleteItem(pos)

            lifecycleScope.launch {
                MainActivity.db.eventDao().delete(item)
            }
        }
        adapterPast = EventAdapter(mutableListOf()) { pos ->
            val item = adapterPast.getItem(pos)
            adapterPast.deleteItem(pos)

            lifecycleScope.launch {
                MainActivity.db.eventDao().delete(item)
            }
        }
        recyclerToday = todayView
        recyclerPast = pastView

        todayView.layoutManager = LinearLayoutManager(requireContext())
        todayView.adapter = adapterToday

        pastView.layoutManager = LinearLayoutManager(requireContext())
        pastView.adapter = adapterPast

    }

    fun filterByQuery(query: String) {
        if (!::adapterToday.isInitialized || !::adapterPast.isInitialized) return

        Log.d("BaseEventFragment", "필터 실행됨: $query")
        adapterToday.filter(query)
        adapterPast.filter(query)
    }
}