package com.example.finalproject.ui.events

import android.app.Application
import androidx.lifecycle.*

import kotlinx.coroutines.launch

class EventViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = EventDatabase.getInstance(app).eventDao()

    // 2. DB 에서 한 번에 전체 리스트를 꺼내는 suspend 함수
    suspend fun getAllEvents(): List<EventItem> = dao.getAll()

    /** 외부 호출용 CRUD */
    fun insert(item: EventItem) = viewModelScope.launch { dao.insert(item) }
    fun delete(item: EventItem) = viewModelScope.launch { dao.delete(item) }
}