package com.tomisakae.accessibilityserviceapi.infrastructure.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.domain.models.NodeBounds
import com.tomisakae.accessibilityserviceapi.domain.models.UiNode

/**
 * Utility class for converting AccessibilityNodeInfo to domain models
 */
object NodeConversionUtils {
    
    private var nodeCounter = 0
    private val nodeIdMap = mutableMapOf<String, AccessibilityNodeInfo>()
    
    /**
     * Reset conversion state
     */
    fun resetConversionState() {
        nodeCounter = 0
        nodeIdMap.clear()
    }
    
    /**
     * Get node by ID
     */
    fun getNodeById(nodeId: String): AccessibilityNodeInfo? {
        return nodeIdMap[nodeId]
    }
    
    /**
     * Get current node count
     */
    fun getNodeCount(): Int = nodeCounter
    
    /**
     * Convert AccessibilityNodeInfo to UiNode recursively
     */
    fun convertToUiNode(node: AccessibilityNodeInfo): UiNode {
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
     * Convert list of AccessibilityNodeInfo to list of UiNode
     */
    fun convertToUiNodes(nodes: List<AccessibilityNodeInfo>): List<UiNode> {
        return nodes.map { node ->
            val nodeId = generateNodeId(node)
            nodeIdMap[nodeId] = node
            
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            val nodeBounds = NodeBounds.fromRect(bounds)
            
            UiNode(
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
                children = emptyList() // Don't include children for list conversion
            )
        }
    }
    
    /**
     * Generate unique ID for a node
     */
    private fun generateNodeId(node: AccessibilityNodeInfo): String {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val className = node.className?.toString() ?: "unknown"
        val text = node.text?.toString()?.take(20) ?: ""
        val contentDesc = node.contentDescription?.toString()?.take(20) ?: ""
        
        return "${className}_${bounds.left}_${bounds.top}_${bounds.right}_${bounds.bottom}_${text.hashCode()}_${contentDesc.hashCode()}"
    }
    
    /**
     * Extract basic node info without children
     */
    fun extractBasicNodeInfo(node: AccessibilityNodeInfo): UiNode {
        val nodeId = generateNodeId(node)
        nodeIdMap[nodeId] = node
        
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val nodeBounds = NodeBounds.fromRect(bounds)
        
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
            children = emptyList()
        )
    }
    
    /**
     * Check if two nodes are the same
     */
    fun areNodesSame(node1: AccessibilityNodeInfo, node2: AccessibilityNodeInfo): Boolean {
        return generateNodeId(node1) == generateNodeId(node2)
    }
    
    /**
     * Get node summary for debugging
     */
    fun getNodeSummary(node: AccessibilityNodeInfo): String {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        
        return buildString {
            append("Class: ${node.className}")
            append(", Text: '${node.text}'")
            append(", ContentDesc: '${node.contentDescription}'")
            append(", Bounds: $bounds")
            append(", Clickable: ${node.isClickable}")
            append(", Scrollable: ${node.isScrollable}")
            append(", Enabled: ${node.isEnabled}")
        }
    }
}
