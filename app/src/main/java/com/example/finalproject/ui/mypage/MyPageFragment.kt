package com.example.finalproject.ui.mypage

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.fragment.app.Fragment

import com.example.finalproject.R


class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 위험시 보낼 문자 저장된 번호 불러오기
        val savedNumber = requireContext()
            .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("emergency_number", null)
        if (savedNumber != null) {
            view.findViewById<TextView>(R.id.to_mes_num).text = "위험 시 보낼 번호: $savedNumber"
        }

        // 체인지 버튼 클릭
        view.findViewById<Button>(R.id.btn_change_profile).setOnClickListener{
            Log.d("ChangeBtn", "체인지 버튼 클릭됨")
//            val (amplitude, duration) = CustomVibrationPreference.load(requireContext())
//            val messageText = "체인지 알림이 도착했습니다"
            showPhoneNumberDialog()
            //sendVibrationToWatch(messageText, amplitude, duration)

        }
        // 커스터마이징 카드 클릭 이벤트
        view.findViewById<CardView>(R.id.card_customize).setOnClickListener {
            Log.d("MyPageFragment", "커스터마이징 카드 클릭됨")
            CustomVibrationDialogFragment().show(parentFragmentManager, "CustomVibration")
        }

        // 블랙박스 카드 클릭 이벤트
        view.findViewById<CardView>(R.id.card_blackbox).setOnClickListener {

            Log.d("MyPageFragment", "블랙박스 카드 클릭됨")
            BlackboxDialogFragment().show(parentFragmentManager, "BlackboxDialog")

        }

        // 신고내역 카드 클릭 이벤트
        view.findViewById<CardView>(R.id.card_report).setOnClickListener {
            Log.d("MyPageFragment", "신고내역 카드 클릭됨")
//            val (amplitude, duration) = CustomVibrationPreference.load(requireContext())
//            val messageText = "신고내역 알림이 도착했습니다"

        }

    }

    // 위험시 연락할 번호 등록
    private fun showPhoneNumberDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "(예: 01012345678)"
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(11))

        }

        AlertDialog.Builder(requireContext())
            .setTitle("위험시 연락할 번호를 입력해주세요")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val input = editText.text.toString()
                Log.d("MyPage", "번호변경 : $input")
                if (input.matches(Regex("^010\\d{8}$"))) {

                    val numSharedPref = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    numSharedPref.edit {
                        putString("emergency_number", input)
                    }
                    // 예: TextView에 표시하거나 저장
                    view?.findViewById<TextView>(R.id.to_mes_num)?.text = "위험 시 보낼 번호: $input"
                    // 필요하면 저장 로직 추가
                } else {
                    Toast.makeText(requireContext(), "올바른 11자리 번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}