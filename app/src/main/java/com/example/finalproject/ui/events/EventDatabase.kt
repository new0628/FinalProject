package com.example.finalproject.ui.events


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EventItem::class], version = 1, exportSchema = false)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}