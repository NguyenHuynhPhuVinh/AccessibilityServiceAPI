package com.tomisakae.accessibilityserviceapi.infrastructure.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import com.tomisakae.accessibilityserviceapi.domain.models.ActionResponse
import com.tomisakae.accessibilityserviceapi.domain.models.NavigationResponse

/**
 * Manager for navigation operations
 */
class NavigationManager(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "NavigationManager"
    }
    
    /**
     * Perform home action
     */
    fun performHome(): NavigationResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            Log.d(TAG, "Home action performed: $success")
            NavigationResponse(success, "HOME")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing home action", e)
            NavigationResponse(false, "HOME")
        }
    }
    
    /**
     * Perform back action
     */
    fun performBack(): NavigationResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            Log.d(TAG, "Back action performed: $success")
            NavigationResponse(success, "BACK")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing back action", e)
            NavigationResponse(false, "BACK")
        }
    }
    
    /**
     * Perform recent apps action
     */
    fun performRecent(): NavigationResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            Log.d(TAG, "Recent action performed: $success")
            NavigationResponse(success, "RECENT")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing recent action", e)
            NavigationResponse(false, "RECENT")
        }
    }
    
    /**
     * Open notifications panel
     */
    fun openNotifications(): ActionResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            Log.d(TAG, "Notifications opened: $success")
            ActionResponse(success, if (success) "Notifications opened" else "Failed to open notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening notifications", e)
            ActionResponse(false, "Error opening notifications: ${e.message}")
        }
    }
    
    /**
     * Open quick settings panel
     */
    fun openQuickSettings(): ActionResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
            Log.d(TAG, "Quick settings opened: $success")
            ActionResponse(success, if (success) "Quick settings opened" else "Failed to open quick settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening quick settings", e)
            ActionResponse(false, "Error opening quick settings: ${e.message}")
        }
    }
    
    /**
     * Open power dialog
     */
    fun openPowerDialog(): ActionResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
            Log.d(TAG, "Power dialog opened: $success")
            ActionResponse(success, if (success) "Power dialog opened" else "Failed to open power dialog")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening power dialog", e)
            ActionResponse(false, "Error opening power dialog: ${e.message}")
        }
    }
    
    /**
     * Lock screen
     */
    fun lockScreen(): ActionResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            Log.d(TAG, "Screen locked: $success")
            ActionResponse(success, if (success) "Screen locked" else "Failed to lock screen")
        } catch (e: Exception) {
            Log.e(TAG, "Error locking screen", e)
            ActionResponse(false, "Error locking screen: ${e.message}")
        }
    }
    
    /**
     * Take screenshot
     */
    fun takeScreenshot(): ActionResponse {
        return try {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
            Log.d(TAG, "Screenshot taken: $success")
            ActionResponse(success, if (success) "Screenshot taken" else "Failed to take screenshot")
        } catch (e: Exception) {
            Log.e(TAG, "Error taking screenshot", e)
            ActionResponse(false, "Error taking screenshot: ${e.message}")
        }
    }
}
