package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*

/**
 * Construction Catalogue Seeder
 * 
 * Script to seed the database with initial catalogue data
 * Based on the comprehensive construction data structure from the JavaScript seeding example
 */
class CatalogueSeeder(private val catalogueService: EnhancedCatalogueDataService) {
    private val TAG = "CatalogueSeeder"
    
    /**
     * Main seeding function that populates the entire construction catalogue
     */
    suspend fun seedCatalogue(): Result<String> {
        return try {
            Log.d(TAG, "Starting construction catalogue seeding...")
            
            // Create categories
            val categories = createCategories()
            if (categories.isFailure) {
                return Result.failure(categories.exceptionOrNull()!!)
            }
            val categoryList = categories.getOrThrow()
            
            // Create trades, scopes, assemblies for each category
            createInteriorFinishes(categoryList.find { it.name == "Interior Finishes" }?.id!!)
            createPlumbing(categoryList.find { it.name == "Plumbing" }?.id!!)
            createElectrical(categoryList.find { it.name == "Electrical" }?.id!!)
            
            Log.d(TAG, "Catalogue seeding completed successfully")
            Result.success("Catalogue seeding completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding catalogue: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create all construction categories
     */
    private suspend fun createCategories(): Result<List<Category>> {
        Log.d(TAG, "Creating categories...")
        
        val categoryData = listOf(
            mapOf(
                "name" to "Pre-Construction",
                "description" to "Activities before main construction begins",
                "sequence" to 1,
                "isActive" to true
            ),
            mapOf(
                "name" to "Foundation",
                "description" to "Structural foundation systems",
                "sequence" to 2,
                "isActive" to true
            ),
            mapOf(
                "name" to "Structural",
                "description" to "Framing and structural elements",
                "sequence" to 3,
                "isActive" to true
            ),
            mapOf(
                "name" to "Exterior Envelope",
                "description" to "Exterior elements and weatherproofing",
                "sequence" to 4,
                "isActive" to true
            ),
            mapOf(
                "name" to "Plumbing",
                "description" to "Plumbing systems and fixtures",
                "sequence" to 5,
                "isActive" to true
            ),
            mapOf(
                "name" to "HVAC",
                "description" to "Heating, ventilation, and air conditioning",
                "sequence" to 6,
                "isActive" to true
            ),
            mapOf(
                "name" to "Electrical",
                "description" to "Electrical systems and fixtures",
                "sequence" to 7,
                "isActive" to true
            ),
            mapOf(
                "name" to "Interior Finishes",
                "description" to "Interior finishing and trim work",
                "sequence" to 8,
                "isActive" to true
            ),
            mapOf(
                "name" to "Specialty Areas",
                "description" to "Specialty rooms and features",
                "sequence" to 9,
                "isActive" to true
            ),
            mapOf(
                "name" to "Outdoor Spaces",
                "description" to "Outdoor structures and landscaping",
                "sequence" to 10,
                "isActive" to true
            )
        )
        
        val categories = mutableListOf<Category>()
        
        for (data in categoryData) {
            val result = catalogueService.createCategory(
                name = data["name"] as String,
                description = data["description"] as String,
                sequence = data["sequence"] as Int,
                imageUrl = null
            )
            if (result.isSuccess) {
                val category = result.getOrThrow()
                categories.add(category)
                Log.d(TAG, "Created category: ${category.name}")
            } else {
                return Result.failure(result.exceptionOrNull()!!)
            }
        }
        
        return Result.success(categories)
    }
    
    /**
     * Create interior finishes category data
     */
    private suspend fun createInteriorFinishes(categoryId: String) {
        Log.d(TAG, "Creating Interior Finishes category data...")
        
        // Create Finish Carpentry trade
        val finishCarpentryResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Finish Carpentry",
            description = "Fine woodwork and detailed carpentry",
            sequence = 1,
            imageUrl = null
        )
        val finishCarpentryTrade = finishCarpentryResult.getOrThrow()
        
        // Create Stair Construction scope
        val stairScopeResult = catalogueService.createScope(
            tradeId = finishCarpentryTrade.id,
            name = "Stair Construction",
            description = "Building and finishing interior staircases",
            sequence = 1,
            imageUrl = null
        )
        val stairScope = stairScopeResult.getOrThrow()
        
        // Create Standard Staircase assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to stairScope.id,
                "name" to "Standard Staircase",
                "description" to "Standard wooden staircase with handrail and balusters",
                "sequence" to 1,
                "unit" to "flight",
                "laborHours" to 24.0,
                "materialCost" to 1250.0,
                "laborCost" to 1200.0,
                "equipmentCost" to 150.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 50.0,
                "totalCost" to 2650.0,
                "markupPercentage" to 0.15,
                "notes" to "Assumes standard 12-14 step staircase with oak treads and painted risers",
                "tags" to listOf("stair", "wood", "interior", "carpentry"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Layout stair dimensions",
                    "description" to "Measure and mark stair locations and dimensions",
                    "sequence" to 1,
                    "laborHours" to 2.0,
                    "materialCost" to 0.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Verify measurements against building code requirements",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Cut stringers",
                    "description" to "Cut and prepare stair stringers",
                    "sequence" to 2,
                    "laborHours" to 4.0,
                    "materialCost" to 180.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 50.0,
                    "notes" to "Use 2x12 pressure treated lumber for stringers",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install stringers",
                    "description" to "Install and secure stair stringers",
                    "sequence" to 3,
                    "laborHours" to 3.0,
                    "materialCost" to 40.0,
                    "laborCost" to 150.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Secure with lag bolts and joist hangers",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Stair Stringers",
                    "description" to "2x12 pressure treated lumber",
                    "quantity" to 3.0,
                    "unit" to "each",
                    "unitCost" to 60.0,
                    "totalCost" to 180.0,
                    "waste" to 10.0,
                    "notes" to "Length depends on stair rise and run",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Stair Risers",
                    "description" to "1x8 pine boards",
                    "quantity" to 14.0,
                    "unit" to "each",
                    "unitCost" to 8.50,
                    "totalCost" to 119.0,
                    "waste" to 5.0,
                    "notes" to "Pre-primed boards recommended",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Interior Finishes data")
    }
    
    /**
     * Create plumbing category data
     */
    private suspend fun createPlumbing(categoryId: String) {
        Log.d(TAG, "Creating Plumbing category data...")
        
        // Create Finish Plumbing trade
        val finishPlumbingResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Finish Plumbing",
            description = "Installation of plumbing fixtures and connections",
            sequence = 1,
            imageUrl = null
        )
        val finishPlumbingTrade = finishPlumbingResult.getOrThrow()
        
        // Create Bathroom Fixture Installation scope
        val bathroomFixtureResult = catalogueService.createScope(
            tradeId = finishPlumbingTrade.id,
            name = "Bathroom Fixture Installation",
            description = "Installation of toilets, sinks, and other bathroom fixtures",
            sequence = 1,
            imageUrl = null
        )
        val bathroomFixtureScope = bathroomFixtureResult.getOrThrow()
        
        // Create Toilet Installation assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to bathroomFixtureScope.id,
                "name" to "Toilet Installation",
                "description" to "Standard toilet installation including wax ring and supply line",
                "sequence" to 1,
                "unit" to "each",
                "laborHours" to 1.5,
                "materialCost" to 325.0,
                "laborCost" to 75.0,
                "equipmentCost" to 0.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 0.0,
                "totalCost" to 400.0,
                "markupPercentage" to 0.15,
                "notes" to "Includes standard two-piece toilet with seat",
                "tags" to listOf("plumbing", "bathroom", "toilet", "fixture"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install wax ring",
                    "description" to "Apply wax ring to toilet flange",
                    "sequence" to 1,
                    "laborHours" to 0.25,
                    "materialCost" to 8.0,
                    "laborCost" to 12.50,
                    "equipmentCost" to 0.0,
                    "notes" to "Ensure flange is clean before applying",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Set toilet on flange",
                    "description" to "Position toilet on wax ring and flange",
                    "sequence" to 2,
                    "laborHours" to 0.25,
                    "materialCost" to 0.0,
                    "laborCost" to 12.50,
                    "equipmentCost" to 0.0,
                    "notes" to "Align bolt holes with flange bolts",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Toilet",
                    "description" to "Two-piece toilet",
                    "quantity" to 1.0,
                    "unit" to "each",
                    "unitCost" to 250.0,
                    "totalCost" to 250.0,
                    "waste" to 0.0,
                    "notes" to "Standard height, 1.28 GPF",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Wax Ring",
                    "description" to "Toilet wax ring with sleeve",
                    "quantity" to 1.0,
                    "unit" to "each",
                    "unitCost" to 8.0,
                    "totalCost" to 8.0,
                    "waste" to 0.0,
                    "notes" to "With plastic sleeve for better seal",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Plumbing data")
    }
    
    /**
     * Create electrical category data
     */
    private suspend fun createElectrical(categoryId: String) {
        Log.d(TAG, "Creating Electrical category data...")
        
        // Create Finish Electrical trade
        val finishElectricalResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Finish Electrical",
            description = "Installation of electrical fixtures and devices",
            sequence = 1,
            imageUrl = null
        )
        val finishElectricalTrade = finishElectricalResult.getOrThrow()
        
        // Create Room Device Installation scope
        val roomDeviceResult = catalogueService.createScope(
            tradeId = finishElectricalTrade.id,
            name = "Room Device Installation",
            description = "Installation of electrical devices and fixtures by room",
            sequence = 1,
            imageUrl = null
        )
        val roomDeviceScope = roomDeviceResult.getOrThrow()
        
        // Create Bedroom Devices assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to roomDeviceScope.id,
                "name" to "Bedroom Electrical Devices",
                "description" to "Installation of all electrical devices for a standard bedroom",
                "sequence" to 1,
                "unit" to "room",
                "laborHours" to 3.5,
                "materialCost" to 215.0,
                "laborCost" to 175.0,
                "equipmentCost" to 0.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 0.0,
                "totalCost" to 390.0,
                "markupPercentage" to 0.15,
                "notes" to "Includes switches, receptacles, ceiling fan/light, and smoke detector for standard bedroom",
                "tags" to listOf("electrical", "bedroom", "devices", "fixtures"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install light switches",
                    "description" to "Install single-pole and three-way switches",
                    "sequence" to 1,
                    "laborHours" to 0.5,
                    "materialCost" to 15.0,
                    "laborCost" to 25.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Typically 2 switches per bedroom",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install receptacles",
                    "description" to "Install electrical outlets",
                    "sequence" to 2,
                    "laborHours" to 1.5,
                    "materialCost" to 60.0,
                    "laborCost" to 75.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Typically 6 receptacles per bedroom",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Light Switches",
                    "description" to "Single-pole and three-way switches",
                    "quantity" to 2.0,
                    "unit" to "each",
                    "unitCost" to 3.50,
                    "totalCost" to 7.0,
                    "waste" to 0.0,
                    "notes" to "White, decora style",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Receptacles",
                    "description" to "Standard 15A receptacles",
                    "quantity" to 6.0,
                    "unit" to "each",
                    "unitCost" to 3.50,
                    "totalCost" to 21.0,
                    "waste" to 0.0,
                    "notes" to "White, decora style",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Electrical data")
    }
}