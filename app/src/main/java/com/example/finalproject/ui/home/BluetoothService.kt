package com.example.finalproject.ui.home

import android.app.*
import android.bluetooth.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.finalproject.CustomVibrationPreference
import com.example.finalproject.WatchMessenger
import com.example.finalproject.ui.events.EventDatabase
import com.example.finalproject.ui.events.EventItem
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.*
import android.Manifest
import android.content.Context

import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


data class Event(
    val mode: String,
    val title: String,
    val date: String,
    val imageName: String,
    val imageBytes: ByteArray
)

class BluetoothService : Service() {

    private var rubikpiMac: String? = null
    private var socket: BluetoothSocket? = null
    @Volatile private var listening = false

    private val eventChannel = Channel<Event>(Channel.UNLIMITED)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var watchMessenger: WatchMessenger

    companion object {
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "bt_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createBtNotificationChannel()
        createAlertNotificationChannel()
        initWatchMessenger()
        launchEventSaver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!listening) {
            rubikpiMac = intent?.getStringExtra("rubikpi_mac")
            Log.d("BtService", "목표 MAC: $rubikpiMac")
            listening = true
            startForeground(NOTIF_ID, buildNotification())
            scope.launch { producerLoop() }
        }
        return START_STICKY
    }

    private suspend fun producerLoop() {
        while (listening) {
            try {
                ensureConnected()
                val input = socket!!.inputStream
                while (listening) {
                    processIncomingData(input)
                }
            } catch (e: IOException) {
                Log.e("BtService", "소켓 오류: ${e.message}")
                delay(5000)
                socket?.close()
                socket = null
            }
        }
    }

    private suspend fun processIncomingData(input: InputStream) {
        val line = input.readLineOrThrow().trim()
        if (line.isBlank()) return

        val parts = line.split("|")
        if (parts.size != 4) return

        val (mode, title, date, imageName) = parts
        Log.d("Events", "mode: $mode, title: $title, date: $date, image: $imageName")
        vibrateWatchIfNeeded(mode, title)

        val imageBytes = if (imageName.isNotBlank()) {
            val header = input.readLineOrThrow()
            val size = header.split("|")[1].toIntOrNull() ?: 0
            val buf = ByteArray(size)
            var read = 0
            while (read < size) {
                val r = input.read(buf, read, size - read)
                if (r <= 0) throw IOException("EOF image")
                read += r
            }
            input.readLineOrThrow() // 종료 태그
            buf
        } else ByteArray(0)

        eventChannel.send(Event(mode, title, date, imageName, imageBytes))

    }

    private suspend fun saveEvent(event: Event) {
        if (event.imageName.isBlank()) return
        try {
            val file = File(filesDir, event.imageName)
            file.writeBytes(event.imageBytes)
            val path = file.absolutePath
            Log.d("BtService", "이미지 저장: $path")

            val dbEvent = EventItem(
                mode = event.mode,
                title = event.title,
                date = event.date,
                color = getColorForTitle(event.title),
                imagePath = path
            )
            EventDatabase.getInstance(applicationContext)
                .eventDao().insert(dbEvent)
        } catch (e: Exception) {
            Log.e("BtService", "저장 오류: ${e.message}")
        }
    }

    private fun vibrateWatchIfNeeded(mode: String, title: String) {
        val (amplitude, duration) = CustomVibrationPreference.loadForMode(this, mode)

        val watchMes = when {
            mode == "siren" && title == "siren"     -> "사이렌"
            mode == "siren" && title == "horn"      -> "경적"
            mode == "light" && title == "light"     -> "초록불입니다. 출발하세요."
            mode == "siren" && title == "crash"  -> "차량 충돌이 발생했습니다."
            mode == "siren" && title == "car_accident"  -> "차량 충돌이 발생했습니다."
            mode == "driving" && title == "lane_closed" -> "차선 이탈을 감지했습니다."
            else -> return // 조건에 맞지 않으면 진동 안 보냄
        }
        // 문자 전송은 특정 조건에서만
        if (mode == "siren" && (title == "crash" || title == "car_accident")) {
            sendSms(this, mode, getCurrentDate(), watchMes)
            sendAlertNotification("사고 감지", watchMes)
        }

        watchMessenger.sendVibration(watchMes, amplitude, duration)
    }

    @Throws(IOException::class)
    private fun ensureConnected() {
        if (socket?.isConnected == true) return

        socket?.close()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) throw IOException("권한 필요")

        val mac = rubikpiMac ?: throw IOException("MAC 누락")
        val adapter = (getSystemService(BluetoothManager::class.java)
            ?: throw IOException("BT 매니저 없음")).adapter
        val dev = adapter.getRemoteDevice(mac)
        val method = dev.javaClass.getMethod("createRfcommSocket", Int::class.java)
        socket = (method.invoke(dev, 22) as BluetoothSocket).apply {
            connect()
            Handler(mainLooper).post {
                Toast.makeText(applicationContext, "소켓 연결 완료", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("socket", "소켓연결완료")
    }

    private fun buildNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("RubikPi 연결 중")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()

//    private fun createNotificationChannel() {
//        val nm = getSystemService(NotificationManager::class.java)
//        val channel = NotificationChannel(
//            CHANNEL_ID,
//            "BT 서비스",
//            NotificationManager.IMPORTANCE_LOW
//        )
//        nm.createNotificationChannel(channel)
//    }

    // BT 서비스용 채널
    private fun createBtNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        val btChannel = NotificationChannel(
            CHANNEL_ID,
            "BT 서비스",
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(btChannel)
    }

    // 긴급 알림용 채널
    private fun createAlertNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                "alert_channel_id",
                "긴급 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "사고 등의 중요한 이벤트 알림"
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(alertChannel)
        }
    }
    private fun initWatchMessenger() {
        watchMessenger = WatchMessenger(this)
        scope.launch {
            try {
                watchMessenger.init()
            } catch (e: Exception) {
                Log.e("BtService", "워치 초기화 실패: ${e.message}")
            }
        }
    }

    private fun launchEventSaver() {
        scope.launch {
            for (event in eventChannel) {
                saveEvent(event)
            }
        }
    }

    override fun onDestroy() {
        listening = false
        socket?.close()
        scope.cancel()
        eventChannel.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun InputStream.readLineOrThrow(): String {
        val sb = StringBuilder()
        while (true) {
            val b = read().takeIf { it >= 0 } ?: throw IOException("EOF")
            if (b.toChar() == '\n') break
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    private fun getColorForTitle(title: String) = when (title) {
        "car accident" -> "#00FF00"
        "door open" -> "#FF0000"
        "door close" -> "#FFA500"
        else -> "#FFFFFF"
    }

    // 메시지 보내기
    private fun sendSms(context: Context, mode: String, date: String, title: String) {

        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val targetNumber = prefs.getString("emergency_number", null)

        Log.d("SendMsg", "$targetNumber")

        if (targetNumber.isNullOrBlank()) {
            Log.w("SENDMSG", "저장된 연락처가 없습니다.")
            Toast.makeText(this,"저장된 연락처가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val message =  "사고 발생! 날짜: $date, $title"
        Log.d("SENDMSG", "$mode, $message")

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val smsManager = context.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(
                    targetNumber, // 수신자 번호
                    null,         // 발신자 (null이면 기본 발신자)
                    message,      // 메시지 내용
                    null, null    // 전송 완료 및 수신 확인 인텐트 (필요시 사용)
                )
                Log.d("SMS", "문자 전송 성공")
            } catch (e: Exception) {
                Log.e("SMS", "문자 전송 실패: ${e.message}")
            }
        } else {
            Log.w("SMS", "SEND_SMS 권한 없음")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "SMS 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        val now = System.currentTimeMillis()
        return android.text.format.DateFormat.format("yyyy-MM-dd", now).toString()
    }

    private fun sendAlertNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("Notification", "알림 권한 없음. 알림을 전송하지 않음.")
                return
            }
        }


        val notification = NotificationCompat.Builder(this, "alert_channel_id")
            .setSmallIcon(android.R.drawable.stat_notify_error) // 필수! 여기에 png 직접 사용 불가
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(999, notification)
    }

//    private fun sendSms(context: Context, mode: String, date: String) {
//        val message =  "사고 발생! 모드: $mode, 날짜: $date"
//        Log.d("SENDMSG", "$message")
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
//    }
}