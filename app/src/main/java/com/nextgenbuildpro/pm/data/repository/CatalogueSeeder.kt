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
            createStructural(categoryList.find { it.name == "Structural" }?.id!!)
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
     * Create structural category data
     */
    private suspend fun createStructural(categoryId: String) {
        Log.d(TAG, "Creating Structural category data...")
        
        // Create Framing trade
        val framingResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Framing",
            description = "Wood and steel framing systems",
            sequence = 1,
            imageUrl = null
        )
        val framingTrade = framingResult.getOrThrow()
        
        // Create Wall Framing scope
        val wallFramingResult = catalogueService.createScope(
            tradeId = framingTrade.id,
            name = "Wall Framing",
            description = "Interior and exterior wall framing systems",
            sequence = 1,
            imageUrl = null
        )
        val wallFramingScope = wallFramingResult.getOrThrow()
        
        // Create Framing Assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to wallFramingScope.id,
                "name" to "Framing Assembly",
                "description" to "Standard wall framing with studs",
                "sequence" to 1,
                "unit" to "LF",
                "laborHours" to 3.0,
                "materialCost" to 90.0,  // 60% of $150
                "laborCost" to 45.0,     // $45 for 3 hours at $15/hour
                "equipmentCost" to 15.0,  // 10% of $150
                "subcontractorCost" to 0.0,
                "otherCost" to 0.0,
                "totalCost" to 150.0,
                "markupPercentage" to 0.15,
                "notes" to "Standard 16\" OC wall framing with 2x4 or 2x6 studs",
                "tags" to listOf("framing", "walls", "structural", "studs"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Layout wall",
                    "description" to "Mark stud locations and openings",
                    "sequence" to 1,
                    "laborHours" to 0.5,
                    "materialCost" to 5.0,
                    "laborCost" to 7.50,
                    "equipmentCost" to 2.50,
                    "notes" to "Use chalk line and square for accuracy",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Cut studs",
                    "description" to "Cut studs to proper length",
                    "sequence" to 2,
                    "laborHours" to 1.0,
                    "materialCost" to 40.0,
                    "laborCost" to 15.0,
                    "equipmentCost" to 7.50,
                    "notes" to "Account for plates and header height",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install framing",
                    "description" to "Install plates, studs, and headers",
                    "sequence" to 3,
                    "laborHours" to 1.5,
                    "materialCost" to 45.0,
                    "laborCost" to 22.50,
                    "equipmentCost" to 5.0,
                    "notes" to "Use proper fasteners and check for plumb",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "2x4 Studs",
                    "description" to "Standard construction grade 2x4 studs",
                    "quantity" to 6.0,
                    "unit" to "each",
                    "unitCost" to 8.0,
                    "totalCost" to 48.0,
                    "waste" to 0.1,
                    "notes" to "8 foot lengths for standard ceiling height",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Top/Bottom Plates",
                    "description" to "2x4 lumber for top and bottom plates",
                    "quantity" to 2.0,
                    "unit" to "each",
                    "unitCost" to 8.0,
                    "totalCost" to 16.0,
                    "waste" to 0.05,
                    "notes" to "Use treated lumber for bottom plate on concrete",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Fasteners",
                    "description" to "Nails and screws for framing",
                    "quantity" to 5.0,
                    "unit" to "lbs",
                    "unitCost" to 5.20,
                    "totalCost" to 26.0,
                    "waste" to 0.0,
                    "notes" to "16d common nails and 3\" construction screws",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Structural data")
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
        
        // Create Rough Plumbing trade
        val roughPlumbingResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Rough Plumbing",
            description = "Plumbing rough-in and supply line installation",
            sequence = 2,
            imageUrl = null
        )
        val roughPlumbingTrade = roughPlumbingResult.getOrThrow()
        
        // Create Plumbing Rough-in scope
        val plumbingRoughInResult = catalogueService.createScope(
            tradeId = roughPlumbingTrade.id,
            name = "Plumbing Rough-in",
            description = "Standard plumbing rough-in installation including supply and drain lines",
            sequence = 1,
            imageUrl = null
        )
        val plumbingRoughInScope = plumbingRoughInResult.getOrThrow()
        
        // Create Plumbing Rough-in assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to plumbingRoughInScope.id,
                "name" to "Plumbing Rough-in",
                "description" to "Standard plumbing rough-in",
                "sequence" to 1,
                "unit" to "bathroom",
                "laborHours" to 6.0,
                "materialCost" to 180.0,  // 60% of $300
                "laborCost" to 90.0,      // 6 hours at $15/hour
                "equipmentCost" to 30.0,  // 10% of $300
                "subcontractorCost" to 0.0,
                "otherCost" to 0.0,
                "totalCost" to 300.0,
                "markupPercentage" to 0.15,
                "notes" to "Includes supply lines, drain lines, and vent installation for standard bathroom",
                "tags" to listOf("plumbing", "rough-in", "supply", "drain", "vent"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install supply lines",
                    "description" to "Run hot and cold water supply lines",
                    "sequence" to 1,
                    "laborHours" to 2.0,
                    "materialCost" to 60.0,
                    "laborCost" to 30.0,
                    "equipmentCost" to 10.0,
                    "notes" to "Use PEX or copper tubing with proper fittings",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install drain lines",
                    "description" to "Install waste and drain piping",
                    "sequence" to 2,
                    "laborHours" to 3.0,
                    "materialCost" to 80.0,
                    "laborCost" to 45.0,
                    "equipmentCost" to 15.0,
                    "notes" to "Use PVC drain pipe with proper slope",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install vent lines",
                    "description" to "Install plumbing vent system",
                    "sequence" to 3,
                    "laborHours" to 1.0,
                    "materialCost" to 40.0,
                    "laborCost" to 15.0,
                    "equipmentCost" to 5.0,
                    "notes" to "Connect to main vent stack or install separate vent",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "PEX Supply Tubing",
                    "description" to "1/2\" and 3/4\" PEX tubing for water supply",
                    "quantity" to 100.0,
                    "unit" to "feet",
                    "unitCost" to 0.75,
                    "totalCost" to 75.0,
                    "waste" to 0.15,
                    "notes" to "Red for hot, blue for cold water lines",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "PVC Drain Pipe",
                    "description" to "3\" and 4\" PVC drain pipe and fittings",
                    "quantity" to 30.0,
                    "unit" to "feet",
                    "unitCost" to 2.50,
                    "totalCost" to 75.0,
                    "waste" to 0.1,
                    "notes" to "Includes elbows, tees, and couplings",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Plumbing Fittings",
                    "description" to "PEX fittings, valves, and connections",
                    "quantity" to 15.0,
                    "unit" to "each",
                    "unitCost" to 2.00,
                    "totalCost" to 30.0,
                    "waste" to 0.1,
                    "notes" to "SharkBite or crimp fittings for PEX connections",
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
        
        // Create Rough Electrical trade
        val roughElectricalResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Rough Electrical",
            description = "Electrical rough-in and wiring installation",
            sequence = 2,
            imageUrl = null
        )
        val roughElectricalTrade = roughElectricalResult.getOrThrow()
        
        // Create Rough-in Installation scope
        val roughInResult = catalogueService.createScope(
            tradeId = roughElectricalTrade.id,
            name = "Rough-in Installation",
            description = "Basic electrical rough-in work including wiring and boxes",
            sequence = 1,
            imageUrl = null
        )
        val roughInScope = roughInResult.getOrThrow()
        
        // Create Electrical Rough-in assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to roughInScope.id,
                "name" to "Electrical Rough-in",
                "description" to "Basic electrical rough-in work",
                "sequence" to 1,
                "unit" to "room",
                "laborHours" to 4.0,
                "materialCost" to 120.0,  // 60% of $200
                "laborCost" to 60.0,      // 4 hours at $15/hour
                "equipmentCost" to 20.0,  // 10% of $200
                "subcontractorCost" to 0.0,
                "otherCost" to 0.0,
                "totalCost" to 200.0,
                "markupPercentage" to 0.15,
                "notes" to "Includes wiring, boxes, and basic rough-in electrical work",
                "tags" to listOf("electrical", "rough-in", "wiring", "boxes"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install electrical boxes",
                    "description" to "Install outlet and switch boxes",
                    "sequence" to 1,
                    "laborHours" to 1.5,
                    "materialCost" to 30.0,
                    "laborCost" to 22.50,
                    "equipmentCost" to 7.50,
                    "notes" to "Position boxes at proper height and secure to studs",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Run electrical wire",
                    "description" to "Pull 12 and 14 AWG wire through walls",
                    "sequence" to 2,
                    "laborHours" to 2.0,
                    "materialCost" to 60.0,
                    "laborCost" to 30.0,
                    "equipmentCost" to 10.0,
                    "notes" to "Use appropriate wire gauge for circuit amperage",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Connect to panel",
                    "description" to "Make connections at electrical panel",
                    "sequence" to 3,
                    "laborHours" to 0.5,
                    "materialCost" to 30.0,
                    "laborCost" to 7.50,
                    "equipmentCost" to 2.50,
                    "notes" to "Install appropriate breakers and label circuits",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Electrical Boxes",
                    "description" to "Standard outlet and switch boxes",
                    "quantity" to 8.0,
                    "unit" to "each",
                    "unitCost" to 2.50,
                    "totalCost" to 20.0,
                    "waste" to 0.1,
                    "notes" to "Single gang and double gang boxes",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Electrical Wire",
                    "description" to "12 AWG and 14 AWG THHN wire",
                    "quantity" to 150.0,
                    "unit" to "feet",
                    "unitCost" to 0.50,
                    "totalCost" to 75.0,
                    "waste" to 0.15,
                    "notes" to "Mix of 12 AWG for 20A circuits and 14 AWG for 15A circuits",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Circuit Breakers",
                    "description" to "15A and 20A circuit breakers",
                    "quantity" to 3.0,
                    "unit" to "each",
                    "unitCost" to 8.33,
                    "totalCost" to 25.0,
                    "waste" to 0.0,
                    "notes" to "Single pole breakers for 120V circuits",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Electrical data")
    }
}