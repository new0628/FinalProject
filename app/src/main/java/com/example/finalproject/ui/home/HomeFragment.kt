//HomeFragment.kt 

package com.example.finalproject.ui.home

import android.Manifest

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log

import android.view.View

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    // 권한 요청
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    // 뷰 생성 후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 마지막 연결 디바이스 자동 연결 시도
        vm.reconnectLastDeviceIfPossible()
        // 바인딩 연결
        _binding = FragmentHomeBinding.bind(view)
        registerPermissionLauncher()
        // 블랙박스 버튼 클릭 시 BLE 연결 시작
        binding.btnRegister.setOnClickListener {
            Log.d("btnRegister", "블랙박스 버튼 눌림")
            checkPermissions()
        }
    }
    // 끝날 때 호출
    override fun onDestroyView() {
        vm.stopScan()
        _binding = null
        super.onDestroyView()
    }
    // 권한체크 함수
    private fun checkPermissions() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        else
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        // 거부된 권한 필터링
        val deniedList = perms.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        // 모든 권한 허용된 경우 BLE 스캔 시작
        if (deniedList.isEmpty()) {
            vm.startScan(
                onSuccess = {
                    BluetoothDialogFragment().show(parentFragmentManager, "bt")
                },
                onFail = {
                    Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
                }
            )

        }else {
            // 아직 허용되지 않은 권한이 있다면 요청
            // "다시 묻지 않음"이 포함되어 있는지 확인
            val someDeniedPermanently = deniedList.any {
                !shouldShowRequestPermissionRationale(it)
            }

            if (someDeniedPermanently) {
                // 사용자가 '다시 묻지 않음'을 선택한 경우 -> 설정으로 안내
                Toast.makeText(
                    requireContext(),
                    "권한이 영구적으로 거부되었습니다. 설정에서 권한을 허용해주세요.",
                    Toast.LENGTH_LONG
                ).show()

                // 설정 화면으로 이동
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)

            } else {
                // 아직은 한 번도 거부하지 않았거나, 거부했지만 '다시 묻지 않음'은 아님 -> 재요청 가능
                permissionLauncher.launch(perms)
            }
        }
    }
    // 권한 등록 함수
    private fun registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grant ->
            //val allGranted = grant.values.all { it }
            if (grant.values.all { it }) {
                vm.startScan(
                    onSuccess = {
                        BluetoothDialogFragment().show(parentFragmentManager, "bt")
                    },
                    onFail = {
                        Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
