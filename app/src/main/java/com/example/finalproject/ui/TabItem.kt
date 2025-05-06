package com.example.finalproject.ui

import androidx.fragment.app.Fragment

//ocp 적용위해
data class TabItem(
    val containerId: Int,        // 프래그먼트가 표시될 컨테이너 뷰 ID
    val iconViewId: Int,         // 아이콘 뷰 ID
    val labelViewId: Int,        // 텍스트 라벨 뷰 ID
    val iconSelected: Int,       // 선택 상태일 때 아이콘 리소스
    val iconUnselected: Int,     // 비선택 상태일 때 아이콘 리소스
    val label: String,           // 라벨 텍스트
    val fragment: Fragment       // 연결된 프래그먼트 객체
)
