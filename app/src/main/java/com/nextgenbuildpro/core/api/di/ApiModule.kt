package com.nextgenbuildpro.core.api.di

import android.content.Context
import com.nextgenbuildpro.core.api.ApiUsageRegistry
import com.nextgenbuildpro.core.api.ConfigurationService
import com.nextgenbuildpro.core.api.DataValidationAndCachingService
import com.nextgenbuildpro.core.api.FirebaseStorageService
import com.nextgenbuildpro.core.api.FirestoreService
import com.nextgenbuildpro.core.api.SecureCredentialStorage
import com.nextgenbuildpro.core.api.impl.ApiUsageRegistryImpl
import com.nextgenbuildpro.core.api.impl.ConfigurationServiceImpl
import com.nextgenbuildpro.core.api.impl.DataValidationAndCachingServiceImpl
import com.nextgenbuildpro.core.api.impl.FirebaseStorageServiceImpl
import com.nextgenbuildpro.core.api.impl.FirestoreServiceImpl
import com.nextgenbuildpro.core.api.impl.SecureCredentialStorageImpl

/**
 * Dependency Injection module for API services.
 * 
 * This class provides factory methods for creating instances of API service interfaces.
 * It follows the Service Locator pattern to make it easier to:
 * 1. Swap implementations if the API changes
 * 2. Mock services for testing
 * 3. Manage dependencies between services
 */
object ApiModule {
    
    private var apiUsageRegistry: ApiUsageRegistry? = null
    private var configurationService: ConfigurationService? = null
    private var secureCredentialStorage: SecureCredentialStorage? = null
    private var dataValidationAndCachingService: DataValidationAndCachingService? = null
    private var firebaseStorageService: FirebaseStorageService? = null
    private var firestoreService: FirestoreService? = null
    
    /**
     * Initialize all API services
     * @param context The application context
     */
    fun initialize(context: Context) {
        // Create instances in dependency order
        apiUsageRegistry = provideApiUsageRegistry(context)
        configurationService = provideConfigurationService(context)
        secureCredentialStorage = provideSecureCredentialStorage(context)
        dataValidationAndCachingService = provideDataValidationAndCachingService(context)
        firebaseStorageService = provideFirebaseStorageService(
            provideConfigurationService(context),
            provideApiUsageRegistry(context)
        )
        firestoreService = provideFirestoreService(
            provideConfigurationService(context),
            provideApiUsageRegistry(context)
        )
    }
    
    /**
     * Provide an instance of ApiUsageRegistry
     * @param context The application context
     * @return An instance of ApiUsageRegistry
     */
    fun provideApiUsageRegistry(context: Context): ApiUsageRegistry {
        return apiUsageRegistry ?: ApiUsageRegistryImpl(context).also {
            apiUsageRegistry = it
        }
    }
    
    /**
     * Provide an instance of ConfigurationService
     * @param context The application context
     * @return An instance of ConfigurationService
     */
    fun provideConfigurationService(context: Context): ConfigurationService {
        return configurationService ?: ConfigurationServiceImpl(context).also {
            configurationService = it
        }
    }
    
    /**
     * Provide an instance of SecureCredentialStorage
     * @param context The application context
     * @return An instance of SecureCredentialStorage
     */
    fun provideSecureCredentialStorage(context: Context): SecureCredentialStorage {
        return secureCredentialStorage ?: SecureCredentialStorageImpl().also {
            it.initialize(context)
            secureCredentialStorage = it
        }
    }
    
    /**
     * Provide an instance of DataValidationAndCachingService
     * @param context The application context
     * @return An instance of DataValidationAndCachingService
     */
    fun provideDataValidationAndCachingService(context: Context): DataValidationAndCachingService {
        return dataValidationAndCachingService ?: DataValidationAndCachingServiceImpl(context).also {
            dataValidationAndCachingService = it
        }
    }
    
    /**
     * Provide an instance of FirebaseStorageService
     * @param configService The ConfigurationService instance
     * @param apiUsageRegistry The ApiUsageRegistry instance
     * @return An instance of FirebaseStorageService
     */
    fun provideFirebaseStorageService(
        configService: ConfigurationService,
        apiUsageRegistry: ApiUsageRegistry
    ): FirebaseStorageService {
        return firebaseStorageService ?: FirebaseStorageServiceImpl(
            configService,
            apiUsageRegistry
        ).also {
            firebaseStorageService = it
        }
    }
    
    /**
     * Provide an instance of FirestoreService
     * @param configService The ConfigurationService instance
     * @param apiUsageRegistry The ApiUsageRegistry instance
     * @return An instance of FirestoreService
     */
    fun provideFirestoreService(
        configService: ConfigurationService,
        apiUsageRegistry: ApiUsageRegistry
    ): FirestoreService {
        return firestoreService ?: FirestoreServiceImpl(
            configService,
            apiUsageRegistry
        ).also {
            firestoreService = it
        }
    }
    
    /**
     * Reset all services (useful for testing)
     */
    fun reset() {
        apiUsageRegistry = null
        configurationService = null
        secureCredentialStorage = null
        dataValidationAndCachingService = null
        firebaseStorageService = null
        firestoreService = null
    }
}