package com.tomisakae.accessibilityserviceapi.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import com.tomisakae.accessibilityserviceapi.models.*
import com.tomisakae.accessibilityserviceapi.utils.UiTreeTraversal
import kotlinx.coroutines.*
import java.io.IOException

class AccessibilityApiService : AccessibilityService() {
    
    companion object {
        private const val TAG = "AccessibilityApiService"
        private var instance: AccessibilityApiService? = null
        private var httpServer: ApiHttpServer? = null
        private var serviceStartTime: Long = 0
        
        fun getInstance(): AccessibilityApiService? = instance
        
        fun isServerRunning(): Boolean = httpServer?.isAlive == true
        
        fun getUptime(): Long = if (serviceStartTime > 0) {
            System.currentTimeMillis() - serviceStartTime
        } else 0
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceStartTime = System.currentTimeMillis()
        
        Log.d(TAG, "Accessibility Service Connected")
        
        // Start HTTP server
        startHttpServer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceStartTime = 0
        
        // Stop HTTP server
        stopHttpServer()
        
        // Cancel all coroutines
        serviceScope.cancel()
        
        // Clear UI tree cache
        UiTreeTraversal.clearCache()
        
        Log.d(TAG, "Accessibility Service Destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
        // For now, we'll just log them
        event?.let {
            Log.v(TAG, "Accessibility Event: ${it.eventType}")
        }
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted")
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
    
    /**
     * Get current UI tree
     */
    fun getCurrentUiTree(): UiTreeResponse {
        val rootNode = rootInActiveWindow
        return UiTreeTraversal.captureUiTree(rootNode)
    }
    
    /**
     * Perform click action
     */
    fun performClick(request: ClickRequest): ClickResponse {
        return try {
            if (request.nodeId != null) {
                // Click on specific node
                val node = UiTreeTraversal.findNodeById(request.nodeId)
                if (node != null) {
                    val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    ClickResponse(success, Pair(request.x, request.y), true)
                } else {
                    ClickResponse(false, null, false)
                }
            } else {
                // Click on coordinates
                val success = performGestureClick(request.x, request.y)
                ClickResponse(success, Pair(request.x, request.y), true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing click", e)
            ClickResponse(false, null, false)
        }
    }
    
    /**
     * Perform scroll action
     */
    fun performScroll(request: ScrollRequest): ScrollResponse {
        return try {
            val node = if (request.nodeId != null) {
                UiTreeTraversal.findNodeById(request.nodeId)
            } else {
                // Find first scrollable node
                UiTreeTraversal.findScrollableNodes(rootInActiveWindow).firstOrNull()
            }
            
            if (node != null) {
                val action = when (request.direction) {
                    ScrollDirection.UP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    ScrollDirection.DOWN -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    ScrollDirection.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    ScrollDirection.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                }
                
                val success = node.performAction(action)
                ScrollResponse(success, request.direction, true)
            } else {
                ScrollResponse(false, request.direction, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing scroll", e)
            ScrollResponse(false, request.direction, false)
        }
    }
    
    /**
     * Perform text input
     */
    fun performTextInput(request: InputTextRequest): InputTextResponse {
        return try {
            val node = if (request.nodeId != null) {
                UiTreeTraversal.findNodeById(request.nodeId)
            } else {
                // Find focused input field
                findFocusedEditText()
            }
            
            if (node != null) {
                var success = true
                
                // Clear existing text if requested
                if (request.clearFirst) {
                    // Try to clear text by setting empty text first
                    val clearArguments = Bundle()
                    clearArguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
                    success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearArguments)
                }
                
                // Input new text
                if (success) {
                    val arguments = Bundle()
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, request.text)
                    success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                }
                
                InputTextResponse(success, request.text, true)
            } else {
                InputTextResponse(false, request.text, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing text input", e)
            InputTextResponse(false, request.text, false)
        }
    }
    
    private fun performGestureClick(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    private fun findFocusedEditText(): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    /**
     * Perform swipe gesture
     */
    fun performSwipe(request: SwipeRequest): SwipeResponse {
        return try {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val (startX, startY, endX, endY) = when (request.direction) {
                SwipeDirection.LEFT -> {
                    val sX = request.startX ?: (screenWidth * 0.8).toInt()
                    val sY = request.startY ?: (screenHeight * 0.5).toInt()
                    val eX = request.endX ?: (screenWidth * 0.2).toInt()
                    val eY = request.endY ?: sY
                    Tuple4(sX, sY, eX, eY)
                }
                SwipeDirection.RIGHT -> {
                    val sX = request.startX ?: (screenWidth * 0.2).toInt()
                    val sY = request.startY ?: (screenHeight * 0.5).toInt()
                    val eX = request.endX ?: (screenWidth * 0.8).toInt()
                    val eY = request.endY ?: sY
                    Tuple4(sX, sY, eX, eY)
                }
                SwipeDirection.UP -> {
                    val sX = request.startX ?: (screenWidth * 0.5).toInt()
                    val sY = request.startY ?: (screenHeight * 0.8).toInt()
                    val eX = request.endX ?: sX
                    val eY = request.endY ?: (screenHeight * 0.2).toInt()
                    Tuple4(sX, sY, eX, eY)
                }
                SwipeDirection.DOWN -> {
                    val sX = request.startX ?: (screenWidth * 0.5).toInt()
                    val sY = request.startY ?: (screenHeight * 0.2).toInt()
                    val eX = request.endX ?: sX
                    val eY = request.endY ?: (screenHeight * 0.8).toInt()
                    Tuple4(sX, sY, eX, eY)
                }
            }

            val success = performGestureSwipe(startX, startY, endX, endY, request.duration)
            SwipeResponse(success, request.direction, Pair(startX, startY), Pair(endX, endY))
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe", e)
            SwipeResponse(false, request.direction, null, null)
        }
    }

    /**
     * Perform home button action
     */
    fun performHome(): NavigationResponse {
        return try {
            val success = performGlobalAction(GLOBAL_ACTION_HOME)
            NavigationResponse(success, "HOME")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing home action", e)
            NavigationResponse(false, "HOME")
        }
    }

    /**
     * Perform back button action
     */
    fun performBack(): NavigationResponse {
        return try {
            val success = performGlobalAction(GLOBAL_ACTION_BACK)
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
            val success = performGlobalAction(GLOBAL_ACTION_RECENTS)
            NavigationResponse(success, "RECENT")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing recent action", e)
            NavigationResponse(false, "RECENT")
        }
    }

    /**
     * Click on app by name or package
     */
    fun performClickApp(request: ClickAppRequest): ClickAppResponse {
        return try {
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                return ClickAppResponse(false, false, null, null)
            }

            // Search by app name or search text
            val searchText = request.appName ?: request.searchText
            val foundNodes = if (searchText != null) {
                UiTreeTraversal.findNodesByText(rootNode, searchText)
            } else {
                emptyList()
            }

            // Try to find by package name if provided
            val packageNodes = if (request.packageName != null) {
                findNodesByPackage(rootNode, request.packageName)
            } else {
                emptyList()
            }

            val targetNodes = foundNodes + packageNodes
            val clickableNode = targetNodes.firstOrNull { it.isClickable }

            if (clickableNode != null) {
                val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                ClickAppResponse(success, true, request.appName, request.packageName)
            } else {
                ClickAppResponse(false, targetNodes.isNotEmpty(), request.appName, request.packageName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clicking app", e)
            ClickAppResponse(false, false, request.appName, request.packageName)
        }
    }

    private fun performGestureSwipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long): Boolean {
        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())

        // Ensure duration is at least 1ms
        val safeDuration = if (duration <= 0) 300L else duration

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, safeDuration))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun findNodesByPackage(rootNode: AccessibilityNodeInfo, packageName: String): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByPackage(rootNode, packageName, foundNodes)
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

    /**
     * Find elements by criteria
     */
    fun performFindElements(request: FindElementsRequest): FindElementsResponse {
        val rootNode = rootInActiveWindow
        val foundElements = mutableListOf<UiNode>()

        if (rootNode != null) {
            searchElementsByCriteria(rootNode, request, foundElements)
        }

        return FindElementsResponse(foundElements, foundElements.size)
    }

    /**
     * Perform long click
     */
    fun performLongClick(request: LongClickRequest): ActionResponse {
        return try {
            if (request.nodeId != null) {
                val node = UiTreeTraversal.findNodeById(request.nodeId)
                if (node != null) {
                    val success = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                    ActionResponse(success, if (success) "Long click successful" else "Long click failed")
                } else {
                    ActionResponse(false, "Node not found")
                }
            } else if (request.x != null && request.y != null) {
                val success = performGestureLongClick(request.x, request.y, request.duration)
                ActionResponse(success, if (success) "Long click successful" else "Long click failed")
            } else {
                ActionResponse(false, "Either nodeId or coordinates required")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing long click", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    /**
     * Perform double click
     */
    fun performDoubleClick(request: DoubleClickRequest): ActionResponse {
        return try {
            val success = performGestureDoubleClick(request.x, request.y, request.delay)
            ActionResponse(success, if (success) "Double click successful" else "Double click failed")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing double click", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    /**
     * Take screenshot using shell command
     */
    fun takeScreenshot(): ActionResponse {
        return try {
            val timestamp = System.currentTimeMillis()
            val filename = "screenshot_$timestamp.png"

            // Try multiple screenshot paths
            val possiblePaths = listOf(
                "/sdcard/Pictures/$filename",
                "/sdcard/DCIM/$filename",
                "/sdcard/Download/$filename",
                "/data/local/tmp/$filename"
            )

            for (filepath in possiblePaths) {
                try {
                    val process = Runtime.getRuntime().exec("screencap -p $filepath")
                    val exitCode = process.waitFor()

                    if (exitCode == 0) {
                        // Verify file exists
                        val checkProcess = Runtime.getRuntime().exec("ls $filepath")
                        if (checkProcess.waitFor() == 0) {
                            return ActionResponse(true, "Screenshot saved to $filepath")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to save screenshot to $filepath: ${e.message}")
                    continue
                }
            }

            // If all paths failed, try without specifying path
            val process = Runtime.getRuntime().exec("screencap")
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                ActionResponse(true, "Screenshot taken (path unknown - check /sdcard/)")
            } else {
                ActionResponse(false, "Screenshot failed - may need root permissions or storage access")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error taking screenshot", e)
            ActionResponse(false, "Screenshot error: ${e.message}. Try enabling storage permissions.")
        }
    }

    /**
     * Get device information
     */
    fun getDeviceInfo(): DeviceInfoResponse {
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration

        return DeviceInfoResponse(
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels,
            density = displayMetrics.density,
            orientation = if (configuration.orientation == 1) "portrait" else "landscape",
            androidVersion = android.os.Build.VERSION.RELEASE,
            apiLevel = android.os.Build.VERSION.SDK_INT,
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            currentApp = rootInActiveWindow?.packageName?.toString()
        )
    }

    /**
     * Open notifications panel
     */
    fun openNotifications(): ActionResponse {
        return try {
            val success = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            ActionResponse(success, if (success) "Notifications opened" else "Failed to open notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening notifications", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    /**
     * Open quick settings
     */
    fun openQuickSettings(): ActionResponse {
        return try {
            val success = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            ActionResponse(success, if (success) "Quick settings opened" else "Failed to open quick settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening quick settings", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    /**
     * Launch app by package name
     */
    fun launchApp(request: LaunchAppRequest): ActionResponse {
        return try {
            val packageManager = packageManager
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
                startActivity(intent)
                ActionResponse(true, "App launched successfully")
            } else {
                ActionResponse(false, "App not found or no launch intent")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    /**
     * Wait for element to appear
     */
    fun waitForElement(request: WaitForElementRequest): ActionResponse {
        val startTime = System.currentTimeMillis()
        val timeout = request.timeout

        while (System.currentTimeMillis() - startTime < timeout) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val found = when {
                    request.nodeId != null -> UiTreeTraversal.findNodeById(request.nodeId) != null
                    request.text != null -> UiTreeTraversal.findNodesByText(rootNode, request.text).isNotEmpty()
                    request.className != null -> findNodesByClassName(rootNode, request.className).isNotEmpty()
                    else -> false
                }

                if (found) {
                    return ActionResponse(true, "Element found")
                }
            }

            Thread.sleep(500) // Check every 500ms
        }

        return ActionResponse(false, "Element not found within timeout")
    }

    /**
     * Get list of recent apps
     */
    fun getRecentApps(): RecentAppsResponse {
        return try {
            // First open recent apps
            val recentOpened = performGlobalAction(GLOBAL_ACTION_RECENTS)
            if (!recentOpened) {
                return RecentAppsResponse(false, emptyList(), 0, "Failed to open recent apps")
            }

            // Wait a bit for recent apps to load
            Thread.sleep(1000)

            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                return RecentAppsResponse(false, emptyList(), 0, "No UI tree available")
            }

            // Find recent app cards/items
            val recentApps = mutableListOf<RecentAppInfo>()
            var position = 0

            // Look for app cards in recent apps UI
            findRecentAppCards(rootNode, recentApps, position)

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
     * Open app from recent apps
     */
    fun openFromRecent(request: RecentAppRequest): ActionResponse {
        return try {
            // First get recent apps to find the target
            val recentAppsResponse = getRecentApps()
            if (!recentAppsResponse.success) {
                return ActionResponse(false, "Failed to get recent apps")
            }

            val targetApp = when {
                request.position != null -> {
                    recentAppsResponse.apps.getOrNull(request.position)
                }
                request.packageName != null -> {
                    recentAppsResponse.apps.find { it.packageName == request.packageName }
                }
                request.appName != null -> {
                    recentAppsResponse.apps.find { it.appName?.contains(request.appName, ignoreCase = true) == true }
                }
                else -> null
            }

            if (targetApp == null) {
                return ActionResponse(false, "App not found in recent apps")
            }

            // Click on the app card
            val centerX = targetApp.bounds.centerX
            val centerY = targetApp.bounds.centerY
            val success = performGestureClick(centerX, centerY)

            ActionResponse(success, if (success) "Opened ${targetApp.appName}" else "Failed to click on app")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening from recent", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    /**
     * Close app from recent apps (swipe up to dismiss)
     */
    fun closeFromRecent(request: RecentAppRequest): ActionResponse {
        return try {
            // First get recent apps to find the target
            val recentAppsResponse = getRecentApps()
            if (!recentAppsResponse.success) {
                return ActionResponse(false, "Failed to get recent apps")
            }

            val targetApp = when {
                request.position != null -> {
                    recentAppsResponse.apps.getOrNull(request.position)
                }
                request.packageName != null -> {
                    recentAppsResponse.apps.find { it.packageName == request.packageName }
                }
                request.appName != null -> {
                    recentAppsResponse.apps.find { it.appName?.contains(request.appName, ignoreCase = true) == true }
                }
                else -> null
            }

            if (targetApp == null) {
                return ActionResponse(false, "App not found in recent apps")
            }

            // Swipe up on the app card to close it
            val centerX = targetApp.bounds.centerX
            val startY = targetApp.bounds.centerY
            val endY = targetApp.bounds.top - 200 // Swipe up beyond the card

            val success = performGestureSwipe(centerX, startY, centerX, endY, 300)

            ActionResponse(success, if (success) "Closed ${targetApp.appName}" else "Failed to close app")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing from recent", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }

    private fun findRecentAppCards(node: AccessibilityNodeInfo, recentApps: MutableList<RecentAppInfo>, position: Int) {
        val packageName = node.packageName?.toString()
        val className = node.className?.toString()
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        // Enhanced filtering for recent app cards
        val isValidRecentCard = packageName != null &&
            packageName != "com.android.systemui" &&
            node.isClickable &&
            bounds.width() > 50 && bounds.height() > 50 && // Minimum size
            bounds.top > 100 && // Not in status bar area
            !isAlreadyAdded(packageName, recentApps) && // Avoid duplicates
            isLikelyAppCard(node, className, bounds)

        if (isValidRecentCard) {
            val nodeBounds = NodeBounds.fromRect(bounds)

            // Enhanced app name detection
            val appName = findBestAppName(node, packageName!!)

            // Only add if we found a meaningful app name or it's not launcher
            if (appName != "launcher" || !packageName.contains("launcher")) {
                val recentApp = RecentAppInfo(
                    appName = appName,
                    packageName = packageName,
                    position = recentApps.size,
                    bounds = nodeBounds,
                    isClickable = node.isClickable
                )

                recentApps.add(recentApp)
                Log.d(TAG, "Found recent app: $appName ($packageName) at ${bounds}")
            }
        }

        // Search children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findRecentAppCards(child, recentApps, position)
            }
        }
    }

    private fun isLikelyAppCard(node: AccessibilityNodeInfo, className: String?, bounds: Rect): Boolean {
        // Check if this looks like an app card based on various criteria
        return when {
            // Common app card class names
            className?.contains("Card", ignoreCase = true) == true -> true
            className?.contains("Item", ignoreCase = true) == true -> true
            className?.contains("Task", ignoreCase = true) == true -> true
            className?.contains("Recent", ignoreCase = true) == true -> true

            // Size-based detection (typical app card sizes)
            bounds.width() in 200..800 && bounds.height() in 150..600 -> true

            // Has app-like content (icon + text)
            hasAppLikeContent(node) -> true

            else -> false
        }
    }

    private fun hasAppLikeContent(node: AccessibilityNodeInfo): Boolean {
        var hasIcon = false
        var hasText = false

        // Check current node and immediate children for app-like content
        checkNodeForAppContent(node) { icon, text ->
            hasIcon = hasIcon || icon
            hasText = hasText || text
        }

        for (i in 0 until minOf(node.childCount, 5)) { // Check first 5 children
            node.getChild(i)?.let { child ->
                checkNodeForAppContent(child) { icon, text ->
                    hasIcon = hasIcon || icon
                    hasText = hasText || text
                }
            }
        }

        return hasIcon || hasText
    }

    private fun checkNodeForAppContent(node: AccessibilityNodeInfo, callback: (Boolean, Boolean) -> Unit) {
        val className = node.className?.toString()
        val text = node.text?.toString()
        val contentDesc = node.contentDescription?.toString()

        val hasIcon = className?.contains("Image", ignoreCase = true) == true ||
                     className?.contains("Icon", ignoreCase = true) == true

        val hasText = !text.isNullOrBlank() && text.length < 50 ||
                     !contentDesc.isNullOrBlank() && contentDesc.length < 50

        callback(hasIcon, hasText)
    }

    private fun isAlreadyAdded(packageName: String, recentApps: List<RecentAppInfo>): Boolean {
        return recentApps.any { it.packageName == packageName }
    }

    private fun findBestAppName(node: AccessibilityNodeInfo, packageName: String): String {
        // Try multiple strategies to find the best app name

        // Strategy 1: Look for text in current node
        node.text?.toString()?.let { text ->
            if (isValidAppName(text)) return text
        }

        node.contentDescription?.toString()?.let { desc ->
            if (isValidAppName(desc)) return desc
        }

        // Strategy 2: Look in children for app name
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val childAppName = findAppNameInNodeRecursive(child, 2) // Max depth 2
                if (childAppName != null && isValidAppName(childAppName)) {
                    return childAppName
                }
            }
        }

        // Strategy 3: Extract from package name
        val packageParts = packageName.split(".")
        val appNameFromPackage = when {
            packageParts.size >= 3 -> packageParts[2] // com.google.chrome -> chrome
            packageParts.size >= 2 -> packageParts[1] // com.android -> android
            else -> packageParts.lastOrNull() ?: "unknown"
        }

        // Strategy 4: Known package mappings
        val knownApps = mapOf(
            "com.google.android.youtube" to "YouTube",
            "com.android.settings" to "Settings",
            "com.android.chrome" to "Chrome",
            "com.whatsapp" to "WhatsApp",
            "com.facebook.katana" to "Facebook",
            "com.instagram.android" to "Instagram",
            "com.android.camera2" to "Camera",
            "com.android.gallery3d" to "Gallery",
            "com.android.contacts" to "Contacts",
            "com.android.dialer" to "Phone",
            "com.android.mms" to "Messages"
        )

        return knownApps[packageName] ?: appNameFromPackage.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    private fun isValidAppName(name: String): Boolean {
        return name.isNotBlank() &&
               name.length in 2..30 &&
               !name.contains("com.") &&
               !name.contains("android.") &&
               !name.contains("View") &&
               !name.contains("Layout") &&
               !name.contains("Button") &&
               name != "launcher"
    }

    private fun findAppNameInNodeRecursive(node: AccessibilityNodeInfo, maxDepth: Int): String? {
        if (maxDepth <= 0) return null

        // Check current node
        node.text?.toString()?.let { text ->
            if (isValidAppName(text)) return text
        }

        node.contentDescription?.toString()?.let { desc ->
            if (isValidAppName(desc)) return desc
        }

        // Check children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findAppNameInNodeRecursive(child, maxDepth - 1)?.let { return it }
            }
        }

        return null
    }



    /**
     * Close app by package name or current app
     */
    fun closeApp(request: CloseAppRequest): ActionResponse {
        return try {
            val packageToClose = request.packageName ?: rootInActiveWindow?.packageName?.toString()

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
                    if (rootInActiveWindow?.packageName?.toString() == packageToClose) {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        Thread.sleep(500)
                    } else {
                        closed = true
                        return@repeat
                    }
                }

                // If still not closed, try recent apps and swipe up to close
                if (!closed && rootInActiveWindow?.packageName?.toString() == packageToClose) {
                    performGlobalAction(GLOBAL_ACTION_RECENTS)
                    Thread.sleep(1000)

                    // Swipe up to close current app in recent apps
                    val displayMetrics = resources.displayMetrics
                    val centerX = displayMetrics.widthPixels / 2
                    val startY = (displayMetrics.heightPixels * 0.7).toInt()
                    val endY = (displayMetrics.heightPixels * 0.3).toInt()

                    performGestureSwipe(centerX, startY, centerX, endY, 300)
                    closed = true
                }

                closed
            }

            ActionResponse(success, if (success) "App closed: $packageToClose" else "Failed to close app: $packageToClose")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing app", e)
            ActionResponse(false, "Error: ${e.message}")
        }
    }



    private fun performGestureLongClick(x: Int, y: Int, duration: Long): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        val safeDuration = if (duration <= 0) 1000L else duration

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, safeDuration))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun performGestureDoubleClick(x: Int, y: Int, delay: Long): Boolean {
        val path1 = Path()
        path1.moveTo(x.toFloat(), y.toFloat())

        val path2 = Path()
        path2.moveTo(x.toFloat(), y.toFloat())

        val safeDelay = if (delay <= 0) 100L else delay

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path1, 0, 50))
            .addStroke(GestureDescription.StrokeDescription(path2, safeDelay, 50))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun searchElementsByCriteria(
        node: AccessibilityNodeInfo,
        criteria: FindElementsRequest,
        results: MutableList<UiNode>
    ) {
        var matches = true

        // Check all criteria
        criteria.text?.let { text ->
            val nodeText = node.text?.toString() ?: ""
            if (!nodeText.contains(text, ignoreCase = true)) matches = false
        }

        criteria.className?.let { className ->
            val nodeClassName = node.className?.toString() ?: ""
            if (!nodeClassName.contains(className, ignoreCase = true)) matches = false
        }

        criteria.packageName?.let { packageName ->
            val nodePackage = node.packageName?.toString() ?: ""
            if (!nodePackage.contains(packageName, ignoreCase = true)) matches = false
        }

        criteria.contentDescription?.let { desc ->
            val nodeDesc = node.contentDescription?.toString() ?: ""
            if (!nodeDesc.contains(desc, ignoreCase = true)) matches = false
        }

        criteria.clickable?.let { clickable ->
            if (node.isClickable != clickable) matches = false
        }

        criteria.scrollable?.let { scrollable ->
            if (node.isScrollable != scrollable) matches = false
        }

        if (matches) {
            results.add(UiTreeTraversal.convertToUiNode(node))
        }

        // Search children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchElementsByCriteria(child, criteria, results)
            }
        }
    }

    private fun findNodesByClassName(rootNode: AccessibilityNodeInfo, className: String): List<AccessibilityNodeInfo> {
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByClassName(rootNode, className, foundNodes)
        return foundNodes
    }

    private fun searchNodesByClassName(
        node: AccessibilityNodeInfo,
        className: String,
        foundNodes: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.className?.toString()?.contains(className, ignoreCase = true) == true) {
            foundNodes.add(node)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                searchNodesByClassName(child, className, foundNodes)
            }
        }
    }

    // Helper data class for tuple
    private data class Tuple4<T>(val first: T, val second: T, val third: T, val fourth: T)
}
