package com.example.finalproject.ui.events


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged

import androidx.fragment.app.Fragment

import androidx.viewpager2.widget.ViewPager2


import com.example.finalproject.databinding.FragmentEventsBinding
import com.google.android.material.tabs.TabLayoutMediator


class EventFragment : Fragment() {

    private lateinit var adapter: EventPageAdapter
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private var currentQuery: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    //@SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventPageAdapter(requireActivity())
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "주행시" else "주차시"
        }.attach()

        binding.eventSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.eventSearchBar.text.toString().trim()
                currentQuery = query
                filterEventItems(query)
                val imm = requireContext().getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(binding.eventSearchBar.windowToken, 0)
                binding.eventSearchBar.clearFocus()
                true
            } else false
        }
        binding.eventSearchBar.doOnTextChanged { text, _, _, _ ->
            val q = text?.trim()?.toString() ?: ""
            currentQuery = q
            filterEventItems(q)
        }

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
        _binding = null
        super.onDestroyView()

    }
}