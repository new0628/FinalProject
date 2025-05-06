package com.example.finalproject.ui.home

enum class ConnState {
    NONE,        // 초기 상태
    BONDING,     // 페어링 중
    BONDED,      // 페어링 완료
    CONNECTING,  // GATT 연결 중
    CONNECTED,   // GATT 연결 완료
    FAILED       // 실패 또는 끊김
}