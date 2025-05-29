// HomeViewModel.kt

package com.example.finalproject.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// Socket 관리
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    //private val repo = HomeRepository(app)


    private val _rubikpiMac = MutableLiveData<String?>()
    val rubikpiMac: LiveData<String?> get() = _rubikpiMac

    fun setRubikpiMac(address: String) {
        _rubikpiMac.value = address
    }


}