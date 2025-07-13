package com.tomisakae.accessibilityserviceapi.domain.models

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
 * Navigation button response
 */
data class NavigationResponse(
    val success: Boolean,
    val action: String
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


