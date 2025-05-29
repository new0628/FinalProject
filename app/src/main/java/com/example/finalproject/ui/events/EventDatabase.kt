package com.example.finalproject.ui.events


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EventItem::class], version = 2, exportSchema = false)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile private var INSTANCE: EventDatabase? = null

        fun getInstance(context: Context): EventDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_db"
                )
                    // 개발 중이면
                    .fallbackToDestructiveMigration(true) //<- 앱 실행시마다 db지우고싶으면 true
                    .build()
                    .also { INSTANCE = it }
            }
    }
}