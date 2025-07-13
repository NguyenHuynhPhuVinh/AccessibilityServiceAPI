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
 * Swipe action request
 */
data class SwipeRequest(
    val direction: SwipeDirection,
    val startX: Int? = null,
    val startY: Int? = null,
    val endX: Int? = null,
    val endY: Int? = null,
    val duration: Long = 300L
)

/**
 * Swipe direction enum
 */
enum class SwipeDirection {
    LEFT, RIGHT, UP, DOWN
}

/**
 * Swipe action response
 */
data class SwipeResponse(
    val swiped: Boolean,
    val direction: SwipeDirection,
    val startPoint: Pair<Int, Int>?,
    val endPoint: Pair<Int, Int>?
)

/**
 * Navigation button response
 */
data class NavigationResponse(
    val success: Boolean,
    val action: String
)

/**
 * Click app request
 */
data class ClickAppRequest(
    val appName: String? = null,
    val packageName: String? = null,
    val searchText: String? = null
)

/**
 * Click app response
 */
data class ClickAppResponse(
    val success: Boolean,
    val appFound: Boolean,
    val appName: String?,
    val packageName: String?
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
 * Long click request
 */
data class LongClickRequest(
    val x: Int? = null,
    val y: Int? = null,
    val nodeId: String? = null,
    val duration: Long = 1000L
)

/**
 * Double click request
 */
data class DoubleClickRequest(
    val x: Int,
    val y: Int,
    val delay: Long = 100L
)

/**
 * Pinch gesture request
 */
data class PinchRequest(
    val centerX: Int,
    val centerY: Int,
    val startRadius: Int = 100,
    val endRadius: Int = 300,
    val duration: Long = 500L,
    val zoomIn: Boolean = true
)

/**
 * Rotate gesture request
 */
data class RotateRequest(
    val centerX: Int,
    val centerY: Int,
    val radius: Int = 200,
    val startAngle: Float = 0f,
    val endAngle: Float = 90f,
    val duration: Long = 500L
)

/**
 * Device info response
 */
data class DeviceInfoResponse(
    val screenWidth: Int,
    val screenHeight: Int,
    val density: Float,
    val orientation: String,
    val androidVersion: String,
    val apiLevel: Int,
    val manufacturer: String,
    val model: String,
    val currentApp: String?
)

/**
 * Volume control request
 */
data class VolumeRequest(
    val type: VolumeType,
    val action: VolumeAction,
    val level: Int? = null
)

enum class VolumeType {
    MEDIA, RING, ALARM, NOTIFICATION, SYSTEM
}

enum class VolumeAction {
    UP, DOWN, SET, MUTE, UNMUTE
}

/**
 * System setting request
 */
data class SystemSettingRequest(
    val enabled: Boolean
)

/**
 * App launch request
 */
data class LaunchAppRequest(
    val packageName: String,
    val activityName: String? = null
)

/**
 * Close app request
 */
data class CloseAppRequest(
    val packageName: String? = null,
    val forceKill: Boolean = false
)

/**
 * Recent app info
 */
data class RecentAppInfo(
    val appName: String?,
    val packageName: String?,
    val position: Int,
    val bounds: NodeBounds,
    val isClickable: Boolean
)

/**
 * Recent apps response
 */
data class RecentAppsResponse(
    val success: Boolean,
    val apps: List<RecentAppInfo>,
    val totalApps: Int,
    val message: String? = null
)

/**
 * Recent app action request
 */
data class RecentAppRequest(
    val packageName: String? = null,
    val appName: String? = null,
    val position: Int? = null
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



/**
 * Generic action response
 */
data class ActionResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)

/**
 * Error response for API errors
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: String? = null
)
