
package com.example.finalproject.ui

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


// MainActivity 확장
fun FragmentActivity.setupCustomTabs(tabItems: List<TabItem>, onTabSelected: (Fragment) -> Unit) {
    tabItems.forEachIndexed { index, tab ->
        findViewById<View>(tab.containerId).setOnClickListener {
            selectTab(tabItems, index, this, onTabSelected)
        }
    }
    selectTab(tabItems, 0, this, onTabSelected)
}

private fun selectTab(
    tabItems: List<TabItem>,
    index: Int,
    activity: Activity,
    onTabSelected: (Fragment) -> Unit
) {
    tabItems.forEachIndexed { i, tab ->
        val icon = activity.findViewById<ImageView>(tab.iconViewId)
        val label = activity.findViewById<TextView>(tab.labelViewId)
        val isSelected = i == index

        icon.setImageResource(if (isSelected) tab.iconSelected else tab.iconUnselected)
        label.setTextColor(if (isSelected) Color.BLUE else Color.GRAY)

        if (isSelected) {
            onTabSelected(tab.fragment)
        }
    }
}
