package com.tomisakae.accessibilityserviceapi.domain.models

import android.graphics.Rect

/**
 * UI Node representation
 */
data class UiNode(
    val id: String?,
    val className: String?,
    val packageName: String?,
    val text: String?,
    val contentDescription: String?,
    val bounds: NodeBounds,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isCheckable: Boolean,
    val isChecked: Boolean,
    val isEnabled: Boolean,
    val isFocusable: Boolean,
    val isFocused: Boolean,
    val isSelected: Boolean,
    val isPassword: Boolean,
    val children: List<UiNode> = emptyList()
)

/**
 * Node bounds information
 */
data class NodeBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val width: Int = right - left,
    val height: Int = bottom - top,
    val centerX: Int = left + width / 2,
    val centerY: Int = top + height / 2
) {
    companion object {
        fun fromRect(rect: Rect): NodeBounds {
            return NodeBounds(rect.left, rect.top, rect.right, rect.bottom)
        }
    }
}

/**
 * UI Tree response
 */
data class UiTreeResponse(
    val rootNode: UiNode?,
    val totalNodes: Int,
    val captureTime: Long = System.currentTimeMillis()
)

/**
 * Find elements request
 */
data class FindElementsRequest(
    val text: String? = null,
    val className: String? = null,
    val packageName: String? = null,
    val contentDescription: String? = null,
    val resourceId: String? = null,
    val clickable: Boolean? = null,
    val scrollable: Boolean? = null
)

/**
 * Find elements response
 */
data class FindElementsResponse(
    val elements: List<UiNode>,
    val count: Int
)

/**
 * Wait for element request
 */
data class WaitForElementRequest(
    val text: String? = null,
    val className: String? = null,
    val nodeId: String? = null,
    val timeout: Long = 10000L
)
