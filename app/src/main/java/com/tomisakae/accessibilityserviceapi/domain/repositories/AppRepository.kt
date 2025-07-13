package com.tomisakae.accessibilityserviceapi.domain.repositories

import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Repository interface for app operations
 */
interface AppRepository {
    
    /**
     * Click on app by name or package
     */
    suspend fun clickApp(request: ClickAppRequest): ClickAppResponse
    
    /**
     * Launch app
     */
    suspend fun launchApp(request: LaunchAppRequest): ActionResponse
    
    /**
     * Close app
     */
    suspend fun closeApp(request: CloseAppRequest): ActionResponse
    
    /**
     * Get recent apps
     */
    suspend fun getRecentApps(): RecentAppsResponse
    
    /**
     * Open app from recent
     */
    suspend fun openFromRecent(request: RecentAppRequest): ActionResponse
    
    /**
     * Close app from recent
     */
    suspend fun closeFromRecent(request: RecentAppRequest): ActionResponse
}
