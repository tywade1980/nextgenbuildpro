package com.nextgenbuildpro.pm.demo

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.repository.SeedCatalogueRunner

/**
 * Catalogue Seeder Demo
 * 
 * Demo class to demonstrate the seeding functionality
 * This provides an easy way to trigger the seeding process and verify results
 */
class CatalogueSeederDemo(private val context: Context) {
    private val TAG = "CatalogueSeederDemo"
    
    /**
     * Run the complete seeding demonstration
     */
    fun runSeederDemo() {
        Log.d(TAG, "=== Starting Catalogue Seeder Demo ===")
        
        try {
            // Run the seeding process
            val seedingSuccess = SeedCatalogueRunner.runSeeding(context)
            
            if (seedingSuccess) {
                Log.d(TAG, "✓ Seeding process completed successfully")
                
                // Verify the seeded data
                SeedCatalogueRunner.verifySeedData(context)
                
                Log.d(TAG, "✓ Data verification completed")
                Log.d(TAG, "=== Catalogue Seeder Demo Complete ===")
                Log.d(TAG, "The construction catalogue has been successfully seeded with:")
                Log.d(TAG, "  • 10 comprehensive construction categories")
                Log.d(TAG, "  • Interior Finishes with Stair Construction assemblies")
                Log.d(TAG, "  • Plumbing with Bathroom Fixture Installation assemblies")
                Log.d(TAG, "  • Electrical with Room Device Installation assemblies")
                Log.d(TAG, "  • Detailed tasks and materials for each assembly")
                
            } else {
                Log.e(TAG, "✗ Seeding process failed")
                Log.e(TAG, "Please check the logs above for specific error details")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Demo failed with exception", e)
        }
    }
    
    /**
     * Run just the data verification without seeding
     */
    fun verifyExistingData() {
        Log.d(TAG, "=== Verifying Existing Catalogue Data ===")
        SeedCatalogueRunner.verifySeedData(context)
    }
}