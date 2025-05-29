package com.example.finalproject.ui.mypage


import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.ui.events.EventItem
import kotlinx.coroutines.launch
import java.io.File

class BlackboxDialogFragment : DialogFragment() {

    private lateinit var adapter: BlackboxAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = layoutInflater.inflate(R.layout.dialog_blackbox_list, null)
        val rv = v.findViewById<RecyclerView>(R.id.rvBlackbox)

        adapter = BlackboxAdapter { event -> showImageDialog(event) }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // DB 가져오기
        lifecycleScope.launch {
            val list = MainActivity.db.eventDao().getAll()      // id DESC → 가장 최근순
            adapter.submitList(list)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("저장된 블랙박스 이벤트")
            .setView(v)
            .setPositiveButton("닫기", null)
            .create()
    }

    // 사진 확대 다이얼로그
    private fun showImageDialog(event: EventItem) {
        // 1) dialog_event_image.xml 전체를 inflate
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_image, null)
        // 2) 그 안의 ImageView 를 찾아서 Glide 로 로드
        val img = dialogView.findViewById<ImageView>(R.id.imgEvent)

        Glide.with(this)
            .load(File(event.imagePath))
            .error(R.drawable.profile_image)
            .into(img)

        AlertDialog.Builder(requireContext())
            .setTitle("${event.title} • ${event.date}")
            .setView(dialogView)
            .setPositiveButton("닫기", null)
            .show()
    }
}