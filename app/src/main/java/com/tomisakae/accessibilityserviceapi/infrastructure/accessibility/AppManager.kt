package com.tomisakae.accessibilityserviceapi.infrastructure.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Manager for app-related operations
 */
class AppManager(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "AppManager"
    }
    
    private val uiTreeManager = UiTreeManager(service)
    
    /**
     * Click on app by name or package
     */
    fun clickApp(request: ClickAppRequest): ClickAppResponse {
        return try {
            val rootNode = service.rootInActiveWindow
            if (rootNode == null) {
                return ClickAppResponse(false, false, null, null)
            }
            
            // Search by app name or search text
            val searchText = request.appName ?: request.searchText
            val foundNodes = if (searchText != null) {
                uiTreeManager.findNodesByText(rootNode, searchText)
            } else {
                emptyList()
            }
            
            // Try to find by package name if provided
            val packageNodes = if (request.packageName != null) {
                findNodesByPackage(rootNode, request.packageName)
            } else {
                emptyList()
            }
            
            // Combine results and try to click
            val allNodes = (foundNodes + packageNodes).distinctBy { it.hashCode() }
            
            for (node in allNodes) {
                if (node.isClickable) {
                    val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (success) {
                        return ClickAppResponse(
                            success = true,
                            appFound = true,
                            appName = node.text?.toString() ?: node.contentDescription?.toString(),
                            packageName = node.packageName?.toString()
                        )
                    }
                }
            }
            
            ClickAppResponse(false, allNodes.isNotEmpty(), null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error clicking app", e)
            ClickAppResponse(false, false, null, null)
        }
    }
    
    /**
     * Launch app by package name
     */
    fun launchApp(request: LaunchAppRequest): ActionResponse {
        return try {
            val packageManager = service.packageManager
            val intent = if (request.activityName != null) {
                Intent().apply {
                    setClassName(request.packageName, request.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                packageManager.getLaunchIntentForPackage(request.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            
            if (intent != null) {
                service.startActivity(intent)
                ActionResponse(true, "App launched successfully")
            } else {
                ActionResponse(false, "Could not find launch intent for package: ${request.packageName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app", e)
            ActionResponse(false, "Error launching app: ${e.message}")
        }
    }
    
    /**
     * Close app
     */
    fun closeApp(request: CloseAppRequest): ActionResponse {
        return try {
            val packageToClose = request.packageName ?: service.rootInActiveWindow?.packageName?.toString()
            
            if (packageToClose == null) {
                return ActionResponse(false, "No package name provided and no current app found")
            }
            
            val success = if (request.forceKill) {
                // Force kill using shell command
                val process = Runtime.getRuntime().exec("am force-stop $packageToClose")
                process.waitFor() == 0
            } else {
                // Try to close gracefully using back button multiple times
                var closed = false
                repeat(5) {
                    if (service.rootInActiveWindow?.packageName?.toString() == packageToClose) {
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                        Thread.sleep(500)
                    } else {
                        closed = true
                        return@repeat
                    }
                }
                closed
            }
            
            ActionResponse(success, if (success) "App closed successfully" else "Failed to close app")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing app", e)
            ActionResponse(false, "Error closing app: ${e.message}")
        }
    }
    
    /**
     * Get recent apps
     */
    fun getRecentApps(): RecentAppsResponse {
        return try {
            // First open recent apps
            val recentOpened = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            if (!recentOpened) {
                return RecentAppsResponse(false, emptyList(), 0, "Failed to open recent apps")
            }
            
            // Wait a bit for the UI to load
            Thread.sleep(1000)
            
            val rootNode = service.rootInActiveWindow
            if (rootNode == null) {
                return RecentAppsResponse(false, emptyList(), 0, "No root node found")
            }
            
            val recentApps = findRecentApps(rootNode)
            
            RecentAppsResponse(
                success = true,
                apps = recentApps,
                totalApps = recentApps.size,
                message = "Found ${recentApps.size} recent apps"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent apps", e)
            RecentAppsResponse(false, emptyList(), 0, "Error: ${e.message}")
        }
    }
    
    /**
     * Open app from recent
     */
    fun openFromRecent(request: RecentAppRequest): ActionResponse {
        return try {
            val recentApps = getRecentApps()
            if (!recentApps.success) {
                return ActionResponse(false, "Failed to get recent apps")
            }
            
            val targetApp = when {
                request.position != null -> recentApps.apps.find { it.position == request.position }
                request.packageName != null -> recentApps.apps.find { it.packageName == request.packageName }
                request.appName != null -> recentApps.apps.find { it.appName?.contains(request.appName, true) == true }
                else -> null
            }
            
            if (targetApp == null) {
                return ActionResponse(false, "App not found in recent apps")
            }
            
            // Click on the app
            val rootNode = service.rootInActiveWindow
            if (rootNode != null) {
                val appNode = findNodeAtBounds(rootNode, targetApp.bounds)
                if (appNode?.isClickable == true) {
                    val success = appNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return ActionResponse(success, if (success) "App opened from recent" else "Failed to click app")
                }
            }
            
            ActionResponse(false, "Could not find clickable node for app")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening from recent", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }
    
    /**
     * Close app from recent
     */
    fun closeFromRecent(request: RecentAppRequest): ActionResponse {
        return try {
            val recentApps = getRecentApps()
            if (!recentApps.success) {
                return ActionResponse(false, "Failed to get recent apps")
            }
            
            val targetApp = when {
                request.position != null -> recentApps.apps.find { it.position == request.position }
                request.packageName != null -> recentApps.apps.find { it.packageName == request.packageName }
                request.appName != null -> recentApps.apps.find { it.appName?.contains(request.appName, true) == true }
                else -> null
            }
            
            if (targetApp == null) {
                return ActionResponse(false, "App not found in recent apps")
            }
            
            // Try to find close button or swipe to close
            // This is device-specific and might need adjustment
            ActionResponse(false, "Close from recent not fully implemented")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing from recent", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }
    
    private fun findNodesByPackage(
        node: AccessibilityNodeInfo,
        packageName: String
    ): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByPackage(node, packageName, foundNodes)
        return foundNodes
    }
    
    private fun searchNodesByPackage(
        node: AccessibilityNodeInfo,
        packageName: String,
        foundNodes: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.packageName?.toString()?.contains(packageName, ignoreCase = true) == true) {
            foundNodes.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByPackage(child, packageName, foundNodes)
            }
        }
    }
    
    private fun findRecentApps(rootNode: AccessibilityNodeInfo): List<RecentAppInfo> {
        val recentApps = mutableListOf<RecentAppInfo>()
        // This is a simplified implementation
        // Real implementation would need to parse the specific recent apps UI
        return recentApps
    }
    
    private fun findNodeAtBounds(
        node: AccessibilityNodeInfo,
        bounds: NodeBounds
    ): AccessibilityNodeInfo? {
        // Find node at specific bounds
        // This is a simplified implementation
        return null
    }
}
