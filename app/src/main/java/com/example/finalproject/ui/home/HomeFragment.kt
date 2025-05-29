//HomeFragment.kt 

package com.example.finalproject.ui.home
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.finalproject.R
import com.example.finalproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    // 뷰 바인딩
    private var _binding: FragmentHomeBinding? = null
    // _binding 가져오기 (안전하게 하려고)
    private val binding get() = _binding!!
    // HomeViewModel 참조
    private val vm: HomeViewModel by activityViewModels()
    //private val targetMacAddress = "9C:B8:B4:9F:5A:19" // <-루빅파이 MAC 주소로 바꾸세요

    // 뷰 생성 후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 바인딩 연결
        _binding = FragmentHomeBinding.bind(view)

        // 블랙박스 버튼 클릭 시 블루투스 설정 들어가기
        binding.btnRegister.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
        binding.btnSocketRegister.setOnClickListener {
            Log.d("btnRegister", "소켓 버튼 눌림")
            val mac = vm.rubikpiMac.value
            if (mac != null) {
                val intent = Intent(requireContext(), BluetoothService::class.java).apply {
                    putExtra("rubikpi_mac", mac)
                }
                ContextCompat.startForegroundService(requireContext(), intent)
            } else {
                Toast.makeText(requireContext(), "루빅파이 MAC 주소가 없습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 끝날 때 호출
    override fun onDestroyView() {

        _binding = null
        super.onDestroyView()
    }

    @SuppressLint("MissingPermission")
    private fun logBondedDevices() {
        val bluetoothManager = requireContext().getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager?.adapter
        if (bluetoothAdapter == null || !PermissionUtils.hasBluetoothConnectPermission(requireContext())) {
            Log.w("HomeFragment", "Bluetooth 사용 불가 또는 권한 없음")
            return
        }

        val bondedDevices = bluetoothAdapter.bondedDevices
        if (bondedDevices.isEmpty()) {
            Log.d("HomeFragment", "등록된 블루투스 기기 없음")
        } else {
            bondedDevices.forEach { device ->
                Log.d("HomeFragment", "등록된 기기 이름: ${device.name}, 주소: ${device.address}")
                if (device.name == "rubikpi") {
                    Log.d("HomeFragment", "✅ RubikPi 기기 발견 → MAC 저장: ${device.address}")
                    vm.setRubikpiMac(device.address)
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()

        logBondedDevices()
    }
}