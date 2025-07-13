# Android Accessibility Service MCP

## Giới thiệu

MCP Server để điều khiển thiết bị Android thông qua Accessibility Service API. Cho phép AI tương tác với giao diện Android: click, nhập text, cuộn, tìm elements, v.v.

## Yêu cầu hệ thống

- **Node.js** 18+
- **Android device** với Accessibility Service API đã cài đặt và chạy
- **Cùng mạng WiFi** giữa máy tính và thiết bị Android

## Cài đặt và Cấu hình

### 1. Cài đặt dependencies

```bash
npm install
```

### 2. Build TypeScript

```bash
npm run build
```

### 3. Cấu hình IP thiết bị Android

Chỉnh sửa file `.mcp.json` và cập nhật IP của thiết bị Android:

```json
{
  "mcpServers": {
    "android-accessibility": {
      "command": "node",
      "args": ["path/to/build/index.js"],
      "env": {
        "ACCESSIBILITY_API_HOST": "192.168.1.5" // 👈 Thay bằng IP thiết bị Android
      }
    }
  }
}
```

**Cách tìm IP thiết bị Android:**

- Vào **Settings > Wi-Fi > Chọn mạng đang kết nối**
- Hoặc xem trong app **Accessibility Service API**
- Hoặc dùng lệnh: `adb shell ip route`

## Cấu hình với Model Context Protocol (MCP)

### Claude Desktop

1. Mở Claude Desktop và vào Settings
2. Chọn mục Developer và bật Developer Mode
3. Tìm file cấu hình tại:
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
4. Thêm cấu hình MCP vào file:

```json
{
  "mcpServers": {
    "android-accessibility": {
      "command": "node",
      "args": ["/path/to/AccessibilityServiceMCP/build/index.js"],
      "env": {
        "ACCESSIBILITY_API_HOST": "192.168.1.5",
        "ACCESSIBILITY_API_PORT": "8080"
      }
    }
  }
}
```

## Các Tools có sẵn

### 🏥 System & Health

- `health_check` - Kiểm tra trạng thái API server
- `get_device_info` - Lấy thông tin thiết bị Android
- `get_screenshot` - Chụp ảnh màn hình
- `set_volume` - Điều chỉnh âm lượng
- `open_notifications` - Mở panel thông báo
- `open_quick_settings` - Mở quick settings

### 🔍 UI Operations

- `find_elements` - Tìm kiếm UI elements với smart fallback
- `get_ui_tree` - Lấy cây UI đầy đủ

### 👆 Touch Interactions

- `click` - Click tại tọa độ
- `long_click` - Long click (nhấn giữ)
- `double_click` - Double click
- `swipe` - Vuốt từ điểm này đến điểm khác
- `scroll` - Cuộn màn hình

### ⌨️ Input

- `input_text` - Nhập text vào field

### 🧭 Navigation

- `navigate_home` - Về màn hình chính
- `navigate_back` - Quay lại
- `open_recent_apps` - Mở danh sách app gần đây

### 📱 App Management

- `click_app` - Click vào app trên home screen
- `launch_app` - Mở app bằng package name
- `close_app` - Đóng app
- `get_recent_apps` - Lấy danh sách app gần đây

## Ví dụ sử dụng

```typescript
// Tìm và click button
await find_elements({
  text: "Submit",
  actionType: "click",
});

// Nhập text vào input field
await find_elements({
  text: "search",
  actionType: "input",
});
await input_text({
  text: "Hello World",
  clearFirst: true,
});

// Automation workflow
await navigate_home();
await click_app({ appName: "YouTube" });
await find_elements({ text: "AI video", actionType: "click" });
```
