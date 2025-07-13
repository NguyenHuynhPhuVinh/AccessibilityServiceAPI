package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import fi.iki.elonen.NanoHTTPD

/**
 * Controller for documentation endpoints
 */
class DocumentationController : BaseController() {
    
    /**
     * GET /
     */
    fun getDocumentation(): NanoHTTPD.Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Accessibility Service API</title>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        margin: 0; 
                        padding: 20px; 
                        background: #1a1a1a; 
                        color: #e0e0e0; 
                        line-height: 1.6;
                    }
                    .container { 
                        max-width: 1200px; 
                        margin: 0 auto; 
                    }
                    h1 { 
                        color: #00d4aa; 
                        text-align: center; 
                        margin-bottom: 30px;
                        font-size: 2.5em;
                    }
                    h2 { 
                        color: #00d4aa; 
                        border-bottom: 2px solid #333; 
                        padding-bottom: 10px;
                        margin-top: 40px;
                    }
                    .endpoint { 
                        background: #2d2d2d; 
                        padding: 15px; 
                        margin: 10px 0; 
                        border-radius: 8px; 
                        border-left: 4px solid #00d4aa;
                    }
                    .method { 
                        background: #007acc; 
                        color: white; 
                        padding: 4px 8px; 
                        border-radius: 4px; 
                        font-weight: bold; 
                        font-size: 0.9em;
                    }
                    .method.post { background: #28a745; }
                    .method.get { background: #007bff; }
                    code { 
                        background: #1e1e1e; 
                        padding: 2px 6px; 
                        border-radius: 4px; 
                        color: #f8f8f2;
                        font-family: 'Courier New', monospace;
                    }
                    pre { 
                        background: #1e1e1e; 
                        padding: 15px; 
                        border-radius: 8px; 
                        overflow-x: auto;
                        border: 1px solid #333;
                    }
                    .status { 
                        background: #28a745; 
                        color: white; 
                        padding: 10px; 
                        border-radius: 8px; 
                        text-align: center; 
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                        gap: 20px;
                        margin: 20px 0;
                    }
                    .card {
                        background: #2d2d2d;
                        padding: 20px;
                        border-radius: 8px;
                        border: 1px solid #333;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🤖 Accessibility Service API</h1>
                    
                    <div class="status">
                        ✅ API Server đang hoạt động - 20 endpoints sẵn sàng
                    </div>

                    <h2>📊 Health Check</h2>
                    <div class="endpoint">
                        <span class="method get">GET</span> <code>/health</code>
                        <br>Kiểm tra trạng thái server và accessibility service
                    </div>

                    <h2>🌳 UI Operations</h2>
                    <div class="grid">
                        <div class="card">
                            <div class="endpoint">
                                <span class="method get">GET</span> <code>/ui-tree</code>
                                <br>Lấy cấu trúc UI hiện tại của màn hình
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/find-elements</code>
                                <br>Tìm elements theo criteria
                                <br><code>{"text": "OK", "clickable": true}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/wait-for-element</code>
                                <br>Chờ element xuất hiện
                                <br><code>{"text": "Loading", "timeout": 10000}</code>
                            </div>
                        </div>
                    </div>

                    <h2>👆 Interactions</h2>
                    <div class="grid">
                        <div class="card">
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/click</code>
                                <br>Click vào tọa độ hoặc element
                                <br><code>{"x": 100, "y": 200}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/long-click</code>
                                <br>Long click (nhấn giữ)
                                <br><code>{"x": 100, "y": 200, "duration": 1000}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/double-click</code>
                                <br>Double click (nhấn đôi)
                                <br><code>{"x": 100, "y": 200, "delay": 100}</code>
                            </div>
                        </div>
                        <div class="card">
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/scroll</code>
                                <br>Cuộn màn hình
                                <br><code>{"direction": "DOWN"}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/swipe</code>
                                <br>Vuốt màn hình (LEFT/RIGHT/UP/DOWN)
                                <br><code>{"direction": "LEFT"}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/input-text</code>
                                <br>Nhập văn bản
                                <br><code>{"text": "Hello World", "clearFirst": true}</code>
                            </div>
                        </div>
                    </div>

                    <h2>📱 App Management</h2>
                    <div class="grid">
                        <div class="card">
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/click-app</code>
                                <br>Click app trên home screen
                                <br><code>{"appName": "Chrome"}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/launch-app</code>
                                <br>Mở app bằng package name
                                <br><code>{"packageName": "com.android.chrome"}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/close-app</code>
                                <br>Đóng app
                                <br><code>{"packageName": "com.android.chrome"}</code>
                            </div>
                        </div>
                        <div class="card">
                            <div class="endpoint">
                                <span class="method get">GET</span> <code>/recent-apps</code>
                                <br>Lấy danh sách recent apps
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/recent-open</code>
                                <br>Mở app từ recent
                                <br><code>{"packageName": "com.example.app"}</code>
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/recent-close</code>
                                <br>Đóng app từ recent
                                <br><code>{"packageName": "com.example.app"}</code>
                            </div>
                        </div>
                    </div>

                    <h2>🏠 Navigation</h2>
                    <div class="grid">
                        <div class="card">
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/home</code>
                                <br>Nhấn nút Home
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/back</code>
                                <br>Nhấn nút Back
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/recent</code>
                                <br>Mở Recent Apps
                            </div>
                        </div>
                    </div>



                    <h2>⚙️ System</h2>
                    <div class="grid">
                        <div class="card">
                            <div class="endpoint">
                                <span class="method get">GET</span> <code>/device-info</code>
                                <br>Thông tin thiết bị và màn hình
                            </div>
                            <div class="endpoint">
                                <span class="method get">GET</span> <code>/screenshot</code>
                                <br>Chụp màn hình
                            </div>
                            <div class="endpoint">
                                <span class="method post">POST</span> <code>/volume</code>
                                <br>Điều khiển âm lượng
                                <br><code>{"type": "MEDIA", "action": "UP"}</code>
                            </div>
                        </div>
                    </div>

                    <h2>💻 Usage Examples</h2>
                    <pre><code># Health check
curl -X GET http://localhost:8080/health

# Get UI tree
curl -X GET http://localhost:8080/ui-tree

# Click action
curl -X POST http://localhost:8080/click \\
  -H "Content-Type: application/json" \\
  -d '{"x": 100, "y": 200}'

# Swipe left (chuyển trang home)
curl -X POST http://localhost:8080/swipe \\
  -H "Content-Type: application/json" \\
  -d '{"direction": "LEFT", "duration": 300}'

# Long click
curl -X POST http://localhost:8080/long-click \\
  -H "Content-Type: application/json" \\
  -d '{"x": 540, "y": 1200, "duration": 1000}'

# Launch app
curl -X POST http://localhost:8080/launch-app \\
  -H "Content-Type: application/json" \\
  -d '{"packageName": "com.android.settings"}'

# Volume control
curl -X POST http://localhost:8080/volume \\
  -H "Content-Type: application/json" \\
  -d '{"type": "MEDIA", "action": "UP"}'

# Find elements
curl -X POST http://localhost:8080/find-elements \\
  -H "Content-Type: application/json" \\
  -d '{"clickable": true}'

# Python example
import requests
response = requests.get('http://localhost:8080/health')
print(response.json())

# Get device info
device_info = requests.get('http://localhost:8080/device-info')
print(f"Screen: {device_info.json()['data']['screenWidth']}x{device_info.json()['data']['screenHeight']}")

# Take screenshot
screenshot = requests.get('http://localhost:8080/screenshot')
print(screenshot.json())</code></pre>

                    <h2>🔗 Network Access</h2>
                    <p>Để truy cập từ desktop/máy tính khác, thay <code>localhost</code> bằng IP của điện thoại:</p>
                    <pre><code>curl -X GET http://[PHONE_IP]:8080/health</code></pre>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "text/html",
            html
        ).apply {
            addHeader("Access-Control-Allow-Origin", "*")
        }
    }
}
