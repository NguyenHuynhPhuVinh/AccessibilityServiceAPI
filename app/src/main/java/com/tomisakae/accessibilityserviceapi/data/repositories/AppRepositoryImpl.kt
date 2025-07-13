package com.tomisakae.accessibilityserviceapi.data.repositories

import com.tomisakae.accessibilityserviceapi.domain.models.*
import com.tomisakae.accessibilityserviceapi.domain.repositories.AppRepository
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager

/**
 * Implementation of AppRepository
 */
class AppRepositoryImpl(
    private val serviceManager: AccessibilityServiceManager
) : AppRepository {
    
    override suspend fun clickApp(request: ClickAppRequest): ClickAppResponse {
        return serviceManager.getAppManager().clickApp(request)
    }
    
    override suspend fun launchApp(request: LaunchAppRequest): ActionResponse {
        return serviceManager.getAppManager().launchApp(request)
    }
    
    override suspend fun closeApp(request: CloseAppRequest): ActionResponse {
        return serviceManager.getAppManager().closeApp(request)
    }
    
    override suspend fun getRecentApps(): RecentAppsResponse {
        return serviceManager.getAppManager().getRecentApps()
    }
    
    override suspend fun openFromRecent(request: RecentAppRequest): ActionResponse {
        return serviceManager.getAppManager().openFromRecent(request)
    }
    
    override suspend fun closeFromRecent(request: RecentAppRequest): ActionResponse {
        return serviceManager.getAppManager().closeFromRecent(request)
    }
}
