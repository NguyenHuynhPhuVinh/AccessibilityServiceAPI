package com.tomisakae.accessibilityserviceapi.infrastructure.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.tomisakae.accessibilityserviceapi.infrastructure.http.ApiHttpServer

/**
 * Main accessibility service that coordinates all operations
 */
class AccessibilityServiceManager : AccessibilityService() {
    
    companion object {
        private const val TAG = "AccessibilityServiceManager"
        private var instance: AccessibilityServiceManager? = null
        private var httpServer: ApiHttpServer? = null
        private var serviceStartTime: Long = 0
        
        fun getInstance(): AccessibilityServiceManager? = instance
        
        fun isServerRunning(): Boolean = httpServer?.isAlive == true
        
        fun getUptime(): Long = if (serviceStartTime > 0) {
            System.currentTimeMillis() - serviceStartTime
        } else 0
        
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            
            for (service in enabledServices) {
                if (service.resolveInfo.serviceInfo.packageName == context.packageName) {
                    return true
                }
            }
            return false
        }
    }
    
    private lateinit var gestureManager: GestureManager
    private lateinit var uiTreeManager: UiTreeManager
    private lateinit var navigationManager: NavigationManager
    private lateinit var appManager: AppManager
    private lateinit var systemManager: SystemManager
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceStartTime = System.currentTimeMillis()
        
        Log.i(TAG, "Accessibility Service Connected")
        
        // Initialize managers
        initializeManagers()
        
        // Start HTTP server
        startHttpServer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopHttpServer()
        instance = null
        serviceStartTime = 0
        Log.i(TAG, "Accessibility Service Destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
        event?.let {
            Log.v(TAG, "Accessibility Event: ${it.eventType}")
        }
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted")
    }
    
    private fun initializeManagers() {
        gestureManager = GestureManager(this)
        uiTreeManager = UiTreeManager(this)
        navigationManager = NavigationManager(this)
        appManager = AppManager(this)
        systemManager = SystemManager(this)
    }
    
    private fun startHttpServer() {
        try {
            httpServer = ApiHttpServer("0.0.0.0", 8080, this)
            httpServer?.start()
            Log.i(TAG, "HTTP Server started on 0.0.0.0:8080")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start HTTP server", e)
        }
    }
    
    private fun stopHttpServer() {
        httpServer?.stop()
        httpServer = null
        Log.i(TAG, "HTTP Server stopped")
    }
    
    // Getters for managers
    fun getGestureManager(): GestureManager = gestureManager
    fun getUiTreeManager(): UiTreeManager = uiTreeManager
    fun getNavigationManager(): NavigationManager = navigationManager
    fun getAppManager(): AppManager = appManager
    fun getSystemManager(): SystemManager = systemManager
}
