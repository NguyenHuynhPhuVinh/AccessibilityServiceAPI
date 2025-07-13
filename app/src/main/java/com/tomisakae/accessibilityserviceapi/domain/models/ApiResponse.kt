package com.tomisakae.accessibilityserviceapi.domain.models

/**
 * Base response class for all API responses
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Error response for API errors
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: String? = null
)

/**
 * Generic action response
 */
data class ActionResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)
