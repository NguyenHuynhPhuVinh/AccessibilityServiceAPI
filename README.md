# Android Accessibility Service API

🚀 **Android Accessibility Service API** là một ứng dụng Android native được phát triển bằng Kotlin, cung cấp HTTP API server để cho phép AI và các hệ thống bên ngoài tương tác với Android UI thông qua Accessibility Service.

## ✨ Tính năng chính

- 🌐 **HTTP API Server** chạy trên port 8080
- 🌳 **UI Tree Traversal** - Dump và phân tích cấu trúc UI
- 👆 **Touch Interactions** - Click, scroll, input text
- 📊 **Health Monitoring** - Kiểm tra trạng thái service
- 🔒 **Accessibility Service** - Tương tác an toàn với Android system

## 🛠️ Cài đặt và Sử dụng

### 1. Build và Install App

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Bật Accessibility Service

1. Mở app **AccessibilityServiceAPI**
2. Nhấn nút **"Bật Accessibility Service"**
3. Trong Settings → Accessibility, tìm và bật **"AccessibilityServiceAPI"**
4. Quay lại app để kiểm tra trạng thái

### 3. Sử dụng API

Server sẽ chạy tại:

- **Local access:** `http://localhost:8080`
- **Network access:** `http://[PHONE_IP]:8080`

**Lấy IP của điện thoại:**

- Mở app để xem IP address được hiển thị
- Hoặc Settings → WiFi → Advanced → IP address

## 📡 API Endpoints

### 🏥 Health Check

```bash
GET /health
```

**Response:**

```json
{
  "success": true,
  "data": {
    "status": "OK",
    "serverVersion": "1.0.0",
    "accessibilityServiceEnabled": true,
    "uptime": 12345
  },
  "timestamp": 1640995200000
}
```

### 🌳 UI Tree

```bash
GET /ui-tree
```

**Response:**

```json
{
  "success": true,
  "data": {
    "rootNode": {
      "id": "android.widget.FrameLayout_0_0_1080_2340",
      "className": "android.widget.FrameLayout",
      "packageName": "com.example.app",
      "text": null,
      "contentDescription": null,
      "bounds": {
        "left": 0,
        "top": 0,
        "right": 1080,
        "bottom": 2340,
        "width": 1080,
        "height": 2340,
        "centerX": 540,
        "centerY": 1170
      },
      "isClickable": false,
      "isScrollable": false,
      "children": [...]
    },
    "totalNodes": 45,
    "captureTime": 1640995200000
  }
}
```

### 👆 Click Action

```bash
POST /click
Content-Type: application/json

{
  "x": 540,
  "y": 1170,
  "nodeId": "optional_node_id"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "clicked": true,
    "coordinates": [540, 1170],
    "nodeFound": true
  }
}
```

### 📜 Scroll Action

```bash
POST /scroll
Content-Type: application/json

{
  "direction": "DOWN",
  "nodeId": "optional_scrollable_node_id",
  "amount": 1
}
```

**Directions:** `UP`, `DOWN`, `LEFT`, `RIGHT`

### ⌨️ Text Input

```bash
POST /input-text
Content-Type: application/json

{
  "text": "Hello World",
  "nodeId": "optional_input_field_id",
  "clearFirst": true
}
```

## 🔧 Cấu trúc Dự án

```
app/src/main/java/com/tomisakae/accessibilityserviceapi/
├── MainActivity.kt                    # Main activity với UI control
├── models/
│   └── ApiModels.kt                  # Data classes cho API
├── service/
│   ├── AccessibilityApiService.kt    # Core accessibility service
│   └── ApiHttpServer.kt              # HTTP server implementation
└── utils/
    └── UiTreeTraversal.kt            # UI tree parsing utilities
```

## 🧪 Testing

Chạy unit tests:

```bash
./gradlew test
```

Chạy instrumented tests:

```bash
./gradlew connectedAndroidTest
```

## 📱 Yêu cầu Hệ thống

- **Android API Level:** 24+ (Android 7.0)
- **Permissions:**
  - `INTERNET` - Cho HTTP server
  - `ACCESS_NETWORK_STATE` - Kiểm tra kết nối mạng
  - `BIND_ACCESSIBILITY_SERVICE` - Accessibility service

## 🔒 Bảo mật

- Server chỉ chạy trên localhost (127.0.0.1)
- Không có authentication (phù hợp cho development/testing)
- Accessibility service cần được user bật thủ công

## 🚀 Sử dụng với AI

Ví dụ Python script để tương tác với API từ desktop:

```python
import requests
import json

# Thay YOUR_PHONE_IP bằng IP thực của điện thoại
PHONE_IP = "192.168.1.100"  # Ví dụ IP
BASE_URL = f"http://{PHONE_IP}:8080"

# Health check
response = requests.get(f'{BASE_URL}/health')
print("Health:", response.json())

# Get UI tree
ui_tree = requests.get(f'{BASE_URL}/ui-tree')
print("UI Tree nodes:", ui_tree.json()['data']['totalNodes'])

# Click action
click_data = {"x": 540, "y": 1170}
response = requests.post(f'{BASE_URL}/click', json=click_data)
print("Click result:", response.json())

# Scroll down
scroll_data = {"direction": "DOWN"}
response = requests.post(f'{BASE_URL}/scroll', json=scroll_data)
print("Scroll result:", response.json())

# Input text
input_data = {"text": "Hello from Desktop!", "clearFirst": True}
response = requests.post(f'{BASE_URL}/input-text', json=input_data)
print("Input result:", response.json())
```

**Curl examples từ desktop:**

```bash
# Thay 192.168.1.100 bằng IP thực của điện thoại
curl -X GET http://192.168.1.100:8080/health
curl -X GET http://192.168.1.100:8080/ui-tree
curl -X POST http://192.168.1.100:8080/click \
  -H "Content-Type: application/json" \
  -d '{"x": 540, "y": 1170}'
```

## 🤝 Đóng góp

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push và tạo Pull Request

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết.

---

**Lưu ý:** Đây là tool dành cho development và testing. Sử dụng có trách nhiệm và tuân thủ các quy định về accessibility và privacy.
