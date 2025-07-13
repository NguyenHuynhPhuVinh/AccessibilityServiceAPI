package com.tomisakae.accessibilityserviceapi.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tomisakae.accessibilityserviceapi.models.*
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class ApiHttpServer(hostname: String, port: Int, private val accessibilityService: AccessibilityApiService) : NanoHTTPD(hostname, port) {
    
    companion object {
        private const val TAG = "ApiHttpServer"
    }
    
    private val gson = Gson()
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        
        Log.d(TAG, "Request: $method $uri")
        
        return try {
            when {
                uri == "/health" && method == Method.GET -> handleHealth()
                uri == "/ui-tree" && method == Method.GET -> handleUiTree()
                uri == "/click" && method == Method.POST -> handleClick(session)
                uri == "/scroll" && method == Method.POST -> handleScroll(session)
                uri == "/input-text" && method == Method.POST -> handleInputText(session)
                uri == "/swipe" && method == Method.POST -> handleSwipe(session)
                uri == "/home" && method == Method.POST -> handleHome()
                uri == "/back" && method == Method.POST -> handleBack()
                uri == "/recent" && method == Method.POST -> handleRecent()
                uri == "/recent-apps" && method == Method.GET -> handleGetRecentApps()
                uri == "/recent-open" && method == Method.POST -> handleOpenFromRecent(session)
                uri == "/recent-close" && method == Method.POST -> handleCloseFromRecent(session)
                uri == "/click-app" && method == Method.POST -> handleClickApp(session)
                uri == "/find-elements" && method == Method.POST -> handleFindElements(session)
                uri == "/long-click" && method == Method.POST -> handleLongClick(session)
                uri == "/double-click" && method == Method.POST -> handleDoubleClick(session)
                uri == "/pinch" && method == Method.POST -> handlePinch(session)
                uri == "/rotate" && method == Method.POST -> handleRotate(session)
                uri == "/screenshot" && method == Method.GET -> handleScreenshot()
                uri == "/device-info" && method == Method.GET -> handleDeviceInfo()
                uri == "/notifications" && method == Method.GET -> handleNotifications()
                uri == "/open-notifications" && method == Method.POST -> handleOpenNotifications()
                uri == "/open-quick-settings" && method == Method.POST -> handleOpenQuickSettings()
                uri == "/volume" && method == Method.POST -> handleVolume(session)
                uri == "/brightness" && method == Method.POST -> handleBrightness(session)
                uri == "/wifi" && method == Method.POST -> handleWifi(session)
                uri == "/bluetooth" && method == Method.POST -> handleBluetooth(session)
                uri == "/airplane-mode" && method == Method.POST -> handleAirplaneMode(session)
                uri == "/launch-app" && method == Method.POST -> handleLaunchApp(session)
                uri == "/close-app" && method == Method.POST -> handleCloseApp(session)
                uri == "/wait-for-element" && method == Method.POST -> handleWaitForElement(session)
                uri == "/" && method == Method.GET -> handleRoot()
                else -> createErrorResponse(404, "NOT_FOUND", "Endpoint not found: $uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request", e)
            createErrorResponse(500, "INTERNAL_ERROR", e.message ?: "Unknown error")
        }
    }
    
    private fun handleHealth(): Response {
        val healthData = HealthResponse(
            status = "OK",
            accessibilityServiceEnabled = true,
            uptime = AccessibilityApiService.getUptime()
        )
        
        val response = ApiResponse(
            success = true,
            data = healthData
        )
        
        return createJsonResponse(200, response)
    }
    
    private fun handleUiTree(): Response {
        val uiTreeData = accessibilityService.getCurrentUiTree()
        
        val response = ApiResponse(
            success = true,
            data = uiTreeData
        )
        
        return createJsonResponse(200, response)
    }
    
    private fun handleClick(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)
        
        return try {
            val clickRequest = gson.fromJson(requestBody, ClickRequest::class.java)
            val clickResponse = accessibilityService.performClick(clickRequest)
            
            val response = ApiResponse(
                success = clickResponse.clicked,
                data = clickResponse
            )
            
            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }
    
    private fun handleScroll(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)
        
        return try {
            val scrollRequest = gson.fromJson(requestBody, ScrollRequest::class.java)
            val scrollResponse = accessibilityService.performScroll(scrollRequest)
            
            val response = ApiResponse(
                success = scrollResponse.scrolled,
                data = scrollResponse
            )
            
            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }
    
    private fun handleInputText(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)
        
        return try {
            val inputRequest = gson.fromJson(requestBody, InputTextRequest::class.java)
            val inputResponse = accessibilityService.performTextInput(inputRequest)
            
            val response = ApiResponse(
                success = inputResponse.inputSuccess,
                data = inputResponse
            )
            
            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleSwipe(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val swipeRequest = gson.fromJson(requestBody, SwipeRequest::class.java)

            // Validate duration
            val validatedRequest = swipeRequest.copy(
                duration = if (swipeRequest.duration <= 0) 300L else swipeRequest.duration
            )

            val swipeResponse = accessibilityService.performSwipe(validatedRequest)

            val response = ApiResponse(
                success = swipeResponse.swiped,
                data = swipeResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleSwipe", e)
            createErrorResponse(500, "SWIPE_ERROR", e.message ?: "Unknown swipe error")
        }
    }

    private fun handleHome(): Response {
        val homeResponse = accessibilityService.performHome()

        val response = ApiResponse(
            success = homeResponse.success,
            data = homeResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleBack(): Response {
        val backResponse = accessibilityService.performBack()

        val response = ApiResponse(
            success = backResponse.success,
            data = backResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleRecent(): Response {
        val recentResponse = accessibilityService.performRecent()

        val response = ApiResponse(
            success = recentResponse.success,
            data = recentResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleGetRecentApps(): Response {
        val recentAppsResponse = accessibilityService.getRecentApps()

        val response = ApiResponse(
            success = recentAppsResponse.success,
            data = recentAppsResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleOpenFromRecent(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val openRequest = gson.fromJson(requestBody, RecentAppRequest::class.java)
            val openResponse = accessibilityService.openFromRecent(openRequest)

            val response = ApiResponse(
                success = openResponse.success,
                data = openResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleCloseFromRecent(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val closeRequest = gson.fromJson(requestBody, RecentAppRequest::class.java)
            val closeResponse = accessibilityService.closeFromRecent(closeRequest)

            val response = ApiResponse(
                success = closeResponse.success,
                data = closeResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleClickApp(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val clickAppRequest = gson.fromJson(requestBody, ClickAppRequest::class.java)
            val clickAppResponse = accessibilityService.performClickApp(clickAppRequest)

            val response = ApiResponse(
                success = clickAppResponse.success,
                data = clickAppResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleFindElements(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val findRequest = gson.fromJson(requestBody, FindElementsRequest::class.java)
            val findResponse = accessibilityService.performFindElements(findRequest)

            val response = ApiResponse(
                success = true,
                data = findResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleLongClick(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val longClickRequest = gson.fromJson(requestBody, LongClickRequest::class.java)
            val longClickResponse = accessibilityService.performLongClick(longClickRequest)

            val response = ApiResponse(
                success = longClickResponse.success,
                data = longClickResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleDoubleClick(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val doubleClickRequest = gson.fromJson(requestBody, DoubleClickRequest::class.java)
            val doubleClickResponse = accessibilityService.performDoubleClick(doubleClickRequest)

            val response = ApiResponse(
                success = doubleClickResponse.success,
                data = doubleClickResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleScreenshot(): Response {
        val screenshotResponse = accessibilityService.takeScreenshot()

        val response = ApiResponse(
            success = screenshotResponse.success,
            data = screenshotResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleDeviceInfo(): Response {
        val deviceInfo = accessibilityService.getDeviceInfo()

        val response = ApiResponse(
            success = true,
            data = deviceInfo
        )

        return createJsonResponse(200, response)
    }

    private fun handleOpenNotifications(): Response {
        val notificationResponse = accessibilityService.openNotifications()

        val response = ApiResponse(
            success = notificationResponse.success,
            data = notificationResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleOpenQuickSettings(): Response {
        val quickSettingsResponse = accessibilityService.openQuickSettings()

        val response = ApiResponse(
            success = quickSettingsResponse.success,
            data = quickSettingsResponse
        )

        return createJsonResponse(200, response)
    }

    private fun handleLaunchApp(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val launchRequest = gson.fromJson(requestBody, LaunchAppRequest::class.java)
            val launchResponse = accessibilityService.launchApp(launchRequest)

            val response = ApiResponse(
                success = launchResponse.success,
                data = launchResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleWaitForElement(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val waitRequest = gson.fromJson(requestBody, WaitForElementRequest::class.java)
            val waitResponse = accessibilityService.waitForElement(waitRequest)

            val response = ApiResponse(
                success = waitResponse.success,
                data = waitResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    // Placeholder handlers for other endpoints
    private fun handlePinch(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Pinch gesture not implemented yet")
    private fun handleRotate(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Rotate gesture not implemented yet")
    private fun handleNotifications(): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Notifications not implemented yet")
    private fun handleVolume(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Volume control not implemented yet")
    private fun handleBrightness(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Brightness control not implemented yet")
    private fun handleWifi(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "WiFi control not implemented yet")
    private fun handleBluetooth(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Bluetooth control not implemented yet")
    private fun handleAirplaneMode(session: IHTTPSession): Response = createErrorResponse(501, "NOT_IMPLEMENTED", "Airplane mode not implemented yet")
    private fun handleCloseApp(session: IHTTPSession): Response {
        val requestBody = getRequestBody(session)

        return try {
            val closeRequest = gson.fromJson(requestBody, CloseAppRequest::class.java)
            val closeResponse = accessibilityService.closeApp(closeRequest)

            val response = ApiResponse(
                success = closeResponse.success,
                data = closeResponse
            )

            createJsonResponse(200, response)
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "INVALID_JSON", "Invalid JSON in request body")
        }
    }

    private fun handleRoot(): Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Accessibility Service API</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-radius: 5px; }
                    .method { font-weight: bold; color: #007acc; }
                </style>
            </head>
            <body>
                <h1>Android Accessibility Service API</h1>
                <p>Server đang chạy và sẵn sàng nhận requests.</p>
                
                <h2>Available Endpoints:</h2>
                
                <div class="endpoint">
                    <span class="method">GET</span> /health
                    <br>Kiểm tra trạng thái server và accessibility service
                </div>
                
                <div class="endpoint">
                    <span class="method">GET</span> /ui-tree
                    <br>Lấy cấu trúc UI tree hiện tại
                </div>
                
                <div class="endpoint">
                    <span class="method">POST</span> /click
                    <br>Thực hiện click action
                    <br>Body: {"x": 100, "y": 200} hoặc {"nodeId": "node_id"}
                </div>
                
                <div class="endpoint">
                    <span class="method">POST</span> /scroll
                    <br>Thực hiện scroll action
                    <br>Body: {"direction": "UP|DOWN|LEFT|RIGHT", "nodeId": "optional"}
                </div>
                
                <div class="endpoint">
                    <span class="method">POST</span> /input-text
                    <br>Nhập văn bản vào input field
                    <br>Body: {"text": "Hello World", "nodeId": "optional", "clearFirst": true}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /swipe
                    <br>Vuốt màn hình (LEFT, RIGHT, UP, DOWN)
                    <br>Body: {"direction": "LEFT", "duration": 300}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /home
                    <br>Nhấn nút Home
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /back
                    <br>Nhấn nút Back
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /recent
                    <br>Mở Recent Apps UI
                </div>

                <div class="endpoint">
                    <span class="method">GET</span> /recent-apps
                    <br>Lấy danh sách apps trong Recent
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /recent-open
                    <br>Mở app từ Recent Apps
                    <br>Body: {"packageName": "com.example.app"} hoặc {"position": 0}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /recent-close
                    <br>Xóa app khỏi Recent Apps
                    <br>Body: {"packageName": "com.example.app"} hoặc {"appName": "Chrome"}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /click-app
                    <br>Click vào app trên home screen
                    <br>Body: {"appName": "Chrome"} hoặc {"packageName": "com.android.chrome"}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /find-elements
                    <br>Tìm elements theo criteria
                    <br>Body: {"text": "OK", "clickable": true}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /long-click
                    <br>Long click (nhấn giữ)
                    <br>Body: {"x": 100, "y": 200, "duration": 1000}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /double-click
                    <br>Double click (nhấn đôi)
                    <br>Body: {"x": 100, "y": 200, "delay": 100}
                </div>

                <div class="endpoint">
                    <span class="method">GET</span> /device-info
                    <br>Thông tin thiết bị và màn hình
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /open-notifications
                    <br>Mở panel thông báo
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /open-quick-settings
                    <br>Mở quick settings
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /launch-app
                    <br>Mở app theo package name
                    <br>Body: {"packageName": "com.android.chrome"}
                </div>

                <div class="endpoint">
                    <span class="method">POST</span> /wait-for-element
                    <br>Chờ element xuất hiện
                    <br>Body: {"text": "OK", "timeout": 10000}
                </div>
                
                <h2>Example Usage:</h2>
                <pre>
# Local access
curl -X GET http://localhost:8080/health

# Network access (thay YOUR_PHONE_IP bằng IP thực của điện thoại)
curl -X GET http://YOUR_PHONE_IP:8080/health
curl -X GET http://YOUR_PHONE_IP:8080/ui-tree
curl -X POST http://YOUR_PHONE_IP:8080/click -H "Content-Type: application/json" -d '{"x": 100, "y": 200}'

# Python example
import requests
response = requests.get('http://YOUR_PHONE_IP:8080/health')
print(response.json())

# Swipe left (chuyển trang home)
curl -X POST http://YOUR_PHONE_IP:8080/swipe -H "Content-Type: application/json" -d '{"direction": "LEFT"}'

# Nhấn nút Home
curl -X POST http://YOUR_PHONE_IP:8080/home

# Click vào app Chrome
curl -X POST http://YOUR_PHONE_IP:8080/click-app -H "Content-Type: application/json" -d '{"appName": "Chrome"}'
                </pre>
            </body>
            </html>
        """.trimIndent()
        
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }
    
    private fun getRequestBody(session: IHTTPSession): String {
        val files = mutableMapOf<String, String>()
        session.parseBody(files)
        return files["postData"] ?: ""
    }
    
    private fun createJsonResponse(status: Int, data: Any): Response {
        val json = gson.toJson(data)
        val response = newFixedLengthResponse(
            Response.Status.lookup(status),
            "application/json",
            json
        )
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type")
        return response
    }
    
    private fun createErrorResponse(status: Int, code: String, message: String): Response {
        val errorResponse = ApiResponse<Nothing>(
            success = false,
            error = "$code: $message"
        )
        return createJsonResponse(status, errorResponse)
    }
}
