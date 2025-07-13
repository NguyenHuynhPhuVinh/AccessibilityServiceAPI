package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import com.tomisakae.accessibilityserviceapi.domain.models.FindElementsRequest
import com.tomisakae.accessibilityserviceapi.domain.models.WaitForElementRequest
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import fi.iki.elonen.NanoHTTPD

/**
 * Controller for UI-related endpoints
 */
class UiController(
    private val serviceManager: AccessibilityServiceManager
) : BaseController() {
    
    /**
     * GET /ui-tree
     */
    fun getUiTree(): NanoHTTPD.Response {
        val uiTreeData = serviceManager.getUiTreeManager().getCurrentUiTree()
        return createSuccessResponse(uiTreeData)
    }
    
    /**
     * POST /find-elements
     */
    fun findElements(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<FindElementsRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        val result = serviceManager.getUiTreeManager().findElements(request)
        return createSuccessResponse(result)
    }
    
    /**
     * POST /wait-for-element
     */
    fun waitForElement(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<WaitForElementRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        // This would need to be implemented as a suspending function
        // For now, return a simple response
        return createErrorResponse(501, "NOT_IMPLEMENTED", "Wait for element not yet implemented")
    }
}
