package com.tomisakae.accessibilityserviceapi.domain.usecases.interaction

import com.tomisakae.accessibilityserviceapi.domain.models.ScrollRequest
import com.tomisakae.accessibilityserviceapi.domain.models.ScrollResponse
import com.tomisakae.accessibilityserviceapi.domain.repositories.InteractionRepository
import com.tomisakae.accessibilityserviceapi.domain.usecases.BaseUseCase

/**
 * Use case for performing scroll actions
 */
class PerformScrollUseCase(
    private val interactionRepository: InteractionRepository
) : BaseUseCase<ScrollRequest, ScrollResponse> {
    
    override suspend fun invoke(params: ScrollRequest): ScrollResponse {
        return interactionRepository.performScroll(params)
    }
}
