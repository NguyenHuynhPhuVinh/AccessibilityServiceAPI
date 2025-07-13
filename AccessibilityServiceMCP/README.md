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

## Sử dụng với Claude Desktop

### 1. Bật Developer Mode

1. Mở **Claude Desktop** → **Settings**
2. Chọn **Developer** → Bật **Developer Mode**

### 2. Cấu hình MCP Server

Tìm file cấu hình Claude Desktop:

- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

Thêm cấu hình vào file:

```json
{
  "mcpServers": {
    "android-accessibility": {
      "command": "node",
      "args": ["/full/path/to/AccessibilityServiceMCP/build/index.js"],
      "env": {
        "ACCESSIBILITY_API_HOST": "192.168.1.5"
      }
    }
  }
}
```

### 3. Restart Claude Desktop

Sau khi cấu hình xong, restart Claude Desktop để áp dụng thay đổi.

## Các Tools có sẵn

### 🏥 System & Health

- `health_check` - Kiểm tra trạng thái API server
- `get_device_info` - Lấy thông tin thiết bị Android
- `get_screenshot` - Chụp ảnh màn hình
- `set_volume` - Điều chỉnh âm lượng
- `open_notifications` - Mở panel thông báo
- `open_quick_settings` - Mở quick settings

### 🔍 UI Operations

- `find_elements` - **🎯 TOOL CHÍNH** - AI sẽ dùng tool này để tìm elements trước khi thực hiện bất kỳ action nào. Có smart fallback system khi không tìm thấy elements
- `get_ui_tree` - Lấy cây UI đầy đủ (chỉ dùng khi cần debug hoặc phân tích chi tiết)

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

## Cách AI sử dụng MCP Tools

### 🎯 `find_elements` - Tool chính cho AI

**AI sẽ ưu tiên sử dụng `find_elements` để tìm kiếm UI elements trước khi thực hiện bất kỳ action nào:**

```typescript
// AI workflow chuẩn:
1. find_elements() → Tìm element cần tương tác
2. click() / input_text() / scroll() → Thực hiện action với tọa độ từ bước 1
```

**Tại sao `find_elements` là tool quan trọng nhất:**

- ✅ **Tìm chính xác** elements theo text, contentDescription, className
- ✅ **Smart fallback** khi không tìm thấy → trả về alternatives
- ✅ **Cung cấp tọa độ** để AI có thể click chính xác
- ✅ **Hiểu context** thông qua `actionType` parameter

### 🧠 Smart Fallback System

API `find_elements` có hệ thống fallback thông minh:

- **Tìm thấy elements** → Trả về kết quả chính xác với tọa độ
- **Không tìm thấy** → Tự động fallback dựa trên `actionType`:
  - `actionType: "click"` → Trả về tất cả clickable elements
  - `actionType: "input"` → Trả về tất cả editable elements
  - `actionType: "scroll"` → Trả về tất cả scrollable elements
  - `actionType: "check"` → Trả về checkable/switch elements
  - `actionType: "read"` → Trả về text elements

**Lợi ích cho AI:**

- 🤖 **Một API call duy nhất** thay vì gọi nhiều tools
- 🎯 **Luôn có thông tin** để tiếp tục workflow
- 📍 **Tọa độ chính xác** cho các action tiếp theo
- 🔄 **Tự động adapt** khi UI thay đổi

## Troubleshooting

### ❌ Không kết nối được API

```
Error: Không thể kết nối đến API server
```

**Giải pháp:**

1. Kiểm tra thiết bị Android đã bật API server
2. Xác nhận IP address trong `.mcp.json` đúng
3. Đảm bảo cùng mạng WiFi
4. Test bằng: `curl http://[IP]:8080/health`

### ❌ MCP Server không khởi động

```
Error: Cannot find module 'build/index.js'
```

**Giải pháp:**

1. Chạy `npm run build` để build TypeScript
2. Kiểm tra đường dẫn trong `.mcp.json` đúng
3. Đảm bảo Node.js 18+

### ❌ Elements không tìm thấy

**Giải pháp:**

1. Sử dụng `get_screenshot` để xem màn hình hiện tại
2. Thử `get_ui_tree` để xem cấu trúc UI
3. Sử dụng `actionType` để kích hoạt fallback
4. Thử tìm bằng `contentDescription` thay vì `text`

## Thông tin bổ sung

- **Port mặc định**: 8080 (có thể thay đổi trong Android app)
- **Timeout**: 15 giây cho mỗi API call
- **Supported Android**: API 24+ (Android 7.0+)
- **Network**: Chỉ hoạt động trong cùng mạng LAN

## License

MIT License - Xem file LICENSE để biết thêm chi tiết.
