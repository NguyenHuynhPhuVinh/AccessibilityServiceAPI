package com.tomisakae.accessibilityserviceapi.domain.usecases.system

import com.tomisakae.accessibilityserviceapi.domain.models.HealthResponse
import com.tomisakae.accessibilityserviceapi.domain.repositories.SystemRepository
import com.tomisakae.accessibilityserviceapi.domain.usecases.BaseUseCaseNoParams

/**
 * Use case for getting health status
 */
class GetHealthStatusUseCase(
    private val systemRepository: SystemRepository
) : BaseUseCaseNoParams<HealthResponse> {
    
    override suspend fun invoke(): HealthResponse {
        return systemRepository.getHealthStatus()
    }
}
