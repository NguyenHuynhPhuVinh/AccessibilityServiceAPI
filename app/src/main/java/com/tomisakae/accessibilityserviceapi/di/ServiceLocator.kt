package com.tomisakae.accessibilityserviceapi.di

import com.tomisakae.accessibilityserviceapi.data.repositories.AppRepositoryImpl
import com.tomisakae.accessibilityserviceapi.data.repositories.InteractionRepositoryImpl
import com.tomisakae.accessibilityserviceapi.data.repositories.SystemRepositoryImpl
import com.tomisakae.accessibilityserviceapi.data.repositories.UiRepositoryImpl
import com.tomisakae.accessibilityserviceapi.domain.repositories.*
import com.tomisakae.accessibilityserviceapi.domain.usecases.interaction.PerformClickUseCase
import com.tomisakae.accessibilityserviceapi.domain.usecases.interaction.PerformScrollUseCase
import com.tomisakae.accessibilityserviceapi.domain.usecases.system.GetHealthStatusUseCase
import com.tomisakae.accessibilityserviceapi.domain.usecases.ui.FindElementsUseCase
import com.tomisakae.accessibilityserviceapi.domain.usecases.ui.GetUiTreeUseCase
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager

/**
 * Simple service locator for dependency injection
 * This is a lightweight alternative to full DI frameworks like Dagger/Hilt
 */
object ServiceLocator {
    
    // Repositories
    private var _uiRepository: UiRepository? = null
    private var _interactionRepository: InteractionRepository? = null
    private var _systemRepository: SystemRepository? = null
    private var _appRepository: AppRepository? = null
    
    // Use Cases
    private var _getUiTreeUseCase: GetUiTreeUseCase? = null
    private var _findElementsUseCase: FindElementsUseCase? = null
    private var _performClickUseCase: PerformClickUseCase? = null
    private var _performScrollUseCase: PerformScrollUseCase? = null
    private var _getHealthStatusUseCase: GetHealthStatusUseCase? = null
    
    /**
     * Initialize all dependencies
     */
    fun initialize(serviceManager: AccessibilityServiceManager) {
        // Initialize repositories
        _uiRepository = UiRepositoryImpl(serviceManager)
        _interactionRepository = InteractionRepositoryImpl(serviceManager)
        _systemRepository = SystemRepositoryImpl(serviceManager)
        _appRepository = AppRepositoryImpl(serviceManager)
        
        // Initialize use cases
        _getUiTreeUseCase = GetUiTreeUseCase(uiRepository)
        _findElementsUseCase = FindElementsUseCase(uiRepository)
        _performClickUseCase = PerformClickUseCase(interactionRepository)
        _performScrollUseCase = PerformScrollUseCase(interactionRepository)
        _getHealthStatusUseCase = GetHealthStatusUseCase(systemRepository)
    }
    
    /**
     * Clear all dependencies
     */
    fun clear() {
        _uiRepository = null
        _interactionRepository = null
        _systemRepository = null
        _appRepository = null
        
        _getUiTreeUseCase = null
        _findElementsUseCase = null
        _performClickUseCase = null
        _performScrollUseCase = null
        _getHealthStatusUseCase = null
    }
    
    // Repository getters
    val uiRepository: UiRepository
        get() = _uiRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val interactionRepository: InteractionRepository
        get() = _interactionRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val systemRepository: SystemRepository
        get() = _systemRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val appRepository: AppRepository
        get() = _appRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    
    // Use case getters
    val getUiTreeUseCase: GetUiTreeUseCase
        get() = _getUiTreeUseCase ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val findElementsUseCase: FindElementsUseCase
        get() = _findElementsUseCase ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val performClickUseCase: PerformClickUseCase
        get() = _performClickUseCase ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val performScrollUseCase: PerformScrollUseCase
        get() = _performScrollUseCase ?: throw IllegalStateException("ServiceLocator not initialized")
    
    val getHealthStatusUseCase: GetHealthStatusUseCase
        get() = _getHealthStatusUseCase ?: throw IllegalStateException("ServiceLocator not initialized")
}
