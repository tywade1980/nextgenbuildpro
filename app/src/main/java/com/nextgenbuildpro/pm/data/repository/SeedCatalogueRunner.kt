package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking

/**
 * Seed Catalogue Runner
 * 
 * Executable class to run the catalogue seeding process
 * This is the Kotlin equivalent of the JavaScript "seedCatalogue()" function call
 */
object SeedCatalogueRunner {
    private val TAG = "SeedCatalogueRunner"
    
    /**
     * Run the catalogue seeding process
     */
    fun runSeeding(context: Context): Boolean {
        return try {
            Log.d(TAG, "=== Starting Construction Catalogue Seeding ===")
            
            val catalogueService = EnhancedCatalogueDataService(context)
            val seeder = CatalogueSeeder(catalogueService)
            
            runBlocking {
                val result = seeder.seedCatalogue()
                if (result.isSuccess) {
                    Log.d(TAG, "=== Catalogue Seeding Completed Successfully ===")
                    Log.d(TAG, result.getOrThrow())
                    true
                } else {
                    Log.e(TAG, "=== Catalogue Seeding Failed ===")
                    Log.e(TAG, "Error: ${result.exceptionOrNull()?.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "=== Catalogue Seeding Failed with Exception ===", e)
            false
        }
    }
    
    /**
     * Verify seeded data by logging summary statistics
     */
    fun verifySeedData(context: Context) {
        try {
            Log.d(TAG, "=== Verifying Seeded Catalogue Data ===")
            
            val catalogueService = EnhancedCatalogueDataService(context)
            
            runBlocking {
                // Get summary statistics
                val categoriesResult = catalogueService.getCategoriesWithChildren()
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrThrow()
                    
                    Log.d(TAG, "Verification Results:")
                    Log.d(TAG, "  Total Categories: ${categories.size}")
                    
                    var totalTrades = 0
                    var totalScopes = 0
                    var totalAssemblies = 0
                    var totalTasks = 0
                    var totalMaterials = 0

                    for (categoryWithChildren in categories) {
                        val categoryTrades = categoryWithChildren.trades.size
                        totalTrades += categoryTrades

                        for (tradeWithChildren in categoryWithChildren.trades) {
                            val tradeScopes = tradeWithChildren.scopes.size
                            totalScopes += tradeScopes

                            for (scopeWithChildren in tradeWithChildren.scopes) {
                                val scopeAssemblies = scopeWithChildren.assemblies.size
                                totalAssemblies += scopeAssemblies

                                for (assemblyWithChildren in scopeWithChildren.assemblies) {
                                    totalTasks += assemblyWithChildren.tasks.size
                                    totalMaterials += assemblyWithChildren.materials.size

                                    // Count materials in tasks
                                    for (taskWithMaterials in assemblyWithChildren.tasks) {
                                        totalMaterials += taskWithMaterials.materials.size
                                    }
                                }
                            }
                        }

                        Log.d(TAG, "  Category '${categoryWithChildren.category.name}': ${categoryTrades} trades")
                    }
                    
                    Log.d(TAG, "  Total Trades: $totalTrades")
                    Log.d(TAG, "  Total Scopes: $totalScopes")
                    Log.d(TAG, "  Total Assemblies: $totalAssemblies")
                    Log.d(TAG, "  Total Tasks: $totalTasks")
                    Log.d(TAG, "  Total Materials: $totalMaterials")
                    
                    Log.d(TAG, "=== Catalogue Data Verification Complete ===")
                } else {
                    Log.e(TAG, "Failed to verify seeded data: ${categoriesResult.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during data verification", e)
        }
    }
}