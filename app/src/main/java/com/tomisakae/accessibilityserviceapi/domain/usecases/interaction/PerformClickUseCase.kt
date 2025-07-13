package com.tomisakae.accessibilityserviceapi.domain.usecases.interaction

import com.tomisakae.accessibilityserviceapi.domain.models.ClickRequest
import com.tomisakae.accessibilityserviceapi.domain.models.ClickResponse
import com.tomisakae.accessibilityserviceapi.domain.repositories.InteractionRepository
import com.tomisakae.accessibilityserviceapi.domain.usecases.BaseUseCase

/**
 * Use case for performing click actions
 */
class PerformClickUseCase(
    private val interactionRepository: InteractionRepository
) : BaseUseCase<ClickRequest, ClickResponse> {
    
    override suspend fun invoke(params: ClickRequest): ClickResponse {
        return interactionRepository.performClick(params)
    }
}
