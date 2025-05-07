package com.example.finalproject.ui.events

import android.media.metrics.Event
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
        adapterToday = EventAdapter(mutableListOf()) { pos -> adapterToday.deleteItem(pos) }
        adapterPast = EventAdapter(mutableListOf()) { pos -> adapterPast.deleteItem(pos) }
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