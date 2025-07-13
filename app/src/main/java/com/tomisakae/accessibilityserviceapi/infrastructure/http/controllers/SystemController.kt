package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import com.tomisakae.accessibilityserviceapi.domain.models.VolumeRequest
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import fi.iki.elonen.NanoHTTPD

/**
 * Controller for system-related endpoints
 */
class SystemController(
    private val serviceManager: AccessibilityServiceManager
) : BaseController() {
    
    /**
     * POST /home
     */
    fun performHome(): NanoHTTPD.Response {
        val result = serviceManager.getNavigationManager().performHome()
        return createSuccessResponse(result)
    }
    
    /**
     * POST /back
     */
    fun performBack(): NanoHTTPD.Response {
        val result = serviceManager.getNavigationManager().performBack()
        return createSuccessResponse(result)
    }
    
    /**
     * POST /recent
     */
    fun performRecent(): NanoHTTPD.Response {
        val result = serviceManager.getNavigationManager().performRecent()
        return createSuccessResponse(result)
    }
    
    /**
     * GET /device-info
     */
    fun getDeviceInfo(): NanoHTTPD.Response {
        val deviceInfo = serviceManager.getSystemManager().getDeviceInfo()
        return createSuccessResponse(deviceInfo)
    }
    
    /**
     * GET /screenshot
     */
    fun takeScreenshot(): NanoHTTPD.Response {
        val result = serviceManager.getNavigationManager().takeScreenshot()
        return createSuccessResponse(result)
    }
    

    
    /**
     * POST /open-notifications
     */
    fun openNotifications(): NanoHTTPD.Response {
        val result = serviceManager.getNavigationManager().openNotifications()
        return createSuccessResponse(result)
    }
    
    /**
     * POST /open-quick-settings
     */
    fun openQuickSettings(): NanoHTTPD.Response {
        val result = serviceManager.getNavigationManager().openQuickSettings()
        return createSuccessResponse(result)
    }
    
    /**
     * POST /volume
     */
    fun controlVolume(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<VolumeRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getSystemManager().controlVolume(request)
        return createSuccessResponse(result)
    }
    

}
