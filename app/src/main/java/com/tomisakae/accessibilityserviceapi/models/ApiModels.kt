package com.tomisakae.accessibilityserviceapi.models

import android.graphics.Rect

/**
 * Base response class for all API responses
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Health check response
 */
data class HealthResponse(
    val status: String,
    val serverVersion: String = "1.0.0",
    val accessibilityServiceEnabled: Boolean,
    val uptime: Long
)

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
 * Click action request
 */
data class ClickRequest(
    val x: Int,
    val y: Int,
    val nodeId: String? = null
)

/**
 * Click action response
 */
data class ClickResponse(
    val clicked: Boolean,
    val coordinates: Pair<Int, Int>?,
    val nodeFound: Boolean = true
)

/**
 * Scroll action request
 */
data class ScrollRequest(
    val direction: ScrollDirection,
    val nodeId: String? = null,
    val amount: Int = 1
)

/**
 * Scroll direction enum
 */
enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

/**
 * Scroll action response
 */
data class ScrollResponse(
    val scrolled: Boolean,
    val direction: ScrollDirection,
    val nodeFound: Boolean = true
)

/**
 * Input text request
 */
data class InputTextRequest(
    val text: String,
    val nodeId: String? = null,
    val clearFirst: Boolean = true
)

/**
 * Input text response
 */
data class InputTextResponse(
    val inputSuccess: Boolean,
    val text: String,
    val nodeFound: Boolean = true
)

/**
 * Error response for API errors
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: String? = null
)
