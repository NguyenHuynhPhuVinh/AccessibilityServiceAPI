package com.tomisakae.accessibilityserviceapi.domain.repositories

import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Repository interface for system operations
 */
interface SystemRepository {
    
    /**
     * Get health status
     */
    suspend fun getHealthStatus(): HealthResponse
    
    /**
     * Get device information
     */
    suspend fun getDeviceInfo(): DeviceInfoResponse
    
    /**
     * Perform navigation actions
     */
    suspend fun performHome(): NavigationResponse
    suspend fun performBack(): NavigationResponse
    suspend fun performRecent(): NavigationResponse
    
    /**
     * Open system panels
     */
    suspend fun openNotifications(): ActionResponse
    suspend fun openQuickSettings(): ActionResponse
    
    /**
     * Control volume
     */
    suspend fun controlVolume(request: VolumeRequest): ActionResponse

    /**
     * Take screenshot
     */
    suspend fun takeScreenshot(): ActionResponse
}
