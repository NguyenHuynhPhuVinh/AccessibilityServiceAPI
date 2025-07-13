package com.tomisakae.accessibilityserviceapi.infrastructure.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import com.tomisakae.accessibilityserviceapi.domain.models.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Manager for gesture-based interactions
 */
class GestureManager(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "GestureManager"
    }
    
    /**
     * Perform click gesture at coordinates
     */
    suspend fun performClick(x: Int, y: Int): Boolean {
        return performGestureClick(x, y)
    }
    
    /**
     * Perform long click gesture
     */
    suspend fun performLongClick(x: Int, y: Int, duration: Long): Boolean {
        return performGestureLongClick(x, y, duration)
    }
    
    /**
     * Perform double click gesture
     */
    suspend fun performDoubleClick(x: Int, y: Int, delay: Long): Boolean {
        val firstClick = performGestureClick(x, y)
        if (!firstClick) return false
        
        Thread.sleep(delay)
        return performGestureClick(x, y)
    }
    
    /**
     * Perform swipe gesture
     */
    suspend fun performSwipe(request: SwipeRequest): SwipeResponse {
        val screenWidth = service.resources.displayMetrics.widthPixels
        val screenHeight = service.resources.displayMetrics.heightPixels
        
        val (startX, startY, endX, endY) = calculateSwipeCoordinates(
            request, screenWidth, screenHeight
        )
        
        val success = performGestureSwipe(startX, startY, endX, endY, request.duration)
        
        return SwipeResponse(
            swiped = success,
            direction = request.direction,
            startPoint = Pair(startX, startY),
            endPoint = Pair(endX, endY)
        )
    }
    

    
    private suspend fun performGestureClick(x: Int, y: Int): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }
            
            val success = service.dispatchGesture(gesture, callback, null)
            if (!success) {
                continuation.resume(false)
            }
        }
    }
    
    private suspend fun performGestureLongClick(x: Int, y: Int, duration: Long): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }
            
            val success = service.dispatchGesture(gesture, callback, null)
            if (!success) {
                continuation.resume(false)
            }
        }
    }
    
    private suspend fun performGestureSwipe(
        startX: Int, startY: Int, endX: Int, endY: Int, duration: Long
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(startX.toFloat(), startY.toFloat())
                lineTo(endX.toFloat(), endY.toFloat())
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }
            
            val success = service.dispatchGesture(gesture, callback, null)
            if (!success) {
                continuation.resume(false)
            }
        }
    }
    
    private fun calculateSwipeCoordinates(
        request: SwipeRequest, screenWidth: Int, screenHeight: Int
    ): Array<Int> {
        return if (request.startX != null && request.startY != null && 
                   request.endX != null && request.endY != null) {
            arrayOf(request.startX, request.startY, request.endX, request.endY)
        } else {
            when (request.direction) {
                SwipeDirection.LEFT -> arrayOf(
                    (screenWidth * 0.8).toInt(), screenHeight / 2,
                    (screenWidth * 0.2).toInt(), screenHeight / 2
                )
                SwipeDirection.RIGHT -> arrayOf(
                    (screenWidth * 0.2).toInt(), screenHeight / 2,
                    (screenWidth * 0.8).toInt(), screenHeight / 2
                )
                SwipeDirection.UP -> arrayOf(
                    screenWidth / 2, (screenHeight * 0.8).toInt(),
                    screenWidth / 2, (screenHeight * 0.2).toInt()
                )
                SwipeDirection.DOWN -> arrayOf(
                    screenWidth / 2, (screenHeight * 0.2).toInt(),
                    screenWidth / 2, (screenHeight * 0.8).toInt()
                )
            }
        }
    }
    

}
