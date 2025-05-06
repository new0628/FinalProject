// DeviceAdapter

package com.example.finalproject.ui.home.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.finalproject.databinding.ItemDeviceBinding

// 블루투스 기기 목록 어댑터
class DeviceAdapter(
    // 항목 클릭시 호출
    private val onClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {

    // 블루투스 기기 목록 저장 리스트
    private val items = mutableListOf<BluetoothDevice>()

    // 선택된 기기
    private var selectedPosition = RecyclerView.NO_POSITION

    // 뷰 홀더
    inner class VH(private val b: ItemDeviceBinding) :
        RecyclerView.ViewHolder(b.root) {


            // 기기 정보 바인딩
            @SuppressLint("MissingPermission")
            fun bind(device: BluetoothDevice) = with(b) {
                tvName.text = device.name ?: "이름 없음"
                tvAddr.text = device.address

                // 하이라이트 상태 반영
                root.isSelected = (adapterPosition == selectedPosition)

                root.setOnClickListener {
                    val prev = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onClick(device)
                }
            }
    }
    // 뷰 홀더 생성
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(
        ItemDeviceBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
    // 뷰 홀더 데이터 바인딩
    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])
    // 아이템 갯수 변환
    override fun getItemCount() = items.size
    // 기기 목록 갱신
    fun updateDevices(newList: List<BluetoothDevice>) {
        items.apply {
            clear()
            addAll(newList)
        }
        notifyDataSetChanged()
    }
}
