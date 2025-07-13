package com.tomisakae.accessibilityserviceapi.infrastructure.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Manager for UI tree operations
 */
class UiTreeManager(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "UiTreeManager"
    }
    
    private var nodeCounter = 0
    private val nodeIdMap = mutableMapOf<String, AccessibilityNodeInfo>()
    
    /**
     * Get current UI tree
     */
    fun getCurrentUiTree(): UiTreeResponse {
        nodeCounter = 0
        nodeIdMap.clear()
        
        val rootNode = service.rootInActiveWindow
        val uiNode = rootNode?.let { convertToUiNode(it) }
        
        return UiTreeResponse(
            rootNode = uiNode,
            totalNodes = nodeCounter
        )
    }
    
    /**
     * Find elements by criteria
     */
    fun findElements(request: FindElementsRequest): FindElementsResponse {
        val rootNode = service.rootInActiveWindow
        val foundElements = mutableListOf<UiNode>()
        
        if (rootNode != null) {
            searchElementsByCriteria(rootNode, request, foundElements)
        }
        
        return FindElementsResponse(foundElements, foundElements.size)
    }
    
    /**
     * Find node by ID
     */
    fun findNodeById(nodeId: String): AccessibilityNodeInfo? {
        return nodeIdMap[nodeId]
    }
    
    /**
     * Find nodes by text
     */
    fun findNodesByText(rootNode: AccessibilityNodeInfo, text: String): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByText(rootNode, text, foundNodes)
        return foundNodes
    }
    
    /**
     * Find clickable nodes
     */
    fun findClickableNodes(): List<AccessibilityNodeInfo> {
        val rootNode = service.rootInActiveWindow
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        rootNode?.let { searchClickableNodes(it, clickableNodes) }
        return clickableNodes
    }
    
    /**
     * Find scrollable nodes
     */
    fun findScrollableNodes(): List<AccessibilityNodeInfo> {
        val rootNode = service.rootInActiveWindow
        val scrollableNodes = mutableListOf<AccessibilityNodeInfo>()
        rootNode?.let { searchScrollableNodes(it, scrollableNodes) }
        return scrollableNodes
    }
    
    private fun convertToUiNode(node: AccessibilityNodeInfo): UiNode {
        nodeCounter++
        
        // Generate unique ID for this node
        val nodeId = generateNodeId(node)
        nodeIdMap[nodeId] = node
        
        // Get bounds
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val nodeBounds = NodeBounds.fromRect(bounds)
        
        // Convert children
        val children = mutableListOf<UiNode>()
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                children.add(convertToUiNode(child))
            }
        }
        
        return UiNode(
            id = nodeId,
            className = node.className?.toString(),
            packageName = node.packageName?.toString(),
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            bounds = nodeBounds,
            isClickable = node.isClickable,
            isScrollable = node.isScrollable,
            isCheckable = node.isCheckable,
            isChecked = node.isChecked,
            isEnabled = node.isEnabled,
            isFocusable = node.isFocusable,
            isFocused = node.isFocused,
            isSelected = node.isSelected,
            isPassword = node.isPassword,
            children = children
        )
    }
    
    private fun generateNodeId(node: AccessibilityNodeInfo): String {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val className = node.className?.toString() ?: "unknown"
        return "${className}_${bounds.left}_${bounds.top}_${bounds.right}_${bounds.bottom}"
    }
    
    private fun searchElementsByCriteria(
        node: AccessibilityNodeInfo,
        request: FindElementsRequest,
        foundElements: MutableList<UiNode>
    ) {
        var matches = true
        
        // Check text
        if (request.text != null) {
            val nodeText = node.text?.toString() ?: ""
            if (!nodeText.contains(request.text, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check className
        if (request.className != null) {
            val nodeClassName = node.className?.toString() ?: ""
            if (!nodeClassName.contains(request.className, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check packageName
        if (request.packageName != null) {
            val nodePackageName = node.packageName?.toString() ?: ""
            if (!nodePackageName.contains(request.packageName, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check contentDescription
        if (request.contentDescription != null) {
            val nodeContentDescription = node.contentDescription?.toString() ?: ""
            if (!nodeContentDescription.contains(request.contentDescription, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check clickable
        if (request.clickable != null && node.isClickable != request.clickable) {
            matches = false
        }
        
        // Check scrollable
        if (request.scrollable != null && node.isScrollable != request.scrollable) {
            matches = false
        }
        
        if (matches) {
            foundElements.add(convertToUiNode(node))
        }
        
        // Search children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchElementsByCriteria(child, request, foundElements)
            }
        }
    }
    
    private fun searchNodesByText(
        node: AccessibilityNodeInfo,
        text: String,
        foundNodes: MutableList<AccessibilityNodeInfo>
    ) {
        val nodeText = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        
        if (nodeText.contains(text, ignoreCase = true) || 
            contentDesc.contains(text, ignoreCase = true)) {
            foundNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByText(child, text, foundNodes)
            }
        }
    }
    
    private fun searchClickableNodes(
        node: AccessibilityNodeInfo,
        clickableNodes: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.isClickable) {
            clickableNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchClickableNodes(child, clickableNodes)
            }
        }
    }
    
    private fun searchScrollableNodes(
        node: AccessibilityNodeInfo,
        scrollableNodes: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.isScrollable) {
            scrollableNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchScrollableNodes(child, scrollableNodes)
            }
        }
    }
}
