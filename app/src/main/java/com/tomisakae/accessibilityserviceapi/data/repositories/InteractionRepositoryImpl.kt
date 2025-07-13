package com.tomisakae.accessibilityserviceapi.data.repositories

import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.domain.models.*
import com.tomisakae.accessibilityserviceapi.domain.repositories.InteractionRepository
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager

/**
 * Implementation of InteractionRepository
 */
class InteractionRepositoryImpl(
    private val serviceManager: AccessibilityServiceManager
) : InteractionRepository {
    
    override suspend fun performClick(request: ClickRequest): ClickResponse {
        return if (request.nodeId != null) {
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
            val success = serviceManager.getGestureManager().performClick(request.x, request.y)
            ClickResponse(success, Pair(request.x, request.y), true)
        }
    }
    
    override suspend fun performLongClick(request: LongClickRequest): ActionResponse {
        return if (request.nodeId != null) {
            val node = serviceManager.getUiTreeManager().findNodeById(request.nodeId)
            if (node != null) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                ActionResponse(success, if (success) "Long click performed" else "Long click failed")
            } else {
                ActionResponse(false, "Node not found")
            }
        } else if (request.x != null && request.y != null) {
            val success = serviceManager.getGestureManager().performLongClick(
                request.x, request.y, request.duration
            )
            ActionResponse(success, if (success) "Long click performed" else "Long click failed")
        } else {
            ActionResponse(false, "Either nodeId or coordinates (x, y) must be provided")
        }
    }
    
    override suspend fun performDoubleClick(request: DoubleClickRequest): ActionResponse {
        val success = serviceManager.getGestureManager().performDoubleClick(
            request.x, request.y, request.delay
        )
        return ActionResponse(success, if (success) "Double click performed" else "Double click failed")
    }
    
    override suspend fun performScroll(request: ScrollRequest): ScrollResponse {
        return if (request.nodeId != null) {
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
    }
    
    override suspend fun performSwipe(request: SwipeRequest): SwipeResponse {
        return serviceManager.getGestureManager().performSwipe(request)
    }
    
    override suspend fun inputText(request: InputTextRequest): InputTextResponse {
        return if (request.nodeId != null) {
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
