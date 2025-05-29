
package com.example.finalproject.ui.mypage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle

import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

import com.example.finalproject.CustomVibrationPreference
import com.example.finalproject.R

// 설정 다이얼로그
class CustomVibrationDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_custom_vibration, null)

        val contextGroup     = view.findViewById<RadioGroup>(R.id.contextGroup)
        val amplitudeSeekBar = view.findViewById<SeekBar>(R.id.amplitudeSeekBar)
        val amplitudeText    = view.findViewById<TextView>(R.id.amplitudeValueText)
        val durationSeekBar  = view.findViewById<SeekBar>(R.id.durationSeekBar)
        val durationText     = view.findViewById<TextView>(R.id.durationValueText)
        val saveButton       = view.findViewById<Button>(R.id.saveButton)

        // 1) 라디오 기본 선택 (예: 사이렌)
        contextGroup.check(R.id.radioSiren)

        // 2) 선택된 타입에 맞춰 슬라이더 초기값 로드
        fun refreshSliders() {
            val type = when (contextGroup.checkedRadioButtonId) {
                R.id.radioSiren     -> "siren"
                R.id.radioLight     -> "light"
                R.id.radioBlackbox  -> "blackbox"
                else                -> "siren"
            }
            val (amp, dur) = CustomVibrationPreference.load(requireContext(), type)
            amplitudeSeekBar.progress = amp
            durationSeekBar.progress  = (dur / 1000).toInt()
            amplitudeText.text = "현재 세기: $amp"
            durationText.text  = "지속: ${dur / 1000.0}초"
        }

        refreshSliders()

        // 3) 타입 변경 시 슬라이더 값 즉시 업데이트
        contextGroup.setOnCheckedChangeListener { _, _ ->
            refreshSliders()
        }

        // 4) SeekBar 리스너
        amplitudeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, prog: Int, fromUser: Boolean) {
                amplitudeText.text = "현재 세기: $prog"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        durationSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, prog: Int, fromUser: Boolean) {
                val secs = prog.toDouble()
                durationText.text = "지속: ${secs}초"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 5) 저장 버튼
        saveButton.setOnClickListener {
            val type = when (contextGroup.checkedRadioButtonId) {
                R.id.radioSiren    -> "siren"
                R.id.radioLight    -> "light"
                R.id.radioBlackbox -> "blackbox"
                else               -> "siren"
            }
            val amp = amplitudeSeekBar.progress
            val dur = durationSeekBar.progress * 1000L
            CustomVibrationPreference.save(requireContext(), type, amp, dur)
            Toast.makeText(requireContext(), "$type 설정 저장됨", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("진동 커스터마이징")
            .setView(view)
            .create()
    }
}