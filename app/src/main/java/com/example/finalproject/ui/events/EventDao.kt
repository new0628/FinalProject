package com.example.finalproject.ui.events

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventItem)

    @Query("SELECT * FROM event_table ORDER BY date DESC")
    suspend fun getAll(): List<EventItem>

    @Delete
    suspend fun delete(event: EventItem)

    @Query("DELETE FROM event_table")
    suspend fun deleteAll()
}