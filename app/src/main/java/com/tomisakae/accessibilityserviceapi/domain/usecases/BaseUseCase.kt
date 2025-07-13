package com.tomisakae.accessibilityserviceapi.domain.usecases

/**
 * Base use case interface
 */
interface BaseUseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

/**
 * Use case without parameters
 */
interface BaseUseCaseNoParams<out R> {
    suspend operator fun invoke(): R
}
