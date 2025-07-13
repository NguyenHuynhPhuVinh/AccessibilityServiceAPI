package com.tomisakae.accessibilityserviceapi.infrastructure.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Manager for system-level operations
 */
class SystemManager(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "SystemManager"
    }
    
    /**
     * Get health status
     */
    fun getHealthStatus(): HealthResponse {
        return HealthResponse(
            status = "OK",
            accessibilityServiceEnabled = true,
            uptime = AccessibilityServiceManager.getUptime()
        )
    }
    
    /**
     * Get device information
     */
    fun getDeviceInfo(): DeviceInfoResponse {
        val displayMetrics = service.resources.displayMetrics
        val currentApp = service.rootInActiveWindow?.packageName?.toString()
        
        return DeviceInfoResponse(
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels,
            density = displayMetrics.density,
            orientation = getOrientationString(),
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            currentApp = currentApp
        )
    }
    
    /**
     * Control volume
     */
    fun controlVolume(request: VolumeRequest): ActionResponse {
        return try {
            val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamType = when (request.type) {
                VolumeType.MEDIA -> AudioManager.STREAM_MUSIC
                VolumeType.RING -> AudioManager.STREAM_RING
                VolumeType.ALARM -> AudioManager.STREAM_ALARM
                VolumeType.NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
                VolumeType.SYSTEM -> AudioManager.STREAM_SYSTEM
            }
            
            when (request.action) {
                VolumeAction.UP -> {
                    audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, 0)
                    ActionResponse(true, "Volume increased")
                }
                VolumeAction.DOWN -> {
                    audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, 0)
                    ActionResponse(true, "Volume decreased")
                }
                VolumeAction.SET -> {
                    if (request.level != null) {
                        audioManager.setStreamVolume(streamType, request.level, 0)
                        ActionResponse(true, "Volume set to ${request.level}")
                    } else {
                        ActionResponse(false, "Volume level not specified")
                    }
                }
                VolumeAction.MUTE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_MUTE, 0)
                        ActionResponse(true, "Volume muted")
                    } else {
                        ActionResponse(false, "Mute not supported on this Android version")
                    }
                }
                VolumeAction.UNMUTE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_UNMUTE, 0)
                        ActionResponse(true, "Volume unmuted")
                    } else {
                        ActionResponse(false, "Unmute not supported on this Android version")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling volume", e)
            ActionResponse(false, "Error controlling volume: ${e.message}")
        }
    }
    

    
    private fun getOrientationString(): String {
        return when (service.resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> "PORTRAIT"
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> "LANDSCAPE"
            else -> "UNDEFINED"
        }
    }
}
