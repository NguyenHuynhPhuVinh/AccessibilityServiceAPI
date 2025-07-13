package com.tomisakae.accessibilityserviceapi.infrastructure.utils

import android.graphics.Path
import com.tomisakae.accessibilityserviceapi.domain.models.SwipeDirection
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility class for gesture calculations and path generation
 */
object GestureUtils {
    
    /**
     * Calculate swipe coordinates based on direction and screen dimensions
     */
    fun calculateSwipeCoordinates(
        direction: SwipeDirection,
        screenWidth: Int,
        screenHeight: Int,
        startX: Int? = null,
        startY: Int? = null,
        endX: Int? = null,
        endY: Int? = null
    ): SwipeCoordinates {
        
        return if (startX != null && startY != null && endX != null && endY != null) {
            SwipeCoordinates(startX, startY, endX, endY)
        } else {
            when (direction) {
                SwipeDirection.LEFT -> SwipeCoordinates(
                    startX = (screenWidth * 0.8).toInt(),
                    startY = screenHeight / 2,
                    endX = (screenWidth * 0.2).toInt(),
                    endY = screenHeight / 2
                )
                SwipeDirection.RIGHT -> SwipeCoordinates(
                    startX = (screenWidth * 0.2).toInt(),
                    startY = screenHeight / 2,
                    endX = (screenWidth * 0.8).toInt(),
                    endY = screenHeight / 2
                )
                SwipeDirection.UP -> SwipeCoordinates(
                    startX = screenWidth / 2,
                    startY = (screenHeight * 0.8).toInt(),
                    endX = screenWidth / 2,
                    endY = (screenHeight * 0.2).toInt()
                )
                SwipeDirection.DOWN -> SwipeCoordinates(
                    startX = screenWidth / 2,
                    startY = (screenHeight * 0.2).toInt(),
                    endX = screenWidth / 2,
                    endY = (screenHeight * 0.8).toInt()
                )
            }
        }
    }
    
    /**
     * Create a simple path for click gesture
     */
    fun createClickPath(x: Int, y: Int): Path {
        return Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }
    }
    
    /**
     * Create a path for swipe gesture
     */
    fun createSwipePath(startX: Int, startY: Int, endX: Int, endY: Int): Path {
        return Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }
    }
    
    /**
     * Create a curved path for more natural swipe
     */
    fun createCurvedSwipePath(
        startX: Int, 
        startY: Int, 
        endX: Int, 
        endY: Int,
        curvature: Float = 0.1f
    ): Path {
        return Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            
            // Calculate control point for curve
            val midX = (startX + endX) / 2f
            val midY = (startY + endY) / 2f
            val controlX = midX + (endY - startY) * curvature
            val controlY = midY - (endX - startX) * curvature
            
            quadTo(controlX, controlY, endX.toFloat(), endY.toFloat())
        }
    }
    
    /**
     * Create paths for pinch gesture
     */
    fun createPinchPaths(
        centerX: Int,
        centerY: Int,
        startRadius: Int,
        endRadius: Int,
        duration: Long
    ): Pair<Path, Path> {
        val path1 = Path()
        val path2 = Path()
        
        // First finger path (top)
        val startX1 = centerX
        val startY1 = centerY - startRadius
        val endX1 = centerX
        val endY1 = centerY - endRadius
        
        path1.moveTo(startX1.toFloat(), startY1.toFloat())
        path1.lineTo(endX1.toFloat(), endY1.toFloat())
        
        // Second finger path (bottom)
        val startX2 = centerX
        val startY2 = centerY + startRadius
        val endX2 = centerX
        val endY2 = centerY + endRadius
        
        path2.moveTo(startX2.toFloat(), startY2.toFloat())
        path2.lineTo(endX2.toFloat(), endY2.toFloat())
        
        return Pair(path1, path2)
    }
    
    /**
     * Create paths for rotation gesture
     */
    fun createRotationPaths(
        centerX: Int,
        centerY: Int,
        radius: Int,
        startAngle: Float,
        endAngle: Float,
        steps: Int = 20
    ): Pair<Path, Path> {
        val path1 = Path()
        val path2 = Path()
        
        val angleStep = (endAngle - startAngle) / steps
        
        // First finger path
        val startX1 = centerX + (radius * cos(Math.toRadians(startAngle.toDouble()))).toInt()
        val startY1 = centerY + (radius * sin(Math.toRadians(startAngle.toDouble()))).toInt()
        path1.moveTo(startX1.toFloat(), startY1.toFloat())
        
        for (i in 1..steps) {
            val angle = startAngle + angleStep * i
            val x = centerX + (radius * cos(Math.toRadians(angle.toDouble()))).toInt()
            val y = centerY + (radius * sin(Math.toRadians(angle.toDouble()))).toInt()
            path1.lineTo(x.toFloat(), y.toFloat())
        }
        
        // Second finger path (opposite side)
        val oppositeStartAngle = startAngle + 180f
        val oppositeEndAngle = endAngle + 180f
        val startX2 = centerX + (radius * cos(Math.toRadians(oppositeStartAngle.toDouble()))).toInt()
        val startY2 = centerY + (radius * sin(Math.toRadians(oppositeStartAngle.toDouble()))).toInt()
        path2.moveTo(startX2.toFloat(), startY2.toFloat())
        
        for (i in 1..steps) {
            val angle = oppositeStartAngle + angleStep * i
            val x = centerX + (radius * cos(Math.toRadians(angle.toDouble()))).toInt()
            val y = centerY + (radius * sin(Math.toRadians(angle.toDouble()))).toInt()
            path2.lineTo(x.toFloat(), y.toFloat())
        }
        
        return Pair(path1, path2)
    }
    
    /**
     * Validate coordinates are within screen bounds
     */
    fun validateCoordinates(
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int
    ): Boolean {
        return x >= 0 && x <= screenWidth && y >= 0 && y <= screenHeight
    }
    
    /**
     * Clamp coordinates to screen bounds
     */
    fun clampCoordinates(
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int
    ): Pair<Int, Int> {
        val clampedX = x.coerceIn(0, screenWidth)
        val clampedY = y.coerceIn(0, screenHeight)
        return Pair(clampedX, clampedY)
    }
    
    /**
     * Calculate distance between two points
     */
    fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
        val dx = (x2 - x1).toDouble()
        val dy = (y2 - y1).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Data class for swipe coordinates
     */
    data class SwipeCoordinates(
        val startX: Int,
        val startY: Int,
        val endX: Int,
        val endY: Int
    )
}
