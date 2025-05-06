package com.example.finalproject.ui.mypage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

import com.example.finalproject.CustomVibrationPreference
import com.example.finalproject.R

// 설정 다이얼로그
class CustomVibrationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_custom_vibration, null)

        //진동 세기 조절 바
        val amplitudeSeekBar = view.findViewById<SeekBar>(R.id.amplitudeSeekBar)
        //진동 세기 텍스트
        val amplitudeValueText = view.findViewById<TextView>(R.id.amplitudeValueText)
        // 진동 지속 시간 조절 바
        val durationSeekBar = view.findViewById<SeekBar>(R.id.durationSeekBar)
        // 진동 지속 시간 텍스트
        val durationValueText = view.findViewById<TextView>(R.id.durationValueText)
        // 저장 버튼
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        //저장된 값 불러오기
        val (savedAmplitude, savedDuration) = CustomVibrationPreference.load(requireContext())
        amplitudeSeekBar.progress = savedAmplitude
        durationSeekBar.progress = (savedDuration/500).toInt()

        amplitudeValueText.text = getString(R.string.amplitude_value, savedAmplitude)
        durationValueText.text = getString(R.string.initial_duration, (savedDuration / 1000.0))
        // 진동 세기 변경 리스너
        amplitudeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                amplitudeValueText.text = getString(R.string.current_amplitude, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // 진동 지속 시간 변경 리스너
        durationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val duration = progress * 500
                val durationInSeconds = duration / 1000.0
                durationValueText.text = getString(R.string.duration_value, durationInSeconds)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // 저장 버튼 클릭 처리
        saveButton.setOnClickListener {
            val amplitude = amplitudeSeekBar.progress
            val duration = durationSeekBar.progress * 500L

            CustomVibrationPreference.save(requireContext(), amplitude, duration)
            Log.d("MyApp", "amplitude : $amplitude, duration : $duration")

            amplitudeValueText.text = getString(R.string.current_amplitude, amplitude)
            durationValueText.text = getString(R.string.duration_in_seconds, duration / 1000.0)
            dismiss()
        }
        // 반환
        return AlertDialog.Builder(requireContext())
            .setTitle("진동 커스터마이징")
            .setView(view)
            .create()
    }
}

