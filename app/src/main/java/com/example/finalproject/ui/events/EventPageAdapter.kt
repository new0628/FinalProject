package com.example.finalproject.ui.events

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EventPageAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val drivingFragment = DrivingEventFragment()
    val parkingFragment = ParkingEventFragment()

    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 ->drivingFragment //주행시
            1 -> parkingFragment //주차시
            else -> throw IndexOutOfBoundsException("인덱스 에러")
        }
    }
}