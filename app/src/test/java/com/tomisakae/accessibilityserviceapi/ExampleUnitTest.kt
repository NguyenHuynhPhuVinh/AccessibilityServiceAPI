package com.tomisakae.accessibilityserviceapi

import com.tomisakae.accessibilityserviceapi.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for API models and utilities
 */
class ApiModelsTest {

    @Test
    fun testNodeBoundsCreation() {
        val bounds = NodeBounds(10, 20, 100, 200)

        assertEquals(10, bounds.left)
        assertEquals(20, bounds.top)
        assertEquals(100, bounds.right)
        assertEquals(200, bounds.bottom)
        assertEquals(90, bounds.width)
        assertEquals(180, bounds.height)
        assertEquals(55, bounds.centerX)
        assertEquals(110, bounds.centerY)
    }

    @Test
    fun testApiResponseSuccess() {
        val data = HealthResponse("OK", "1.0.0", true, 1000L)
        val response = ApiResponse(success = true, data = data)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertNull(response.error)
        assertTrue(response.timestamp > 0)
    }

    @Test
    fun testApiResponseError() {
        val response = ApiResponse<Nothing>(success = false, error = "Test error")

        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("Test error", response.error)
    }

    @Test
    fun testClickRequest() {
        val clickRequest = ClickRequest(100, 200, "test_node")

        assertEquals(100, clickRequest.x)
        assertEquals(200, clickRequest.y)
        assertEquals("test_node", clickRequest.nodeId)
    }

    @Test
    fun testScrollDirection() {
        assertEquals(4, ScrollDirection.values().size)
        assertTrue(ScrollDirection.values().contains(ScrollDirection.UP))
        assertTrue(ScrollDirection.values().contains(ScrollDirection.DOWN))
        assertTrue(ScrollDirection.values().contains(ScrollDirection.LEFT))
        assertTrue(ScrollDirection.values().contains(ScrollDirection.RIGHT))
    }

    @Test
    fun testInputTextRequest() {
        val inputRequest = InputTextRequest("Hello World", "input_node", true)

        assertEquals("Hello World", inputRequest.text)
        assertEquals("input_node", inputRequest.nodeId)
        assertTrue(inputRequest.clearFirst)
    }
}