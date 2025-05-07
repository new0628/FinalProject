package com.example.finalproject.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.finalproject.databinding.FragmentParkingEventBinding


class ParkingEventFragment : BaseEventFragment() {

    private var _binding: FragmentParkingEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkingEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews(binding.rViewToday, binding.rViewPast)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}