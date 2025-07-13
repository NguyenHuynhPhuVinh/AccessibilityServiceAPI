# 🚀 QUICK START - Screen Reading & Element Interaction API

## Chạy nhanh

### 1. Chuẩn bị

```bash
# Kết nối thiết bị Android hoặc mở emulator
adb devices

# Cài đặt dependencies
flutter pub get
```

### 2. Chạy ứng dụng

```bash
# Chạy ứng dụng Flutter
flutter run

# Hoặc sử dụng script tự động
./run_test.sh
```

### 3. Cấp quyền

1. Mở ứng dụng trên thiết bị Android
2. Nhấn **"Request Accessibility Permission"**
3. Vào **Settings > Accessibility**
4. Tìm và bật **"accessibility_service_api"**

### 4. Test API

```bash
# Test với curl
./test_curl.sh

# Hoặc test với Dart
dart test_api.dart
```

## 🔥 API Endpoints Chính

### ✅ Kiểm tra trạng thái

```bash
curl http://localhost:8080/api/status
```

### 📱 Lấy danh sách ứng dụng

```bash
curl http://localhost:8080/api/apps/user
```

### 🚀 Mở ứng dụng

```bash
curl -X POST http://localhost:8080/api/apps/launch \
  -H "Content-Type: application/json" \
  -d '{"packageName": "com.android.chrome"}'
```

### 👆 Chạm để mở ứng dụng

```bash
curl -X POST http://localhost:8080/api/tap \
  -H "Content-Type: application/json" \
  -d '{"packageName": "com.android.settings"}'
```

### 🔙 Thực hiện global action

```bash
curl -X POST http://localhost:8080/api/action \
  -H "Content-Type: application/json" \
  -d '{"action": "back"}'
```

### 🔧 Lấy system actions

```bash
curl http://localhost:8080/api/system/actions
```

### 📱 Hiển thị/Ẩn overlay

```bash
# Hiển thị overlay
curl -X POST http://localhost:8080/api/overlay/show

# Ẩn overlay
curl -X POST http://localhost:8080/api/overlay/hide
```

## 🎯 Sử dụng với AI

### Python Example

```python
import requests

# Lấy danh sách ứng dụng
response = requests.get('http://localhost:8080/api/apps/user')
apps = response.json()['data']

# Mở ứng dụng đầu tiên
if apps:
    package_name = apps[0]['packageName']
    requests.post('http://localhost:8080/api/apps/launch',
                  json={'packageName': package_name})
```

### JavaScript Example

```javascript
// Lấy danh sách ứng dụng
const response = await fetch("http://localhost:8080/api/apps/user");
const data = await response.json();
const apps = data.data;

// Mở ứng dụng
if (apps.length > 0) {
  await fetch("http://localhost:8080/api/apps/launch", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ packageName: apps[0].packageName }),
  });
}
```

## ⚠️ Lưu ý quan trọng

1. **Quyền Accessibility**: Phải cấp quyền thủ công trong Settings
2. **Chỉ Android**: API chỉ hoạt động trên Android
3. **Thiết bị thật**: Một số tính năng cần thiết bị thật, không hoạt động trên emulator
4. **Bảo mật**: Một số ứng dụng có thể chặn tương tác từ Accessibility Service

## 🐛 Troubleshooting

### Lỗi "flutter command not found"

```bash
# Cài đặt Flutter
sudo snap install flutter
# Hoặc tải từ https://flutter.dev
```

### API không hoạt động

1. Kiểm tra ứng dụng Flutter đang chạy
2. Kiểm tra quyền Accessibility đã được cấp
3. Kiểm tra port 8080 không bị chiếm

### Không mở được ứng dụng

1. Kiểm tra package name đúng
2. Ứng dụng có thể đã được cài đặt
3. Thử với ứng dụng khác

## 📞 Support

Nếu gặp vấn đề, kiểm tra:

- Log trong ứng dụng Flutter
- Response từ API endpoints
- Quyền Accessibility Service
