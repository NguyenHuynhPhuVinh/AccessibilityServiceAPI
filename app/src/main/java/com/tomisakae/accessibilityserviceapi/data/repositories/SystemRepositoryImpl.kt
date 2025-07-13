package com.tomisakae.accessibilityserviceapi.data.repositories

import com.tomisakae.accessibilityserviceapi.domain.models.*
import com.tomisakae.accessibilityserviceapi.domain.repositories.SystemRepository
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager

/**
 * Implementation of SystemRepository
 */
class SystemRepositoryImpl(
    private val serviceManager: AccessibilityServiceManager
) : SystemRepository {
    
    override suspend fun getHealthStatus(): HealthResponse {
        return serviceManager.getSystemManager().getHealthStatus()
    }
    
    override suspend fun getDeviceInfo(): DeviceInfoResponse {
        return serviceManager.getSystemManager().getDeviceInfo()
    }
    
    override suspend fun performHome(): NavigationResponse {
        return serviceManager.getNavigationManager().performHome()
    }
    
    override suspend fun performBack(): NavigationResponse {
        return serviceManager.getNavigationManager().performBack()
    }
    
    override suspend fun performRecent(): NavigationResponse {
        return serviceManager.getNavigationManager().performRecent()
    }
    
    override suspend fun openNotifications(): ActionResponse {
        return serviceManager.getNavigationManager().openNotifications()
    }
    
    override suspend fun openQuickSettings(): ActionResponse {
        return serviceManager.getNavigationManager().openQuickSettings()
    }
    
    override suspend fun controlVolume(request: VolumeRequest): ActionResponse {
        return serviceManager.getSystemManager().controlVolume(request)
    }

    override suspend fun takeScreenshot(): ActionResponse {
        return serviceManager.getNavigationManager().takeScreenshot()
    }
}
