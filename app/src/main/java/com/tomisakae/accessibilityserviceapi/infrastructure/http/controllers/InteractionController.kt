package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.domain.models.*
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking

/**
 * Controller for interaction endpoints
 */
class InteractionController(
    private val serviceManager: AccessibilityServiceManager
) : BaseController() {
    
    /**
     * POST /click
     */
    fun performClick(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<ClickRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = if (request.nodeId != null) {
            // Click on specific node
            val node = serviceManager.getUiTreeManager().findNodeById(request.nodeId)
            if (node != null) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                ClickResponse(success, Pair(request.x, request.y), true)
            } else {
                ClickResponse(false, null, false)
            }
        } else {
            // Click on coordinates
            runBlocking {
                val success = serviceManager.getGestureManager().performClick(request.x, request.y)
                ClickResponse(success, Pair(request.x, request.y), true)
            }
        }
        
        return createSuccessResponse(result)
    }
    
    /**
     * POST /long-click
     */
    fun performLongClick(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<LongClickRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = if (request.nodeId != null) {
            val node = serviceManager.getUiTreeManager().findNodeById(request.nodeId)
            if (node != null) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                ActionResponse(success, if (success) "Long click performed" else "Long click failed")
            } else {
                ActionResponse(false, "Node not found")
            }
        } else if (request.x != null && request.y != null) {
            runBlocking {
                val success = serviceManager.getGestureManager().performLongClick(
                    request.x, request.y, request.duration
                )
                ActionResponse(success, if (success) "Long click performed" else "Long click failed")
            }
        } else {
            ActionResponse(false, "Either nodeId or coordinates (x, y) must be provided")
        }
        
        return createSuccessResponse(result)
    }
    
    /**
     * POST /double-click
     */
    fun performDoubleClick(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<DoubleClickRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = runBlocking {
            val success = serviceManager.getGestureManager().performDoubleClick(
                request.x, request.y, request.delay
            )
            ActionResponse(success, if (success) "Double click performed" else "Double click failed")
        }
        
        return createSuccessResponse(result)
    }
    
    /**
     * POST /scroll
     */
    fun performScroll(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<ScrollRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = if (request.nodeId != null) {
            val node = serviceManager.getUiTreeManager().findNodeById(request.nodeId)
            if (node != null && node.isScrollable) {
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
        } else {
            // Find first scrollable node and scroll
            val scrollableNodes = serviceManager.getUiTreeManager().findScrollableNodes()
            if (scrollableNodes.isNotEmpty()) {
                val node = scrollableNodes.first()
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
        }
        
        return createSuccessResponse(result)
    }
    
    /**
     * POST /swipe
     */
    fun performSwipe(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<SwipeRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = runBlocking {
            serviceManager.getGestureManager().performSwipe(request)
        }
        
        return createSuccessResponse(result)
    }
    
    /**
     * POST /input-text
     */
    fun performInputText(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<InputTextRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = if (request.nodeId != null) {
            val node = serviceManager.getUiTreeManager().findNodeById(request.nodeId)
            if (node != null) {
                if (request.clearFirst) {
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION)
                }
                val success = node.performAction(
                    AccessibilityNodeInfo.ACTION_SET_TEXT,
                    android.os.Bundle().apply {
                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, request.text)
                    }
                )
                InputTextResponse(success, request.text, true)
            } else {
                InputTextResponse(false, request.text, false)
            }
        } else {
            // Find focused input field
            val rootNode = serviceManager.rootInActiveWindow
            val focusedNode = findFocusedEditableNode(rootNode)
            if (focusedNode != null) {
                if (request.clearFirst) {
                    focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION)
                }
                val success = focusedNode.performAction(
                    AccessibilityNodeInfo.ACTION_SET_TEXT,
                    android.os.Bundle().apply {
                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, request.text)
                    }
                )
                InputTextResponse(success, request.text, true)
            } else {
                InputTextResponse(false, request.text, false)
            }
        }
        
        return createSuccessResponse(result)
    }
    

    
    private fun findFocusedEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        
        if (node.isFocused && node.isEditable) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findFocusedEditableNode(child)
            if (result != null) return result
        }
        
        return null
    }
}
