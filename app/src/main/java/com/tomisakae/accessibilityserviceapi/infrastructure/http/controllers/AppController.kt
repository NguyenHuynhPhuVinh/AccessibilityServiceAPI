package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import com.tomisakae.accessibilityserviceapi.domain.models.ClickAppRequest
import com.tomisakae.accessibilityserviceapi.domain.models.CloseAppRequest
import com.tomisakae.accessibilityserviceapi.domain.models.LaunchAppRequest
import com.tomisakae.accessibilityserviceapi.domain.models.RecentAppRequest
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import fi.iki.elonen.NanoHTTPD

/**
 * Controller for app-related endpoints
 */
class AppController(
    private val serviceManager: AccessibilityServiceManager
) : BaseController() {
    
    /**
     * POST /click-app
     */
    fun clickApp(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<ClickAppRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getAppManager().clickApp(request)
        return createSuccessResponse(result)
    }
    
    /**
     * POST /launch-app
     */
    fun launchApp(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<LaunchAppRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getAppManager().launchApp(request)
        return createSuccessResponse(result)
    }
    
    /**
     * POST /close-app
     */
    fun closeApp(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<CloseAppRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getAppManager().closeApp(request)
        return createSuccessResponse(result)
    }
    
    /**
     * GET /recent-apps
     */
    fun getRecentApps(): NanoHTTPD.Response {
        val result = serviceManager.getAppManager().getRecentApps()
        return createSuccessResponse(result)
    }
    
    /**
     * POST /recent-open
     */
    fun openFromRecent(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<RecentAppRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getAppManager().openFromRecent(request)
        return createSuccessResponse(result)
    }
    
    /**
     * POST /recent-close
     */
    fun closeFromRecent(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<RecentAppRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getAppManager().closeFromRecent(request)
        return createSuccessResponse(result)
    }
}
