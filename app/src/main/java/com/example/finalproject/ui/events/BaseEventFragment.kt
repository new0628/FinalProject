package com.example.finalproject.ui.events

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseEventFragment : Fragment() {

    protected abstract val modeFilter: String
    private var currentQuery: String = ""

    protected lateinit var adapterToday: EventAdapter
    protected lateinit var adapterPast: EventAdapter
    private val vm: EventViewModel by lazy {
        ViewModelProvider(requireActivity())[EventViewModel::class.java]
    }

    private fun normalizeMode(m: String) = when (m.lowercase()) {
        "driving", "drive", "주행중" -> "주행시"
        "parking", "park", "주차중" -> "주차시"
        else -> m
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews(
            todayRv = view.findViewById(R.id.rViewToday),
            pastRv  = view.findViewById(R.id.rViewPast)
        )
        refreshEvents()
    }

    override fun onResume() {
        super.onResume()
        refreshEvents()
    }

    fun filterByQuery(q: String) {
        currentQuery = q
        if (!isResumed || view == null) return
        refreshEvents()
    }

    protected fun setupRecyclerViews(todayRv: RecyclerView, pastRv: RecyclerView) {
        adapterToday = EventAdapter(
            onDelete = { event ->
                lifecycleScope.launch {
                    MainActivity.db.eventDao().delete(event)
                    refreshEvents()       // ← 삭제 후 즉시 화면 갱신
                } },
            onItemClick = { event -> showImageDialog(event) }     // 연결
        )
        adapterPast  = EventAdapter (
            onDelete = { event ->
                lifecycleScope.launch {
                    MainActivity.db.eventDao().delete(event)
                    refreshEvents()       // ← 삭제 후 즉시 화면 갱신
                } },
            onItemClick = { event -> showImageDialog(event) }     // 연결
        )

        todayRv.layoutManager = LinearLayoutManager(requireContext())
        pastRv .layoutManager = LinearLayoutManager(requireContext())
        todayRv.adapter       = adapterToday
        pastRv .adapter       = adapterPast
    }


     private fun refreshEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            val all = vm.getAllEvents()
            val byMode = all.filter { normalizeMode(it.mode) == modeFilter }

            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayList = byMode.filter {
                it.date == todayKey && it.title.contains(currentQuery, ignoreCase = true)
            }
            val pastList = byMode.filter {
                it.date != todayKey && it.title.contains(currentQuery, ignoreCase = true)
            }

            adapterToday.setItems(todayList)
            adapterPast .setItems(pastList)
        }
    }
    private fun showImageDialog(event: EventItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_image, null)
        val img = dialogView.findViewById<ImageView>(R.id.imgEvent)

        // Glide 권장 (androidx.appcompat:appcompat 1.6.1 이상이면 가능)
        Glide.with(this)
            .load(File(event.imagePath))   // 내부저장소 경로
            .error(R.drawable.profile_image)   // 로드 실패 시 대체 이미지
            .into(img)

        AlertDialog.Builder(requireContext())
            .setTitle("${event.title}  •  ${event.date}")
            .setView(dialogView)
            .setPositiveButton("닫기", null)
            .show()
    }
}
