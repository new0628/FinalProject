# 🚗 AI BLACKBOX ASSISTANCE

**온디바이스 AI 기반의 스마트 차량 안전 블랙박스 시스템**

---

## 📌 프로젝트 개요

운전자 음성과 주변 환경을 분석해 **차선 이탈, 차량 경고, 이상행동 감지, 경적 및 사이렌 소리 감지**까지 수행하는  
**스마트 안전 운전 보조 시스템**입니다.  

Rubik Pi 보드에서 실시간 추론이 가능한 **온디바이스 AI**를 활용하여  
✅ 차량 충돌 및 사고 전후 상황 인지  
✅ 즉시 운전자 및 탑승자에게 **실시간 경고 및 알림 제공**  
✅ 자동 영상 저장 및 모바일 앱 연동으로 상황 관리  

이를 통해 **교통사고 예방**, **위험 대처 시간 단축**, **취약 운전자 배려**, **개인정보 보호 강화**를 실현합니다.

---

## 🎯 목표

1. **교통 사고 예방**
   - 사고 직전 운전자를 빠르게 인지 및 경고
   - "앞차 출발", "차선 이탈", "사이렌 감지" 등 실시간 경고 제공

2. **실시간 대응성과 개인정보 보호 동시 확보**
   - AI 추론 및 저장이 모두 디바이스 내부에서 처리
   - 인터넷 연결 없이도 작동 가능한 완전한 온디바이스 구조

3. **취약 운전자 배려**
   - 웨어러블 기기를 통한 **직관적 경고 제공** (예: 음성 토스트, 진동 알림)
   - 어르신, 청각장애인, 초보 운전자도 쉽게 사용 가능

---

## 📷 주요 기능

### 🚘 BlackBox (ADAS)
- 차선 이탈 감지
- 전방 차량 출발 감지
- 전복/충돌 감지
- 보행자 접근 감지
- 긴급 음성 이벤트 감지 및 알림

### ⌚ Galaxy Watch
- 차량 경고 음성 및 진동 알림
- 사이렌/경적/비명 실시간 감지 및 진동 알림

### 📱 Android App
- 블랙박스 연동 상태 확인
- 블랙박스 이벤트 기록 확인
- 신고 기능 및 사용자 맞춤 기록 관리

---

## 🧠 시스템 구조

### 📦 Hardware
- Rubik Pi 3
- IMX477 카메라 모듈
- USB 마이크 및 스피커
- Galaxy Watch 연동 (BLE)

### 🧩 소프트웨어 구성
- Object Detection: YOLOv8 + OpenCV
- Sound Detection: Edge Impulse (경적/사이렌/비명/울음)
- AI 이벤트 판단 모듈 (충돌, 이상행동, 위험 감지)

### 🧰 시스템 아키텍처 다이어그램

![System Architecture](./assets/system_architecture.png)  
> 위 그림은 실제 시스템의 하드웨어-소프트웨어 구성 및 데이터 흐름을 나타냅니다.

### 📲 통합 연동
- Android Studio 기반 앱 개발
- `MyPage`, `HOME`, `Events` 탭 구성
- 실시간 경고 확인, 신고 기록 및 이벤트 공유

---

## ✨ On-device AI Fit

| 항목                     | 설명 |
|--------------------------|------|
| 🔥 실시간 반응성           | 사고, 경적, 비명 등 긴급 상황 발생 시 **실시간으로 즉각적인 판단과 음성 안내 가능**클라우드 대비 수 밀리초(ms) 단위의 빠른 반응 속도 확보 |
| 🌐 네트워크 독립성         | 지하 주차장, 터널, 시골 도로 등 통신 불가능한 환경에서도 **완전한 작동**<br>인터넷 연결이 없어도 항상 정상 작동 가능한 구조 |
| 🔒 개인정보 보호 및 보안   | 음성 및 영상 데이터를 클라우드로 전송하지 않고 **로컬에서 처리**<br>민감 정보의 유출 가능성 제거 및 보안성 극대화 |
| 🎛️ 멀티모달 처리 호환성    | 마이크, 카메라, 센서 등 다양한 입력을 **기기 내에서 통합 분석**<br>여러 센서 데이터를 조합해 상황을 더 정확히 인식하고 판단 |

---

## 👨‍👩‍👧‍👦 팀원 소개

- **오재우**: 모바일 소프트웨어, 웹공학 트랙  
- **안희영**: 모바일 소프트웨어, 웹공학 트랙  
- **이상원**: 모바일 소프트웨어, 빅데이터 트랙  
- **서서은**: 모바일 소프트웨어, 디지털콘텐츠 트랙  
- **지도교수님**: 한기준 교수님  

---

## 🧪 사용 기술 스택

- Qualcomm Rubik Pi 3
- Edge Impulse
- YOLOv8, OpenCV
- Android Studio (Java/Kotlin)
- GitHub, Colab, AI Hub
- Galaxy Watch 연동 (BLE)

---

## 🔗 관련 링크

### 📘 Qualcomm 및 Rubik Pi 공식 문서

- 📄 [Qualcomm Linux 1.1.1 공식 문서](https://docs.qualcomm.com/bundle/Qualcomm-Linux-User-Guide-1.1.1/page/)
- 🧰 [Rubik Pi 3 공식 문서 (Thundercomm)](https://www.thundercomm.com/product/rubik-pi/)
- 💾 [Rubik Pi 관련 GitHub 예제](https://github.com/ThunderSoft-OSS)

### 🤖 AI 개발 도구

- 🌐 [Qualcomm AI Hub 공식 페이지](https://aihub.qualcomm.com/)
- 📦 [Qualcomm AI Hub GitHub](https://github.com/quic/ai-hub)
- 🛠 [Qualcomm AI SDK 공식 문서](https://developer.qualcomm.com/software/ai-stack)
- 🧪 [Qualcomm AI SDK GitHub](https://github.com/quic/qcom-ai)

### 🧠 Edge Impulse

- 📚 [Edge Impulse 공식 문서](https://docs.edgeimpulse.com/)
- 🛠 [Edge Impulse Studio (모델 학습 플랫폼)](https://studio.edgeimpulse.com/)
- 💡 [Edge Impulse Linux SDK](https://docs.edgeimpulse.com/docs/development-platforms/linux/linux-cli)

![image.png](attachment:1261409e-14c1-4d98-9dd5-97b2fb3e41e8:f62927d8-13d3-4d7a-8fd5-3a4f52f9ea50.png)
