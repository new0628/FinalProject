package com.example.finalproject.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.finalproject.CustomVibrationPreference
import com.example.finalproject.R
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private var watchNodeId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 체인지 버튼 클릭
        view.findViewById<Button>(R.id.btn_change_profile).setOnClickListener{
            Log.d("ChangeBtn", "체인지 버튼 클릭됨")
            val (amplitude, duration) = CustomVibrationPreference.load(requireContext())
            val messageText = "체인지 알림이 도착했습니다"

            sendVibrationToWatch(messageText, amplitude, duration)

        }
        // 커스터마이징 카드 클릭 이벤트
        view.findViewById<CardView>(R.id.card_customize).setOnClickListener {
            Log.d("MyPageFragment", "커스터마이징 카드 클릭됨")
            CustomVibrationDialogFragment().show(parentFragmentManager, "CustomVibration")
        }

        // 블랙박스 카드 클릭 이벤트
        view.findViewById<CardView>(R.id.card_blackbox).setOnClickListener {

            Log.d("MyPageFragment", "블랙박스 카드 클릭됨")
            val (amplitude, duration) = CustomVibrationPreference.load(requireContext())
            val messageText = "블랙박스 알림이 도착했습니다"

            sendVibrationToWatch(messageText, amplitude, duration)

        }

        // 신고내역 카드 클릭 이벤트
        view.findViewById<CardView>(R.id.card_report).setOnClickListener {
            Log.d("MyPageFragment", "신고내역 카드 클릭됨")
            val (amplitude, duration) = CustomVibrationPreference.load(requireContext())
            val messageText = "신고내역 알림이 도착했습니다"

            sendVibrationToWatch(messageText, amplitude, duration)

        }

        // 워치 id 조회
        Wearable.getNodeClient(requireActivity())
            .connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) Log.d("MobileApp","Found watch node ${nodes[0].id}")
                watchNodeId = nodes.firstOrNull()?.id
            }
    }

    // 워치로 보내는 함수
    private fun sendVibrationToWatch(message: String, amplitude: Int, duration: Long) {
        Log.d("g", "워치로 보내짐")
        val payloadJson = JSONObject().apply {
            put("amplitude", amplitude)
            put("duration", duration)
            put("message", message)
        }
        val payload = payloadJson.toString().toByteArray()

        watchNodeId?.let { nodeId ->
            Wearable.getMessageClient(requireActivity())
                .sendMessage(nodeId, "/vibrate", payload)
                .addOnSuccessListener { Log.d("성공", "알림 전송 성공") }
                .addOnFailureListener { Log.e("실패", "알림 전송 실패") }
        }
    }

}