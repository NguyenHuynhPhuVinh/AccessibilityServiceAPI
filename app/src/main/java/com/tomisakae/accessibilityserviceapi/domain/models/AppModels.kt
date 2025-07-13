package com.tomisakae.accessibilityserviceapi.domain.models

/**
 * Click app request
 */
data class ClickAppRequest(
    val appName: String? = null,
    val packageName: String? = null,
    val searchText: String? = null
)

/**
 * Click app response
 */
data class ClickAppResponse(
    val success: Boolean,
    val appFound: Boolean,
    val appName: String?,
    val packageName: String?
)

/**
 * App launch request
 */
data class LaunchAppRequest(
    val packageName: String,
    val activityName: String? = null
)

/**
 * Close app request
 */
data class CloseAppRequest(
    val packageName: String? = null,
    val forceKill: Boolean = false
)

/**
 * Recent app info
 */
data class RecentAppInfo(
    val appName: String?,
    val packageName: String?,
    val position: Int,
    val bounds: NodeBounds,
    val isClickable: Boolean
)

/**
 * Recent apps response
 */
data class RecentAppsResponse(
    val success: Boolean,
    val apps: List<RecentAppInfo>,
    val totalApps: Int,
    val message: String? = null
)

/**
 * Recent app action request
 */
data class RecentAppRequest(
    val packageName: String? = null,
    val appName: String? = null,
    val position: Int? = null
)
