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
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.finalproject.ui.events.EventFragment
import com.example.finalproject.ui.home.HomeFragment
import com.example.finalproject.ui.mypage.MyPageFragment
import com.example.finalproject.databinding.ActivityMainBinding
import com.example.finalproject.ui.TabItem
import com.example.finalproject.ui.events.EventDatabase
import com.example.finalproject.ui.setupCustomTabs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.content.Context


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var db: EventDatabase
        private const val TARGET_MODE = "주행시"
        private const val TARGET_DATE = "2025-05-11"
        private const val SMS_TARGET_NUMBER = "01039489203"
    }
    // 권한 요청 런처
    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.w("MainActivity", "SMS 권한이 거부되었습니다.")
        }
    }

    private lateinit var mqttManager: MqttManager
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

        db = Room.databaseBuilder(
            applicationContext,
            EventDatabase::class.java,
            "event_db"
        ).build()

        // SMS 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestSmsPermission.launch(Manifest.permission.SEND_SMS)
        }

        // MQTT 매니저 세팅
        mqttManager = MqttManager { event ->
            lifecycleScope.launch(Dispatchers.Main) {
                // **조건을 만족하면 SMS 발송**
                if (event.mode == TARGET_MODE) { // && event.date == TARGET_DATE
                    sendSms(this@MainActivity, event.mode, event.date)
                }

                // DB 저장 및 프래그먼트에 전달
                launch(Dispatchers.IO) {
                    db.eventDao().insert(event)
                }
                val current = supportFragmentManager.findFragmentById(R.id.container)
                if (current is EventFragment) {
                    current.addEventFromMqtt(event)
                }
            }
        }

//        // mqtt 필요없어지면 지워야함
//        mqttManager = MqttManager { event ->
//            lifecycleScope.launch {
//                Log.d("MQTT", "DB저장 : $event")
//                db.eventDao().insert(event)
//                val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
//                if (currentFragment is EventFragment) {
//                    currentFragment.addEventFromMqtt(event)
//                }
//            }
//        }
        mqttManager.connectAndSubscribe()

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
        mqttManager.disconnect()
    }

    private fun sendSms(context: Context, mode: String, date: String) {
        val message =  "사고 발생! 모드: $mode, 날짜: $date"
        Log.d("SENDMSG", "$message")
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
//            try {
//                val smsManager = context.getSystemService(SmsManager::class.java)
//                smsManager.sendTextMessage(
//                    SMS_TARGET_NUMBER,
//                    null,
//                    message,
//                    null,
//                    null
//                )
//                Log.d("SMS", "전송 성공: $message")
//            } catch (e: Exception) {
//                Log.e("SMS", "전송 실패: ${e.message}")
//            }
//        }
//        else {
//            Log.w("SMS", "메시지 권한없음")
//        }
    }

}
