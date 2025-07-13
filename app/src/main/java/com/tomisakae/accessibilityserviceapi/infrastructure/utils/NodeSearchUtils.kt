package com.tomisakae.accessibilityserviceapi.infrastructure.utils

import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.domain.models.FindElementsRequest

/**
 * Utility class for searching accessibility nodes
 */
object NodeSearchUtils {
    
    /**
     * Find nodes by text content
     */
    fun findNodesByText(
        rootNode: AccessibilityNodeInfo,
        text: String,
        ignoreCase: Boolean = true
    ): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByText(rootNode, text, foundNodes, ignoreCase)
        return foundNodes
    }
    
    /**
     * Find nodes by class name
     */
    fun findNodesByClassName(
        rootNode: AccessibilityNodeInfo,
        className: String,
        ignoreCase: Boolean = true
    ): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByClassName(rootNode, className, foundNodes, ignoreCase)
        return foundNodes
    }
    
    /**
     * Find nodes by package name
     */
    fun findNodesByPackageName(
        rootNode: AccessibilityNodeInfo,
        packageName: String,
        ignoreCase: Boolean = true
    ): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByPackageName(rootNode, packageName, foundNodes, ignoreCase)
        return foundNodes
    }
    
    /**
     * Find nodes by content description
     */
    fun findNodesByContentDescription(
        rootNode: AccessibilityNodeInfo,
        contentDescription: String,
        ignoreCase: Boolean = true
    ): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByContentDescription(rootNode, contentDescription, foundNodes, ignoreCase)
        return foundNodes
    }
    
    /**
     * Find clickable nodes
     */
    fun findClickableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        searchClickableNodes(rootNode, clickableNodes)
        return clickableNodes
    }
    
    /**
     * Find scrollable nodes
     */
    fun findScrollableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val scrollableNodes = mutableListOf<AccessibilityNodeInfo>()
        searchScrollableNodes(rootNode, scrollableNodes)
        return scrollableNodes
    }
    
    /**
     * Find editable nodes
     */
    fun findEditableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val editableNodes = mutableListOf<AccessibilityNodeInfo>()
        searchEditableNodes(rootNode, editableNodes)
        return editableNodes
    }
    
    /**
     * Find focused node
     */
    fun findFocusedNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (rootNode.isFocused) {
            return rootNode
        }
        
        for (i in 0 until rootNode.childCount) {
            rootNode.getChild(i)?.let { child ->
                val result = findFocusedNode(child)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    /**
     * Find nodes by multiple criteria
     */
    fun findNodesByCriteria(
        rootNode: AccessibilityNodeInfo,
        criteria: FindElementsRequest
    ): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByCriteria(rootNode, criteria, foundNodes)
        return foundNodes
    }
    
    private fun searchNodesByText(
        node: AccessibilityNodeInfo,
        text: String,
        foundNodes: MutableList<AccessibilityNodeInfo>,
        ignoreCase: Boolean
    ) {
        val nodeText = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        
        if (nodeText.contains(text, ignoreCase) || contentDesc.contains(text, ignoreCase)) {
            foundNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByText(child, text, foundNodes, ignoreCase)
            }
        }
    }
    
    private fun searchNodesByClassName(
        node: AccessibilityNodeInfo,
        className: String,
        foundNodes: MutableList<AccessibilityNodeInfo>,
        ignoreCase: Boolean
    ) {
        val nodeClassName = node.className?.toString() ?: ""
        
        if (nodeClassName.contains(className, ignoreCase)) {
            foundNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByClassName(child, className, foundNodes, ignoreCase)
            }
        }
    }
    
    private fun searchNodesByPackageName(
        node: AccessibilityNodeInfo,
        packageName: String,
        foundNodes: MutableList<AccessibilityNodeInfo>,
        ignoreCase: Boolean
    ) {
        val nodePackageName = node.packageName?.toString() ?: ""
        
        if (nodePackageName.contains(packageName, ignoreCase)) {
            foundNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByPackageName(child, packageName, foundNodes, ignoreCase)
            }
        }
    }
    
    private fun searchNodesByContentDescription(
        node: AccessibilityNodeInfo,
        contentDescription: String,
        foundNodes: MutableList<AccessibilityNodeInfo>,
        ignoreCase: Boolean
    ) {
        val nodeContentDescription = node.contentDescription?.toString() ?: ""
        
        if (nodeContentDescription.contains(contentDescription, ignoreCase)) {
            foundNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByContentDescription(child, contentDescription, foundNodes, ignoreCase)
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
    
    private fun searchEditableNodes(
        node: AccessibilityNodeInfo,
        editableNodes: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.isEditable) {
            editableNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchEditableNodes(child, editableNodes)
            }
        }
    }
    
    private fun searchNodesByCriteria(
        node: AccessibilityNodeInfo,
        criteria: FindElementsRequest,
        foundNodes: MutableList<AccessibilityNodeInfo>
    ) {
        var matches = true
        
        // Check text
        if (criteria.text != null) {
            val nodeText = node.text?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""
            if (!nodeText.contains(criteria.text, ignoreCase = true) && 
                !contentDesc.contains(criteria.text, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check className
        if (criteria.className != null) {
            val nodeClassName = node.className?.toString() ?: ""
            if (!nodeClassName.contains(criteria.className, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check packageName
        if (criteria.packageName != null) {
            val nodePackageName = node.packageName?.toString() ?: ""
            if (!nodePackageName.contains(criteria.packageName, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check contentDescription
        if (criteria.contentDescription != null) {
            val nodeContentDescription = node.contentDescription?.toString() ?: ""
            if (!nodeContentDescription.contains(criteria.contentDescription, ignoreCase = true)) {
                matches = false
            }
        }
        
        // Check clickable
        if (criteria.clickable != null && node.isClickable != criteria.clickable) {
            matches = false
        }
        
        // Check scrollable
        if (criteria.scrollable != null && node.isScrollable != criteria.scrollable) {
            matches = false
        }
        
        if (matches) {
            foundNodes.add(node)
        }
        
        // Search children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByCriteria(child, criteria, foundNodes)
            }
        }
    }
}
