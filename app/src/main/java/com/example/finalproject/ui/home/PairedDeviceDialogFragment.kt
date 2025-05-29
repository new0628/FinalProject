//PairedDeviceDialogFragment

package com.example.finalproject.ui.home

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import com.example.finalproject.databinding.BluetoothPopupDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PairedDeviceDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BluetoothPopupDialogBinding? = null
    private val binding get() = _binding!!
    //private lateinit var adapter: DeviceAdapter
    private var selectedDevice: BluetoothDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BluetoothPopupDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        adapter = DeviceAdapter { device ->
//            selectedDevice = device
//            binding.btnConnect.isEnabled = true
//            Log.d("DialogFragment", "선택한 기기 이름: ${device.name}")
//        }

//        binding.recyclerDevices.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerDevices.adapter = adapter

        // 🔄 기존 Permission check → 유틸 함수로 변경
        if (PermissionUtils.checkAndWarnIfNoBluetoothPermission(requireContext())) {
            val bluetoothManager = requireContext().getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager?.adapter
            val bondedDevices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()

            if (bondedDevices.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                //adapter.updateDevices(bondedDevices)
                binding.tvEmpty.visibility = View.GONE
            }
        } else {
            dismiss()
            return
        }

        binding.btnConnect.setOnClickListener {
            selectedDevice?.let { device ->
                //권한 검사 리팩토링
                val hasPermission = PermissionUtils.checkAndWarnIfNoPermissionForAction(
                    context = requireContext(),
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    message = "BLUETOOTH_CONNECT 권한 필요"
                )
                if (!hasPermission) return@setOnClickListener

                val bondState = device.bondState
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(requireContext(), "${device.name ?: "기기"} 이미 페어링됨", Toast.LENGTH_SHORT).show()
                    dismiss()
                    return@setOnClickListener
                }

                if (bondState == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(requireContext(), "${device.name ?: "기기"} 이미 페어링됨", Toast.LENGTH_SHORT).show()
                    dismiss()
                    return@setOnClickListener
                }

                // BroadcastReceiver 정의
                val bondReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val action = intent?.action
                        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                            val bondedDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                            if (bondedDevice?.address == device.address) {
                                when (state) {
                                    BluetoothDevice.BOND_BONDING -> {
                                        Log.d("Bonding", "⏳ 페어링 중...")
                                    }
                                    BluetoothDevice.BOND_BONDED -> {
                                        Log.d("Bonding", "✅ 페어링 완료")
                                        try {
                                            Toast.makeText(requireContext(), "페어링 완료: ${device.name}", Toast.LENGTH_SHORT).show()
                                        } catch (e: SecurityException) {
                                            Toast.makeText(requireContext(), "페어링 완료", Toast.LENGTH_SHORT).show()
                                        }
                                        requireContext().unregisterReceiver(this)
                                        dismiss()
                                    }
                                    BluetoothDevice.BOND_NONE -> {
                                        Log.d("Bonding", "❌ 페어링 실패")
                                        Toast.makeText(requireContext(), "페어링 실패", Toast.LENGTH_SHORT).show()
                                        requireContext().unregisterReceiver(this)
                                    }
                                }
                            }
                        }
                    }
                }

                // 리시버 등록
                val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                requireContext().registerReceiver(bondReceiver, filter)

                // 👉 페어링 요청 시작
                val success = device.createBond()
                if (success) {
                    Toast.makeText(requireContext(), "${device.name ?: "기기"} 페어링 요청 중...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "페어링 요청 실패", Toast.LENGTH_SHORT).show()
                    try {
                        requireContext().unregisterReceiver(bondReceiver)
                    } catch (_: Exception) {}
                }

            } ?: Toast.makeText(requireContext(), "기기를 선택해주세요.", Toast.LENGTH_SHORT).show()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}