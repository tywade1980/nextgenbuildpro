package com.nextgenbuildpro.features.leads.di

import com.google.firebase.firestore.FirebaseFirestore
import com.nextgenbuildpro.core.firestore.FirebaseFirestoreInitializer
import com.nextgenbuildpro.core.storage.FirebaseStorageRepository
import com.nextgenbuildpro.features.leads.data.FirestoreLeadRepository
import com.nextgenbuildpro.features.leads.domain.LeadRepository

/**
 * Module for providing lead repository dependencies
 */
object LeadRepositoryModule {
    
    /**
     * Provide a FirebaseFirestore instance
     */
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestoreInitializer.firestore
    }
    
    /**
     * Provide a FirebaseStorageRepository instance
     */
    fun provideStorageRepository(): FirebaseStorageRepository {
        return FirebaseStorageRepository()
    }
    
    /**
     * Provide a LeadRepository implementation
     */
    fun provideLeadRepository(
        firestore: FirebaseFirestore = provideFirestore(),
        storageRepository: FirebaseStorageRepository = provideStorageRepository()
    ): LeadRepository {
        return FirestoreLeadRepository(firestore, storageRepository)
    }
}