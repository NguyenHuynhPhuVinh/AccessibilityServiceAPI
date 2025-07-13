package com.tomisakae.accessibilityserviceapi.domain.repositories

import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Repository interface for UI operations
 */
interface UiRepository {
    
    /**
     * Get current UI tree
     */
    suspend fun getCurrentUiTree(): UiTreeResponse
    
    /**
     * Find elements by criteria
     */
    suspend fun findElements(request: FindElementsRequest): FindElementsResponse
    
    /**
     * Wait for element to appear
     */
    suspend fun waitForElement(request: WaitForElementRequest): ActionResponse
    
    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(): Boolean
}
