package com.example.finalproject.ui.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.finalproject.MainActivity

import com.example.finalproject.databinding.FragmentEventsBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class EventFragment : Fragment() {

    private lateinit var adapter: EventPageAdapter
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("주행시", "주차시")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventPageAdapter(requireActivity())
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 2

        lifecycleScope.launch {
            val allEvents = MainActivity.db.eventDao().getAll()
            val drivingItems = allEvents.filter { it.mode == "주행시" }
            val parkingItems = allEvents.filter { it.mode == "주차시" }

            drivingItems.forEach { adapter.drivingFragment.addEvent(it) }
            parkingItems.forEach { adapter.parkingFragment.addEvent(it) }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]

        }.attach()

//        // mqtt관련 // mqtt 필요없어지면 지워야함
//        mqttManager = MqttManager { event ->
//            lifecycleScope.launch {
//
//                MainActivity.db.eventDao().insert(event)
//                if (event.mode == "주차시") {
//                    adapter.parkingFragment.addEvent(event)
//                }
//                else {
//                    adapter.drivingFragment.addEvent(event)
//                }
//            }
//        }
//        mqttManager.connectAndSubscribe()

//        binding.exBtn.setOnClickListener {
//            Log.d("EventFragment", "exBtn눌림")
//            val newEvent = generateRandomEvent()
//            Log.d("EventFragment", "$newEvent")
//            lifecycleScope.launch {
//                MainActivity.db.eventDao().insert(newEvent)
//                if (newEvent.mode == "주차시") {
//                    adapter.parkingFragment.addEvent(newEvent)
//                }
//                else {
//                    adapter.drivingFragment.addEvent(newEvent)
//                }
//            }
//        }


        binding.root.setOnTouchListener { _, _ ->
            binding.root.performClick()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.eventSearchBar.windowToken, 0)
            binding.eventSearchBar.clearFocus()
            false
        }

        binding.eventSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.eventSearchBar.text.toString().trim()
                Log.d("EventFragment", "검색어 입력됨: $query")
                filterEventItems(query)

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.eventSearchBar.windowToken, 0)

                binding.eventSearchBar.clearFocus()

                true
            } else {
                false
            }
        }

//        binding.eventSearchBar.addTextChangedListener {
//            val query = it.toString().trim()
//            Log.d("EventFragment", "검색어 입력됨: $query")
//            filterEventItems(query)
//
//        }

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 검색창 비우고
                binding.eventSearchBar.text?.clear()
                // 빈 쿼리로 필터 → 전체 복원
                filterEventItems("")
            }
        })
    }



    private fun filterEventItems(query: String) {
        when (binding.viewPager.currentItem) {
            0 -> adapter.drivingFragment.filterByQuery(query)
            1 -> adapter.parkingFragment.filterByQuery(query)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.eventSearchBar.text?.clear()
        filterEventItems("")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // mqtt
        //mqttManager.disconnect()
        _binding = null
    }

    private fun generateRandomEvent(): EventItem {
        val modes = listOf("주차시", "주행시")
        val titles = listOf("충격감지", "문열림감지", "사고알림")
        val dates = listOf("2025-05-07", "2025-05-06", "2025-05-10")
        val colors = mapOf(
            "충격감지" to "#FF0000", // 빨강
            "문열림감지" to "#FFA500", // 주황
            "사고알림" to "#00FF00"  // 초록
        )

        val random = kotlin.random.Random
        val title = titles[random.nextInt(titles.size)]
        return EventItem(
            mode = modes[random.nextInt(modes.size)],
            title = title,
            date = dates[random.nextInt(dates.size)],
            color = colors[title] ?: "#FFFFFF"
        )
    }

    // mqtt 필요없어지면 지워야함
    fun addEventFromMqtt(event: EventItem) {
        if (event.mode == "주차시") {
            adapter.parkingFragment.addEvent(event)
        } else {
            adapter.drivingFragment.addEvent(event)
        }
    }
}