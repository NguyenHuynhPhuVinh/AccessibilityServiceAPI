package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tomisakae.accessibilityserviceapi.domain.models.ApiResponse
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

/**
 * Base controller with common functionality
 */
abstract class BaseController {
    
    protected val gson = Gson()
    
    /**
     * Create JSON response
     */
    protected fun <T> createJsonResponse(statusCode: Int, data: ApiResponse<T>): NanoHTTPD.Response {
        return NanoHTTPD.newFixedLengthResponse(
            when (statusCode) {
                200 -> NanoHTTPD.Response.Status.OK
                400 -> NanoHTTPD.Response.Status.BAD_REQUEST
                404 -> NanoHTTPD.Response.Status.NOT_FOUND
                500 -> NanoHTTPD.Response.Status.INTERNAL_ERROR
                else -> NanoHTTPD.Response.Status.OK
            },
            "application/json",
            gson.toJson(data)
        ).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type")
        }
    }
    
    /**
     * Create success response
     */
    protected fun <T> createSuccessResponse(data: T): NanoHTTPD.Response {
        val response = ApiResponse(success = true, data = data)
        return createJsonResponse(200, response)
    }
    
    /**
     * Create error response
     */
    protected fun createErrorResponse(statusCode: Int, errorCode: String, message: String): NanoHTTPD.Response {
        val response = ApiResponse<Nothing>(
            success = false,
            error = "$errorCode: $message"
        )
        return createJsonResponse(statusCode, response)
    }
    
    /**
     * Parse JSON request body
     */
    protected inline fun <reified T> parseRequestBody(session: NanoHTTPD.IHTTPSession): T? {
        return try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)
            val postData = files["postData"] ?: return null
            gson.fromJson(postData, T::class.java)
        } catch (e: IOException) {
            null
        } catch (e: JsonSyntaxException) {
            null
        }
    }
    
    /**
     * Get request body as string
     */
    protected fun getRequestBodyString(session: NanoHTTPD.IHTTPSession): String? {
        return try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)
            files["postData"]
        } catch (e: IOException) {
            null
        }
    }
}
