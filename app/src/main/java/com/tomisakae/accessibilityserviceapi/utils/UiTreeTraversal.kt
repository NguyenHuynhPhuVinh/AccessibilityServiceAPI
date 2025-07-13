package com.tomisakae.accessibilityserviceapi.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.models.NodeBounds
import com.tomisakae.accessibilityserviceapi.models.UiNode
import com.tomisakae.accessibilityserviceapi.models.UiTreeResponse
import java.util.*

/**
 * Utility class for traversing and converting AccessibilityNodeInfo to UiNode
 */
object UiTreeTraversal {
    
    private var nodeCounter = 0
    private val nodeIdMap = mutableMapOf<String, AccessibilityNodeInfo>()
    
    /**
     * Capture the complete UI tree from root node
     */
    fun captureUiTree(rootNode: AccessibilityNodeInfo?): UiTreeResponse {
        nodeCounter = 0
        nodeIdMap.clear()
        
        val uiNode = rootNode?.let { convertToUiNode(it) }
        
        return UiTreeResponse(
            rootNode = uiNode,
            totalNodes = nodeCounter
        )
    }
    
    /**
     * Convert AccessibilityNodeInfo to UiNode recursively
     */
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
    
    /**
     * Generate unique ID for a node based on its properties
     */
    private fun generateNodeId(node: AccessibilityNodeInfo): String {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        
        val identifier = StringBuilder()
        identifier.append(node.className ?: "unknown")
        identifier.append("_")
        identifier.append(bounds.left).append("_").append(bounds.top)
        identifier.append("_")
        identifier.append(bounds.right).append("_").append(bounds.bottom)
        
        // Add text or content description if available
        node.text?.let { identifier.append("_").append(it.toString().hashCode()) }
        node.contentDescription?.let { identifier.append("_").append(it.toString().hashCode()) }
        
        return identifier.toString().replace(" ", "_")
    }
    
    /**
     * Find node by ID
     */
    fun findNodeById(nodeId: String): AccessibilityNodeInfo? {
        return nodeIdMap[nodeId]
    }
    
    /**
     * Find nodes by text content
     */
    fun findNodesByText(rootNode: AccessibilityNodeInfo?, searchText: String): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        rootNode?.let { searchNodesByText(it, searchText, foundNodes) }
        return foundNodes
    }
    
    private fun searchNodesByText(
        node: AccessibilityNodeInfo, 
        searchText: String, 
        foundNodes: MutableList<AccessibilityNodeInfo>
    ) {
        // Check current node
        val nodeText = node.text?.toString() ?: ""
        val nodeDescription = node.contentDescription?.toString() ?: ""
        
        if (nodeText.contains(searchText, ignoreCase = true) || 
            nodeDescription.contains(searchText, ignoreCase = true)) {
            foundNodes.add(node)
        }
        
        // Search children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByText(child, searchText, foundNodes)
            }
        }
    }
    
    /**
     * Find clickable nodes
     */
    fun findClickableNodes(rootNode: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        rootNode?.let { searchClickableNodes(it, clickableNodes) }
        return clickableNodes
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
    
    /**
     * Find scrollable nodes
     */
    fun findScrollableNodes(rootNode: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        val scrollableNodes = mutableListOf<AccessibilityNodeInfo>()
        rootNode?.let { searchScrollableNodes(it, scrollableNodes) }
        return scrollableNodes
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
    
    /**
     * Clear cached node references to prevent memory leaks
     */
    fun clearCache() {
        nodeIdMap.clear()
        nodeCounter = 0
    }
}
