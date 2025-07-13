package com.tomisakae.accessibilityserviceapi.infrastructure.http

import android.util.Log
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers.*
import fi.iki.elonen.NanoHTTPD

/**
 * Main HTTP server that routes requests to appropriate controllers
 */
class ApiHttpServer(
    hostname: String, 
    port: Int, 
    private val serviceManager: AccessibilityServiceManager
) : NanoHTTPD(hostname, port) {
    
    companion object {
        private const val TAG = "ApiHttpServer"
    }
    
    private val healthController = HealthController(serviceManager)
    private val uiController = UiController(serviceManager)
    private val interactionController = InteractionController(serviceManager)
    private val systemController = SystemController(serviceManager)
    private val appController = AppController(serviceManager)
    private val documentationController = DocumentationController()
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        
        Log.d(TAG, "Request: $method $uri")
        
        return try {
            when {
                // Health endpoints
                uri == "/health" && method == Method.GET -> 
                    healthController.getHealth()
                
                // UI endpoints
                uri == "/ui-tree" && method == Method.GET -> 
                    uiController.getUiTree()
                uri == "/find-elements" && method == Method.POST -> 
                    uiController.findElements(session)
                uri == "/wait-for-element" && method == Method.POST -> 
                    uiController.waitForElement(session)
                
                // Interaction endpoints
                uri == "/click" && method == Method.POST -> 
                    interactionController.performClick(session)
                uri == "/long-click" && method == Method.POST -> 
                    interactionController.performLongClick(session)
                uri == "/double-click" && method == Method.POST -> 
                    interactionController.performDoubleClick(session)
                uri == "/scroll" && method == Method.POST -> 
                    interactionController.performScroll(session)
                uri == "/swipe" && method == Method.POST -> 
                    interactionController.performSwipe(session)
                uri == "/input-text" && method == Method.POST -> 
                    interactionController.performInputText(session)

                
                // System endpoints
                uri == "/home" && method == Method.POST -> 
                    systemController.performHome()
                uri == "/back" && method == Method.POST -> 
                    systemController.performBack()
                uri == "/recent" && method == Method.POST -> 
                    systemController.performRecent()
                uri == "/device-info" && method == Method.GET -> 
                    systemController.getDeviceInfo()
                uri == "/screenshot" && method == Method.GET -> 
                    systemController.takeScreenshot()
                uri == "/open-notifications" && method == Method.POST ->
                    systemController.openNotifications()
                uri == "/open-quick-settings" && method == Method.POST ->
                    systemController.openQuickSettings()
                uri == "/volume" && method == Method.POST ->
                    systemController.controlVolume(session)
                
                // App endpoints
                uri == "/click-app" && method == Method.POST -> 
                    appController.clickApp(session)
                uri == "/launch-app" && method == Method.POST -> 
                    appController.launchApp(session)
                uri == "/close-app" && method == Method.POST -> 
                    appController.closeApp(session)
                uri == "/recent-apps" && method == Method.GET -> 
                    appController.getRecentApps()
                uri == "/recent-open" && method == Method.POST -> 
                    appController.openFromRecent(session)
                uri == "/recent-close" && method == Method.POST -> 
                    appController.closeFromRecent(session)
                
                // Documentation
                uri == "/" && method == Method.GET -> 
                    documentationController.getDocumentation()
                
                else -> createErrorResponse(404, "NOT_FOUND", "Endpoint not found: $uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request", e)
            createErrorResponse(500, "INTERNAL_ERROR", e.message ?: "Unknown error")
        }
    }
    
    private fun createErrorResponse(statusCode: Int, errorCode: String, message: String): Response {
        val errorResponse = mapOf(
            "success" to false,
            "error" to mapOf(
                "code" to errorCode,
                "message" to message
            ),
            "timestamp" to System.currentTimeMillis()
        )
        
        return newFixedLengthResponse(
            when (statusCode) {
                404 -> Response.Status.NOT_FOUND
                500 -> Response.Status.INTERNAL_ERROR
                else -> Response.Status.BAD_REQUEST
            },
            "application/json",
            com.google.gson.Gson().toJson(errorResponse)
        ).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type")
        }
    }
}
