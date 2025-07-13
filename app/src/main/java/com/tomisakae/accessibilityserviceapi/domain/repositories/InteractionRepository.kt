package com.tomisakae.accessibilityserviceapi.domain.repositories

import com.tomisakae.accessibilityserviceapi.domain.models.*

/**
 * Repository interface for user interactions
 */
interface InteractionRepository {
    
    /**
     * Perform click action
     */
    suspend fun performClick(request: ClickRequest): ClickResponse
    
    /**
     * Perform long click action
     */
    suspend fun performLongClick(request: LongClickRequest): ActionResponse
    
    /**
     * Perform double click action
     */
    suspend fun performDoubleClick(request: DoubleClickRequest): ActionResponse
    
    /**
     * Perform scroll action
     */
    suspend fun performScroll(request: ScrollRequest): ScrollResponse
    
    /**
     * Perform swipe action
     */
    suspend fun performSwipe(request: SwipeRequest): SwipeResponse
    
    /**
     * Input text
     */
    suspend fun inputText(request: InputTextRequest): InputTextResponse
}
