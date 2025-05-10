package com.example.finalproject.ui.events

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_table")
data class EventItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String,
    val title: String,
    val date: String,
    val color: String
)
