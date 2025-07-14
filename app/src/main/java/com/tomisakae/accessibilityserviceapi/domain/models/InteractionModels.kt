package com.tomisakae.accessibilityserviceapi.domain.models

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
 * Keyboard action request
 */
data class KeyboardActionRequest(
    val action: String // "ENTER", "BACK", "HOME", "RECENT", "SEARCH", "SEND", "GO", "DONE"
)

/**
 * Keyboard action response
 */
data class KeyboardActionResponse(
    val success: Boolean,
    val action: String,
    val message: String
)


