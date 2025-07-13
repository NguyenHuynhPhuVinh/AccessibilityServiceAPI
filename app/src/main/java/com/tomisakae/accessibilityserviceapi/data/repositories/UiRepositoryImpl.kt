package com.tomisakae.accessibilityserviceapi.data.repositories

import android.content.Context
import com.tomisakae.accessibilityserviceapi.domain.models.*
import com.tomisakae.accessibilityserviceapi.domain.repositories.UiRepository
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager

/**
 * Implementation of UiRepository
 */
class UiRepositoryImpl(
    private val serviceManager: AccessibilityServiceManager
) : UiRepository {
    
    override suspend fun getCurrentUiTree(): UiTreeResponse {
        return serviceManager.getUiTreeManager().getCurrentUiTree()
    }
    
    override suspend fun findElements(request: FindElementsRequest): FindElementsResponse {
        return serviceManager.getUiTreeManager().findElements(request)
    }
    
    override suspend fun waitForElement(request: WaitForElementRequest): ActionResponse {
        // Implementation for waiting for element to appear
        val startTime = System.currentTimeMillis()
        val timeout = request.timeout
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val findRequest = FindElementsRequest(
                text = request.text,
                className = request.className
            )
            
            val result = findElements(findRequest)
            if (result.count > 0) {
                return ActionResponse(true, "Element found")
            }
            
            // Wait a bit before checking again
            kotlinx.coroutines.delay(500)
        }
        
        return ActionResponse(false, "Element not found within timeout")
    }
    
    override fun isAccessibilityServiceEnabled(): Boolean {
        return AccessibilityServiceManager.isAccessibilityServiceEnabled(serviceManager as Context)
    }
}
