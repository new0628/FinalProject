// BluetoothDialogFragment.kt
package com.example.finalproject.ui.home

import android.bluetooth.BluetoothDevice

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast

import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.finalproject.databinding.BluetoothPopupDialogBinding

import com.example.finalproject.ui.home.adapter.DeviceAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import android.os.Handler
import android.os.Looper

class BluetoothDialogFragment : BottomSheetDialogFragment() {
    // viewBinding 변수
    private var _b: BluetoothPopupDialogBinding? = null
    // _b 접근용
    private val b get() = _b!!
    // HomeViewModel 참조
    private val vm: HomeViewModel by activityViewModels()
    // 리사이클러뷰 어댑터
    private lateinit var adapter: DeviceAdapter
    // 선택된 BLE 기기
    private var selectedDevice: BluetoothDevice? = null

    // 바인딩 초기화
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = BluetoothPopupDialogBinding.inflate(inflater, container, false)
        .also { _b = it }.root

    // 시작 시 상태 설정
    override fun onStart() {
        super.onStart()
        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let { sheet ->
                BottomSheetBehavior.from(sheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    // 필요시 peekHeight 조정 (초기 높이임)
                    peekHeight = Resources.getSystem().displayMetrics.heightPixels / 2
                }
            }
    }
    // UI 초기화 및 뷰모델 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // RecyclerView 세팅
        adapter = DeviceAdapter { dev ->
            selectedDevice = dev
            b.btnConnect.isEnabled = true
        }
        b.recyclerDevices.adapter = adapter
        b.recyclerDevices.layoutManager = LinearLayoutManager(requireContext())

        vm.devices.observe(viewLifecycleOwner) { list ->
            adapter.updateDevices(list)
            b.tvEmpty.isGone = list.isNotEmpty()
            b.progress.isGone = list.isNotEmpty()

        }

        // 스캔 결과 관찰
        vm.connState.observe(viewLifecycleOwner) { st ->
            when (st) {
                ConnState.BONDING -> b.progress.isGone = false
                ConnState.CONNECTING -> b.progress.isGone = false
                ConnState.CONNECTED -> {
                    Toast.makeText(requireContext(), "연결 완료!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                ConnState.FAILED -> {
                    b.progress.isGone = true
                    Toast.makeText(requireContext(), "연결 실패", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }

        // 연결 버튼 클릭시
        b.btnConnect.setOnClickListener {
            selectedDevice?.let { dev ->
                if (PermissionUtils.hasBluetoothConnectPermission(requireContext())) {
                    b.progress.isGone = false
                    vm.connect(dev)
                }
            }
        }
        // 취소 버튼 클릭시
        b.btnCancel.setOnClickListener {
            vm.stopScan()
            dismiss()
        }

        // 다이얼로그 띄우자마자 스캔 시작
        vm.startScan(
            onSuccess = {

            },
            onFail = {
                Toast.makeText(requireContext(), "BLE 스캔 시작 실패", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        )
        // 10초 후 자동 스캔 중지 + 결과
        Handler(Looper.getMainLooper()).postDelayed({
            vm.stopScan()
            if (adapter.itemCount == 0) {
                b.tvEmpty.isGone = false
            }
        }, 10000)
    }
    // 뷰 소멸 시 스캔 중지 및 바인딩 해제
    override fun onDestroyView() {
        vm.stopScan()
        super.onDestroyView()
        _b = null
    }

}
