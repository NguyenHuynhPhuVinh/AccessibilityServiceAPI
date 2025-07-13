package com.tomisakae.accessibilityserviceapi.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import com.tomisakae.accessibilityserviceapi.models.*
import com.tomisakae.accessibilityserviceapi.utils.UiTreeTraversal
import kotlinx.coroutines.*

class AccessibilityApiService : AccessibilityService() {
    
    companion object {
        private const val TAG = "AccessibilityApiService"
        private var instance: AccessibilityApiService? = null
        private var httpServer: ApiHttpServer? = null
        private var serviceStartTime: Long = 0
        
        fun getInstance(): AccessibilityApiService? = instance
        
        fun isServerRunning(): Boolean = httpServer?.isAlive == true
        
        fun getUptime(): Long = if (serviceStartTime > 0) {
            System.currentTimeMillis() - serviceStartTime
        } else 0
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceStartTime = System.currentTimeMillis()
        
        Log.d(TAG, "Accessibility Service Connected")
        
        // Start HTTP server
        startHttpServer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceStartTime = 0
        
        // Stop HTTP server
        stopHttpServer()
        
        // Cancel all coroutines
        serviceScope.cancel()
        
        // Clear UI tree cache
        UiTreeTraversal.clearCache()
        
        Log.d(TAG, "Accessibility Service Destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
        // For now, we'll just log them
        event?.let {
            Log.v(TAG, "Accessibility Event: ${it.eventType}")
        }
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted")
    }
    
    private fun startHttpServer() {
        try {
            httpServer = ApiHttpServer("0.0.0.0", 8080, this)
            httpServer?.start()
            Log.i(TAG, "HTTP Server started on 0.0.0.0:8080")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start HTTP server", e)
        }
    }
    
    private fun stopHttpServer() {
        httpServer?.stop()
        httpServer = null
        Log.i(TAG, "HTTP Server stopped")
    }
    
    /**
     * Get current UI tree
     */
    fun getCurrentUiTree(): UiTreeResponse {
        val rootNode = rootInActiveWindow
        return UiTreeTraversal.captureUiTree(rootNode)
    }
    
    /**
     * Perform click action
     */
    fun performClick(request: ClickRequest): ClickResponse {
        return try {
            if (request.nodeId != null) {
                // Click on specific node
                val node = UiTreeTraversal.findNodeById(request.nodeId)
                if (node != null) {
                    val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    ClickResponse(success, Pair(request.x, request.y), true)
                } else {
                    ClickResponse(false, null, false)
                }
            } else {
                // Click on coordinates
                val success = performGestureClick(request.x, request.y)
                ClickResponse(success, Pair(request.x, request.y), true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing click", e)
            ClickResponse(false, null, false)
        }
    }
    
    /**
     * Perform scroll action
     */
    fun performScroll(request: ScrollRequest): ScrollResponse {
        return try {
            val node = if (request.nodeId != null) {
                UiTreeTraversal.findNodeById(request.nodeId)
            } else {
                // Find first scrollable node
                UiTreeTraversal.findScrollableNodes(rootInActiveWindow).firstOrNull()
            }
            
            if (node != null) {
                val action = when (request.direction) {
                    ScrollDirection.UP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    ScrollDirection.DOWN -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    ScrollDirection.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    ScrollDirection.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                }
                
                val success = node.performAction(action)
                ScrollResponse(success, request.direction, true)
            } else {
                ScrollResponse(false, request.direction, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing scroll", e)
            ScrollResponse(false, request.direction, false)
        }
    }
    
    /**
     * Perform text input
     */
    fun performTextInput(request: InputTextRequest): InputTextResponse {
        return try {
            val node = if (request.nodeId != null) {
                UiTreeTraversal.findNodeById(request.nodeId)
            } else {
                // Find focused input field
                findFocusedEditText()
            }
            
            if (node != null) {
                var success = true
                
                // Clear existing text if requested
                if (request.clearFirst) {
                    // Try to clear text by setting empty text first
                    val clearArguments = Bundle()
                    clearArguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
                    success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearArguments)
                }
                
                // Input new text
                if (success) {
                    val arguments = Bundle()
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, request.text)
                    success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                }
                
                InputTextResponse(success, request.text, true)
            } else {
                InputTextResponse(false, request.text, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing text input", e)
            InputTextResponse(false, request.text, false)
        }
    }
    
    private fun performGestureClick(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    private fun findFocusedEditText(): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }
}
