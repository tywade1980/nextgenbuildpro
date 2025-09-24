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
            createFoundation(categoryList.find { it.name == "Foundation" }?.id!!)
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
    
    /**
     * Create foundation category data with comprehensive basement construction assemblies
     */
    private suspend fun createFoundation(categoryId: String) {
        Log.d(TAG, "Creating Foundation category data...")
        
        // Create Concrete trade
        val concreteResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Concrete",
            description = "Foundation systems and concrete construction",
            sequence = 1,
            imageUrl = null
        )
        val concreteTrade = concreteResult.getOrThrow()
        
        // Create Basement Construction scope
        val basementConstructionResult = catalogueService.createScope(
            tradeId = concreteTrade.id,
            name = "Basement Construction",
            description = "Complete basement foundation construction from excavation to backfill",
            sequence = 1,
            imageUrl = null
        )
        val basementConstructionScope = basementConstructionResult.getOrThrow()
        
        // Create Basement Finishing scope
        val basementFinishingResult = catalogueService.createScope(
            tradeId = concreteTrade.id,
            name = "Basement Finishing",
            description = "Interior basement framing and finishing work",
            sequence = 2,
            imageUrl = null
        )
        val basementFinishingScope = basementFinishingResult.getOrThrow()
        
        // Create Full Basement Foundation assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementConstructionScope.id,
                "name" to "Full Basement Foundation",
                "description" to "Complete basement foundation construction including excavation, footings, walls, and waterproofing",
                "sequence" to 1,
                "unit" to "each",
                "laborHours" to 68.0,
                "materialCost" to 12500.0,
                "laborCost" to 2720.0,
                "equipmentCost" to 1800.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 480.0,
                "totalCost" to 17500.0,
                "markupPercentage" to 0.20,
                "notes" to "Comprehensive basement foundation system from excavation through backfill",
                "tags" to listOf("foundation", "basement", "excavation", "concrete", "waterproofing"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Excavate to required depth",
                    "description" to "Excavate basement area to specified depth with proper slope and drainage considerations",
                    "sequence" to 1,
                    "laborHours" to 8.0,
                    "materialCost" to 0.0,
                    "laborCost" to 320.0,
                    "equipmentCost" to 480.0,
                    "notes" to "Typically 8-9 feet below grade with proper drainage slope",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install footing drains",
                    "description" to "Install perimeter drainage system around foundation footings",
                    "sequence" to 2,
                    "laborHours" to 6.0,
                    "materialCost" to 1500.0,
                    "laborCost" to 240.0,
                    "equipmentCost" to 0.0,
                    "notes" to "4-inch perforated drain pipe with gravel and filter fabric",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Form and pour footings",
                    "description" to "Install footing forms and pour concrete footings to support foundation walls",
                    "sequence" to 3,
                    "laborHours" to 12.0,
                    "materialCost" to 3450.0,
                    "laborCost" to 480.0,
                    "equipmentCost" to 200.0,
                    "notes" to "Typically 20 inch wide by 8 inch deep footings with rebar",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Form basement walls",
                    "description" to "Install forming system for basement concrete walls",
                    "sequence" to 4,
                    "laborHours" to 14.0,
                    "materialCost" to 8160.0,
                    "laborCost" to 560.0,
                    "equipmentCost" to 300.0,
                    "notes" to "Wall forms with proper bracing and embedments",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install steel reinforcement",
                    "description" to "Place reinforcement steel in wall forms according to structural plans",
                    "sequence" to 5,
                    "laborHours" to 8.0,
                    "materialCost" to 2125.0,
                    "laborCost" to 320.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Typically #4 bars at 12 inches on center each way",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Pour concrete walls",
                    "description" to "Place concrete in wall forms using proper techniques and equipment",
                    "sequence" to 6,
                    "laborHours" to 8.0,
                    "materialCost" to 3780.0,
                    "laborCost" to 320.0,
                    "equipmentCost" to 600.0,
                    "notes" to "3500 PSI concrete with air entrainment, 28 cubic yards",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Strip forms",
                    "description" to "Remove concrete forms after proper curing period",
                    "sequence" to 7,
                    "laborHours" to 4.0,
                    "materialCost" to 480.0,
                    "laborCost" to 160.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Remove after 24-48 hours minimum curing time",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Apply waterproofing membrane",
                    "description" to "Install exterior basement wall waterproofing system",
                    "sequence" to 8,
                    "laborHours" to 10.0,
                    "materialCost" to 4080.0,
                    "laborCost" to 400.0,
                    "equipmentCost" to 120.0,
                    "notes" to "Liquid applied membrane with primer and sealants",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install drainage board",
                    "description" to "Install exterior drainage and protection board system",
                    "sequence" to 9,
                    "laborHours" to 6.0,
                    "materialCost" to 2640.0,
                    "laborCost" to 240.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Dimpled drainage board over waterproofing membrane",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install window wells",
                    "description" to "Install basement window wells and drainage systems",
                    "sequence" to 10,
                    "laborHours" to 3.0,
                    "materialCost" to 740.0,
                    "laborCost" to 120.0,
                    "equipmentCost" to 100.0,
                    "notes" to "4 galvanized steel window wells with covers and drainage",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Backfill foundation",
                    "description" to "Backfill around foundation walls with proper compaction",
                    "sequence" to 11,
                    "laborHours" to 6.0,
                    "materialCost" to 1800.0,
                    "laborCost" to 240.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Compacted backfill in 12-inch lifts with proper slope",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Concrete - Footings",
                    "description" to "3000 PSI concrete for footings",
                    "quantity" to 12.0,
                    "unit" to "cubic yard",
                    "unitCost" to 125.0,
                    "totalCost" to 1500.0,
                    "waste" to 5.0,
                    "notes" to "Includes delivery and placement",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Concrete - Walls",
                    "description" to "3500 PSI concrete for basement walls",
                    "quantity" to 28.0,
                    "unit" to "cubic yard",
                    "unitCost" to 135.0,
                    "totalCost" to 3780.0,
                    "waste" to 3.0,
                    "notes" to "High-strength concrete with air entrainment",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Rebar #4",
                    "description" to "Grade 60 deformed reinforcing steel",
                    "quantity" to 5000.0,
                    "unit" to "linear foot",
                    "unitCost" to 1.25,
                    "totalCost" to 6250.0,
                    "waste" to 10.0,
                    "notes" to "For wall and footing reinforcement",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Waterproofing System",
                    "description" to "Complete waterproofing membrane system",
                    "quantity" to 1.0,
                    "unit" to "system",
                    "unitCost" to 4080.0,
                    "totalCost" to 4080.0,
                    "waste" to 5.0,
                    "notes" to "Includes membrane, primer, and sealants",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Drainage Materials",
                    "description" to "Perimeter drainage system components",
                    "quantity" to 1.0,
                    "unit" to "system",
                    "unitCost" to 1500.0,
                    "totalCost" to 1500.0,
                    "waste" to 5.0,
                    "notes" to "Includes pipe, gravel, and filter fabric",
                    "isActive" to true
                )
            )
        )
        
        // Create Basement Floor assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementConstructionScope.id,
                "name" to "Basement Floor",
                "description" to "Complete basement floor construction including base preparation, utilities, reinforcement, and concrete placement",
                "sequence" to 2,
                "unit" to "square foot",
                "laborHours" to 42.0,
                "materialCost" to 8500.0,
                "laborCost" to 1680.0,
                "equipmentCost" to 480.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 290.0,
                "totalCost" to 11750.0,
                "markupPercentage" to 0.18,
                "notes" to "Complete basement floor system including radon mitigation and radiant heat preparation",
                "tags" to listOf("basement", "floor", "slab", "concrete", "insulation"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install radon mitigation system",
                    "description" to "Install sub-slab radon mitigation piping system",
                    "sequence" to 1,
                    "laborHours" to 8.0,
                    "materialCost" to 250.0,
                    "laborCost" to 320.0,
                    "equipmentCost" to 0.0,
                    "notes" to "4-inch PVC piping with proper connections and sealing",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Place 4\" stone base",
                    "description" to "Install 4-inch crushed stone base under basement slab",
                    "sequence" to 2,
                    "laborHours" to 4.0,
                    "materialCost" to 336.0,
                    "laborCost" to 160.0,
                    "equipmentCost" to 60.0,
                    "notes" to "3/4-inch crushed stone, 12 cubic yards",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Compact stone base",
                    "description" to "Compact stone base to proper density for slab support",
                    "sequence" to 3,
                    "laborHours" to 2.0,
                    "materialCost" to 0.0,
                    "laborCost" to 80.0,
                    "equipmentCost" to 120.0,
                    "notes" to "Achieve 95% compaction per specifications",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install vapor barrier",
                    "description" to "Install vapor barrier over stone base under concrete slab",
                    "sequence" to 4,
                    "laborHours" to 2.0,
                    "materialCost" to 868.0,
                    "laborCost" to 80.0,
                    "equipmentCost" to 0.0,
                    "notes" to "6-mil polyethylene with sealed seams",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install perimeter insulation",
                    "description" to "Install rigid insulation around basement slab perimeter",
                    "sequence" to 5,
                    "laborHours" to 3.0,
                    "materialCost" to 1020.0,
                    "laborCost" to 120.0,
                    "equipmentCost" to 0.0,
                    "notes" to "2-inch XPS foam board with adhesive",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install radiant heat (if applicable)",
                    "description" to "Install in-floor radiant heating system in basement slab",
                    "sequence" to 6,
                    "laborHours" to 6.0,
                    "materialCost" to 4865.0,
                    "laborCost" to 240.0,
                    "equipmentCost" to 0.0,
                    "notes" to "PEX tubing system with manifolds - optional upgrade",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install wire mesh/rebar",
                    "description" to "Install reinforcement mesh or rebar in basement slab",
                    "sequence" to 7,
                    "laborHours" to 3.0,
                    "materialCost" to 1610.0,
                    "laborCost" to 120.0,
                    "equipmentCost" to 0.0,
                    "notes" to "6x6 W2.9xW2.9 welded wire mesh with support chairs",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Pour concrete slab",
                    "description" to "Place concrete for basement floor slab",
                    "sequence" to 8,
                    "laborHours" to 4.0,
                    "materialCost" to 1430.0,
                    "laborCost" to 160.0,
                    "equipmentCost" to 200.0,
                    "notes" to "4-inch thick slab, 11 cubic yards of 3000 PSI concrete",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Float and finish concrete",
                    "description" to "Float and finish basement slab surface",
                    "sequence" to 9,
                    "laborHours" to 3.0,
                    "materialCost" to 0.0,
                    "laborCost" to 120.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Steel trowel finish suitable for floor coverings",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Cut control joints",
                    "description" to "Cut control joints in basement slab to control cracking",
                    "sequence" to 10,
                    "laborHours" to 2.0,
                    "materialCost" to 145.0,
                    "laborCost" to 80.0,
                    "equipmentCost" to 100.0,
                    "notes" to "15-foot maximum spacing with diamond saw",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Cure concrete",
                    "description" to "Properly cure basement concrete slab",
                    "sequence" to 11,
                    "laborHours" to 1.0,
                    "materialCost" to 218.0,
                    "laborCost" to 40.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Curing compound and plastic sheeting for 7 days minimum",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Basement Slab Concrete",
                    "description" to "3000 PSI concrete for basement floor",
                    "quantity" to 11.0,
                    "unit" to "cubic yard",
                    "unitCost" to 130.0,
                    "totalCost" to 1430.0,
                    "waste" to 3.0,
                    "notes" to "4-inch thick slab over prepared base",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Radiant Heat System",
                    "description" to "Complete radiant floor heating system",
                    "quantity" to 1.0,
                    "unit" to "system",
                    "unitCost" to 4865.0,
                    "totalCost" to 4865.0,
                    "waste" to 5.0,
                    "notes" to "PEX tubing, manifolds, and controls for 900 SF",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Reinforcement Package",
                    "description" to "Wire mesh and support system",
                    "quantity" to 1.0,
                    "unit" to "package",
                    "unitCost" to 1610.0,
                    "totalCost" to 1610.0,
                    "waste" to 8.0,
                    "notes" to "Includes mesh, chairs, and fasteners",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Insulation System",
                    "description" to "Perimeter insulation and vapor barrier",
                    "quantity" to 1.0,
                    "unit" to "system",
                    "unitCost" to 1888.0,
                    "totalCost" to 1888.0,
                    "waste" to 5.0,
                    "notes" to "XPS foam board and polyethylene barrier",
                    "isActive" to true
                )
            )
        )
        
        // Create Framed Basement Walls assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementFinishingScope.id,
                "name" to "Framed Basement Walls",
                "description" to "Interior framed walls for basement finishing including insulation and vapor barrier",
                "sequence" to 1,
                "unit" to "linear foot",
                "laborHours" to 28.0,
                "materialCost" to 3200.0,
                "laborCost" to 700.0,
                "equipmentCost" to 0.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 180.0,
                "totalCost" to 4500.0,
                "markupPercentage" to 0.18,
                "notes" to "Complete interior wall framing system for basement finishing",
                "tags" to listOf("basement", "framing", "walls", "insulation", "finishing"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Layout wall locations",
                    "description" to "Layout interior basement wall locations on slab",
                    "sequence" to 1,
                    "laborHours" to 2.0,
                    "materialCost" to 0.0,
                    "laborCost" to 50.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Mark wall locations with chalk lines per plans",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install bottom plate (pressure treated)",
                    "description" to "Install pressure-treated bottom plate on basement slab",
                    "sequence" to 2,
                    "laborHours" to 4.0,
                    "materialCost" to 720.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 0.0,
                    "notes" to "PT 2x4 with concrete fasteners and sealant",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install top plate",
                    "description" to "Install top plate for basement wall framing",
                    "sequence" to 3,
                    "laborHours" to 5.0,
                    "materialCost" to 680.0,
                    "laborCost" to 125.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Double 2x4 top plate system",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install studs 16\" O.C.",
                    "description" to "Install wall studs at 16 inches on center spacing",
                    "sequence" to 4,
                    "laborHours" to 8.0,
                    "materialCost" to 780.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 0.0,
                    "notes" to "120 studs at 2x4x8' with framing nails",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Frame door openings",
                    "description" to "Frame rough openings for basement doors",
                    "sequence" to 5,
                    "laborHours" to 3.0,
                    "materialCost" to 105.0,
                    "laborCost" to 75.0,
                    "equipmentCost" to 0.0,
                    "notes" to "3 door openings with headers and king studs",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Frame utilities chases",
                    "description" to "Frame chases and enclosures for utilities in basement walls",
                    "sequence" to 6,
                    "laborHours" to 2.0,
                    "materialCost" to 272.0,
                    "laborCost" to 50.0,
                    "equipmentCost" to 0.0,
                    "notes" to "6 utility chases with access panels",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install blocking for fixtures",
                    "description" to "Install blocking for wall-mounted fixtures and equipment",
                    "sequence" to 7,
                    "laborHours" to 2.0,
                    "materialCost" to 185.0,
                    "laborCost" to 50.0,
                    "equipmentCost" to 0.0,
                    "notes" to "12 blocking locations for TVs, shelving, and fixtures",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install wall insulation",
                    "description" to "Install insulation in basement wall cavities",
                    "sequence" to 8,
                    "laborHours" to 1.5,
                    "materialCost" to 1925.0,
                    "laborCost" to 38.0,
                    "equipmentCost" to 0.0,
                    "notes" to "R-13 fiberglass batts with vapor retarder",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install vapor barrier",
                    "description" to "Install vapor barrier over insulated basement walls",
                    "sequence" to 9,
                    "laborHours" to 0.5,
                    "materialCost" to 722.5,
                    "laborCost" to 12.0,
                    "equipmentCost" to 0.0,
                    "notes" to "6-mil polyethylene with sealed seams",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Framing Lumber Package",
                    "description" to "Complete lumber package for basement walls",
                    "quantity" to 1.0,
                    "unit" to "package",
                    "unitCost" to 1685.0,
                    "totalCost" to 1685.0,
                    "waste" to 10.0,
                    "notes" to "Includes plates, studs, headers, and blocking",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Insulation System",
                    "description" to "R-13 fiberglass insulation with vapor barrier",
                    "quantity" to 1280.0,
                    "unit" to "square foot",
                    "unitCost" to 1.50,
                    "totalCost" to 1920.0,
                    "waste" to 5.0,
                    "notes" to "Kraft-faced batts for 1280 SF of wall area",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Fasteners and Hardware",
                    "description" to "Nails, screws, and hardware for framing",
                    "quantity" to 1.0,
                    "unit" to "package",
                    "unitCost" to 320.0,
                    "totalCost" to 320.0,
                    "waste" to 5.0,
                    "notes" to "Includes framing nails, concrete anchors, and misc hardware",
                    "isActive" to true
                )
            )
        )
        
        // Create Basement Ceiling assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementFinishingScope.id,
                "name" to "Basement Ceiling",
                "description" to "Complete basement ceiling system with sound insulation, drywall, and painted finish",
                "sequence" to 2,
                "unit" to "square foot",
                "laborHours" to 18.0,
                "materialCost" to 2500.0,
                "laborCost" to 450.0,
                "equipmentCost" to 120.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 130.0,
                "totalCost" to 3500.0,
                "markupPercentage" to 0.20,
                "notes" to "Complete ceiling system with sound control and quality finish",
                "tags" to listOf("basement", "ceiling", "drywall", "insulation", "paint"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install sound insulation",
                    "description" to "Install sound insulation between floor joists above basement",
                    "sequence" to 1,
                    "laborHours" to 2.5,
                    "materialCost" to 1598.0,
                    "laborCost" to 62.5,
                    "equipmentCost" to 0.0,
                    "notes" to "R-19 fiberglass batts with wire supports",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install resilient channels",
                    "description" to "Install resilient metal channels for sound isolation",
                    "sequence" to 2,
                    "laborHours" to 3.0,
                    "materialCost" to 1040.0,
                    "laborCost" to 75.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Metal channels at 24 inches on center",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install drywall",
                    "description" to "Install drywall on basement ceiling using resilient channels",
                    "sequence" to 3,
                    "laborHours" to 4.0,
                    "materialCost" to 1592.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 60.0,
                    "notes" to "5/8 inch fire-rated drywall with fine thread screws",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Tape and finish drywall",
                    "description" to "Tape and finish basement ceiling drywall",
                    "sequence" to 4,
                    "laborHours" to 5.0,
                    "materialCost" to 258.0,
                    "laborCost" to 125.0,
                    "equipmentCost" to 30.0,
                    "notes" to "Three coats with sanding for smooth finish",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install ceiling access panels",
                    "description" to "Install access panels for utilities and maintenance",
                    "sequence" to 5,
                    "laborHours" to 1.0,
                    "materialCost" to 270.0,
                    "laborCost" to 25.0,
                    "equipmentCost" to 0.0,
                    "notes" to "3 access panels at 24x24 inches minimum",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Prime and paint ceiling",
                    "description" to "Prime and paint basement ceiling with quality finish",
                    "sequence" to 6,
                    "laborHours" to 2.5,
                    "materialCost" to 245.0,
                    "laborCost" to 62.5,
                    "equipmentCost" to 30.0,
                    "notes" to "High-quality primer and two coats of ceiling paint",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Ceiling Insulation",
                    "description" to "R-19 fiberglass batts for sound control",
                    "quantity" to 900.0,
                    "unit" to "square foot",
                    "unitCost" to 1.65,
                    "totalCost" to 1485.0,
                    "waste" to 5.0,
                    "notes" to "Includes wire supports for installation",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Drywall System",
                    "description" to "5/8 inch fire-rated drywall with resilient channels",
                    "quantity" to 900.0,
                    "unit" to "square foot",
                    "unitCost" to 2.95,
                    "totalCost" to 2655.0,
                    "waste" to 8.0,
                    "notes" to "Includes channels, drywall, screws, and finishing materials",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Paint System",
                    "description" to "Complete paint system for ceiling",
                    "quantity" to 1.0,
                    "unit" to "system",
                    "unitCost" to 245.0,
                    "totalCost" to 245.0,
                    "waste" to 5.0,
                    "notes" to "Primer and paint for 900 SF ceiling",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Foundation data")
    }
}