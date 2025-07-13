package com.tomisakae.accessibilityserviceapi.domain.usecases.ui

import com.tomisakae.accessibilityserviceapi.domain.models.UiTreeResponse
import com.tomisakae.accessibilityserviceapi.domain.repositories.UiRepository
import com.tomisakae.accessibilityserviceapi.domain.usecases.BaseUseCaseNoParams

/**
 * Use case for getting current UI tree
 */
class GetUiTreeUseCase(
    private val uiRepository: UiRepository
) : BaseUseCaseNoParams<UiTreeResponse> {
    
    override suspend fun invoke(): UiTreeResponse {
        return uiRepository.getCurrentUiTree()
    }
}
