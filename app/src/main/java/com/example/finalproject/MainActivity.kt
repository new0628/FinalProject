package com.example.finalproject
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import com.example.finalproject.ui.events.EventFragment
import com.example.finalproject.ui.home.HomeFragment
import com.example.finalproject.ui.mypage.MyPageFragment
import com.example.finalproject.databinding.ActivityMainBinding
import com.example.finalproject.ui.TabItem
import com.example.finalproject.ui.events.EventDatabase
import com.example.finalproject.ui.setupCustomTabs

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat

import com.example.finalproject.ui.home.BluetoothService


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var db: EventDatabase


    }
    // 권한 요청 런처
    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.w("MainActivity", "SMS 권한이 거부되었습니다.")
        }
    }

    // 바인딩
    private lateinit var binding: ActivityMainBinding
    // 탭 항목 리스트
    private lateinit var tabItems: List<TabItem>

    // 화면 포커스 변경 시 시스템 네비게이션바 숨김
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    // 초기 설정
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = EventDatabase.getInstance(applicationContext)
        requestNotificationPermission()
        // SMS 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestSmsPermission.launch(Manifest.permission.SEND_SMS)
        }
        tabItems = listOf( //tabitem만 추가하면됨
            TabItem(R.id.nav_home, R.id.icon_home, R.id.label_home, R.drawable.main_home_selected, R.drawable.main_home, "Home", HomeFragment()),
            TabItem(R.id.nav_events, R.id.icon_events, R.id.label_events, R.drawable.event_page_selected, R.drawable.event_page, "Events", EventFragment()),
            TabItem(R.id.nav_mypage, R.id.icon_mypage, R.id.label_mypage, R.drawable.my_page_selected, R.drawable.my_page, "MyPage", MyPageFragment())
        )
        // 탭 클릭 시 프래그먼트 전환
        setupCustomTabs(tabItems) { fragment ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, BluetoothService::class.java)
        if (isMyServiceRunning(BluetoothService::class.java)) {
            stopService(serviceIntent)
        }
    }
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == serviceClass.name
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

}
