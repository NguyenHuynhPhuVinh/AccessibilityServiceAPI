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
                uri == "/click-app" && method == Method.POST -> handleClickApp(session)
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
