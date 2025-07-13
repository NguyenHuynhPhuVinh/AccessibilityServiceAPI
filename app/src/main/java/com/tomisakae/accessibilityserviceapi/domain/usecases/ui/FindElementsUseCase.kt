package com.tomisakae.accessibilityserviceapi.domain.usecases.ui

import com.tomisakae.accessibilityserviceapi.domain.models.FindElementsRequest
import com.tomisakae.accessibilityserviceapi.domain.models.FindElementsResponse
import com.tomisakae.accessibilityserviceapi.domain.repositories.UiRepository
import com.tomisakae.accessibilityserviceapi.domain.usecases.BaseUseCase

/**
 * Use case for finding UI elements
 */
class FindElementsUseCase(
    private val uiRepository: UiRepository
) : BaseUseCase<FindElementsRequest, FindElementsResponse> {
    
    override suspend fun invoke(params: FindElementsRequest): FindElementsResponse {
        return uiRepository.findElements(params)
    }
}
