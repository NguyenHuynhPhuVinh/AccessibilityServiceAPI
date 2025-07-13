package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import fi.iki.elonen.NanoHTTPD

/**
 * Controller for health check endpoints
 */
class HealthController(
    private val serviceManager: AccessibilityServiceManager
) : BaseController() {
    
    /**
     * GET /health
     */
    fun getHealth(): NanoHTTPD.Response {
        val healthData = serviceManager.getSystemManager().getHealthStatus()
        return createSuccessResponse(healthData)
    }
}
