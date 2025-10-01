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
     * Create foundation category data
     */
    private suspend fun createFoundation(categoryId: String) {
        Log.d(TAG, "Creating Foundation category data...")
        
        // Create Concrete trade
        val concreteResult = catalogueService.createTrade(
            categoryId = categoryId,
            name = "Concrete",
            description = "Concrete work and foundation systems",
            sequence = 1,
            imageUrl = null
        )
        val concreteTrade = concreteResult.getOrThrow()
        
        // Create Basement Construction scope
        val basementConstructionResult = catalogueService.createScope(
            tradeId = concreteTrade.id,
            name = "Basement Construction",
            description = "Full basement excavation, foundation, and construction",
            sequence = 1,
            imageUrl = null
        )
        val basementConstructionScope = basementConstructionResult.getOrThrow()
        
        // Create Full Basement Foundation assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementConstructionScope.id,
                "name" to "Full Basement Foundation",
                "description" to "Complete basement foundation including excavation, footings, walls, and waterproofing",
                "sequence" to 1,
                "unit" to "each",
                "laborHours" to 120.0,
                "materialCost" to 18000.0,
                "laborCost" to 6000.0,
                "equipmentCost" to 3000.0,
                "subcontractorCost" to 2000.0,
                "otherCost" to 1000.0,
                "totalCost" to 30000.0,
                "markupPercentage" to 0.15,
                "notes" to "Complete basement foundation system with waterproofing and drainage",
                "tags" to listOf("foundation", "basement", "concrete", "excavation", "waterproofing"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Excavate to required depth",
                    "description" to "Excavate basement area to required depth and grade",
                    "sequence" to 1,
                    "laborHours" to 8.0,
                    "materialCost" to 500.0,
                    "laborCost" to 400.0,
                    "equipmentCost" to 800.0,
                    "notes" to "Include proper sloping and access for equipment",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install footing drains",
                    "description" to "Install perimeter footing drainage system",
                    "sequence" to 2,
                    "laborHours" to 6.0,
                    "materialCost" to 800.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 100.0,
                    "notes" to "Include drain tile and gravel bed",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Form and pour footings",
                    "description" to "Set forms and pour concrete footings",
                    "sequence" to 3,
                    "laborHours" to 12.0,
                    "materialCost" to 2000.0,
                    "laborCost" to 600.0,
                    "equipmentCost" to 400.0,
                    "notes" to "Ensure proper reinforcement and dimensions",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Form basement walls",
                    "description" to "Set forms for basement wall construction",
                    "sequence" to 4,
                    "laborHours" to 16.0,
                    "materialCost" to 1500.0,
                    "laborCost" to 800.0,
                    "equipmentCost" to 200.0,
                    "notes" to "Include window and door openings",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install steel reinforcement",
                    "description" to "Install rebar reinforcement in walls",
                    "sequence" to 5,
                    "laborHours" to 8.0,
                    "materialCost" to 1200.0,
                    "laborCost" to 400.0,
                    "equipmentCost" to 100.0,
                    "notes" to "Follow structural engineer specifications",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Pour concrete walls",
                    "description" to "Pour concrete for basement walls",
                    "sequence" to 6,
                    "laborHours" to 10.0,
                    "materialCost" to 3500.0,
                    "laborCost" to 500.0,
                    "equipmentCost" to 600.0,
                    "notes" to "Ensure proper vibration and consolidation",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Strip forms",
                    "description" to "Remove forms after concrete cures",
                    "sequence" to 7,
                    "laborHours" to 4.0,
                    "materialCost" to 0.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 50.0,
                    "notes" to "Wait minimum 24 hours before stripping",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Apply waterproofing membrane",
                    "description" to "Apply waterproofing to exterior walls",
                    "sequence" to 8,
                    "laborHours" to 12.0,
                    "materialCost" to 1200.0,
                    "laborCost" to 600.0,
                    "equipmentCost" to 100.0,
                    "notes" to "Include foundation preparation and primer",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install drainage board",
                    "description" to "Install drainage board over waterproofing",
                    "sequence" to 9,
                    "laborHours" to 6.0,
                    "materialCost" to 800.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 50.0,
                    "notes" to "Protect waterproofing membrane during backfill",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install window wells",
                    "description" to "Install basement window wells",
                    "sequence" to 10,
                    "laborHours" to 4.0,
                    "materialCost" to 600.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 50.0,
                    "notes" to "Include proper drainage and covers",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Backfill foundation",
                    "description" to "Backfill around foundation walls",
                    "sequence" to 11,
                    "laborHours" to 6.0,
                    "materialCost" to 300.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 400.0,
                    "notes" to "Compact in lifts to prevent settling",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Concrete",
                    "description" to "Ready-mix concrete for footings and walls",
                    "quantity" to 45.0,
                    "unit" to "CY",
                    "unitCost" to 120.0,
                    "totalCost" to 5400.0,
                    "waste" to 0.05,
                    "notes" to "3000 PSI minimum for foundation",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Rebar",
                    "description" to "#4 and #5 reinforcement steel",
                    "quantity" to 3000.0,
                    "unit" to "LB",
                    "unitCost" to 0.40,
                    "totalCost" to 1200.0,
                    "waste" to 0.1,
                    "notes" to "Grade 60 deformed bars",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Waterproofing Membrane",
                    "description" to "Rubberized asphalt waterproofing",
                    "quantity" to 1200.0,
                    "unit" to "SF",
                    "unitCost" to 1.00,
                    "totalCost" to 1200.0,
                    "waste" to 0.1,
                    "notes" to "Self-adhering membrane with primer",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Drainage Tile",
                    "description" to "4\" perforated drain pipe",
                    "quantity" to 200.0,
                    "unit" to "LF",
                    "unitCost" to 3.00,
                    "totalCost" to 600.0,
                    "waste" to 0.05,
                    "notes" to "Include fittings and connections",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Form Lumber",
                    "description" to "Plywood and lumber for concrete forms",
                    "quantity" to 150.0,
                    "unit" to "SF",
                    "unitCost" to 3.00,
                    "totalCost" to 450.0,
                    "waste" to 0.15,
                    "notes" to "Reusable forming system preferred",
                    "isActive" to true
                )
            )
        )
        
        // Create Basement Floor assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementConstructionScope.id,
                "name" to "Basement Floor",
                "description" to "Complete basement floor system with insulation and finishes",
                "sequence" to 2,
                "unit" to "SF",
                "laborHours" to 40.0,
                "materialCost" to 6000.0,
                "laborCost" to 2000.0,
                "equipmentCost" to 800.0,
                "subcontractorCost" to 1200.0,
                "otherCost" to 0.0,
                "totalCost" to 10000.0,
                "markupPercentage" to 0.15,
                "notes" to "Includes radon mitigation, insulation, and finished concrete floor",
                "tags" to listOf("basement", "floor", "concrete", "insulation", "radon"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install radon mitigation system",
                    "description" to "Install sub-slab radon mitigation piping",
                    "sequence" to 1,
                    "laborHours" to 4.0,
                    "materialCost" to 300.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 100.0,
                    "notes" to "Include collection mat and vent piping",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Place 4\" stone base",
                    "description" to "Place and level stone base under slab",
                    "sequence" to 2,
                    "laborHours" to 6.0,
                    "materialCost" to 800.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 200.0,
                    "notes" to "Use clean crushed stone",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Compact stone base",
                    "description" to "Compact stone base to required density",
                    "sequence" to 3,
                    "laborHours" to 2.0,
                    "materialCost" to 0.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 150.0,
                    "notes" to "Achieve 95% compaction",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install vapor barrier",
                    "description" to "Install plastic vapor barrier over stone",
                    "sequence" to 4,
                    "laborHours" to 3.0,
                    "materialCost" to 200.0,
                    "laborCost" to 150.0,
                    "equipmentCost" to 0.0,
                    "notes" to "6 mil polyethylene with sealed joints",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install perimeter insulation",
                    "description" to "Install foam insulation around perimeter",
                    "sequence" to 5,
                    "laborHours" to 4.0,
                    "materialCost" to 600.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 0.0,
                    "notes" to "2\" rigid foam insulation",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install radiant heat (if applicable)",
                    "description" to "Install radiant floor heating system",
                    "sequence" to 6,
                    "laborHours" to 8.0,
                    "materialCost" to 1500.0,
                    "laborCost" to 400.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Optional upgrade - hydronic system",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install wire mesh/rebar",
                    "description" to "Install reinforcement in concrete slab",
                    "sequence" to 7,
                    "laborHours" to 3.0,
                    "materialCost" to 400.0,
                    "laborCost" to 150.0,
                    "equipmentCost" to 50.0,
                    "notes" to "6x6 W2.9xW2.9 welded wire mesh",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Pour concrete slab",
                    "description" to "Pour concrete floor slab",
                    "sequence" to 8,
                    "laborHours" to 6.0,
                    "materialCost" to 1800.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 200.0,
                    "notes" to "4\" thick slab, 3000 PSI concrete",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Float and finish concrete",
                    "description" to "Float and finish concrete surface",
                    "sequence" to 9,
                    "laborHours" to 4.0,
                    "materialCost" to 100.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 100.0,
                    "notes" to "Power float to smooth finish",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Cut control joints",
                    "description" to "Cut control joints in concrete",
                    "sequence" to 10,
                    "laborHours" to 2.0,
                    "materialCost" to 50.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 50.0,
                    "notes" to "Cut within 24 hours of pour",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Cure concrete",
                    "description" to "Apply curing compound and protect slab",
                    "sequence" to 11,
                    "laborHours" to 1.0,
                    "materialCost" to 150.0,
                    "laborCost" to 50.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Maintain proper moisture for 7 days",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Concrete",
                    "description" to "Ready-mix concrete for floor slab",
                    "quantity" to 15.0,
                    "unit" to "CY",
                    "unitCost" to 120.0,
                    "totalCost" to 1800.0,
                    "waste" to 0.05,
                    "notes" to "3000 PSI with fiber reinforcement",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Crushed Stone",
                    "description" to "Clean crushed stone base",
                    "quantity" to 12.0,
                    "unit" to "CY",
                    "unitCost" to 35.0,
                    "totalCost" to 420.0,
                    "waste" to 0.1,
                    "notes" to "3/4\" minus clean stone",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Vapor Barrier",
                    "description" to "6 mil polyethylene sheeting",
                    "quantity" to 1100.0,
                    "unit" to "SF",
                    "unitCost" to 0.15,
                    "totalCost" to 165.0,
                    "waste" to 0.1,
                    "notes" to "Class A vapor retarder",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Rigid Insulation",
                    "description" to "2\" XPS foam insulation",
                    "quantity" to 200.0,
                    "unit" to "SF",
                    "unitCost" to 2.50,
                    "totalCost" to 500.0,
                    "waste" to 0.1,
                    "notes" to "Extruded polystyrene",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Wire Mesh",
                    "description" to "Welded wire reinforcement",
                    "quantity" to 1000.0,
                    "unit" to "SF",
                    "unitCost" to 0.35,
                    "totalCost" to 350.0,
                    "waste" to 0.1,
                    "notes" to "6x6 W2.9xW2.9 WWR",
                    "isActive" to true
                )
            )
        )
        
        // Create Basement Finishing scope
        val basementFinishingResult = catalogueService.createScope(
            tradeId = concreteTrade.id,
            name = "Basement Finishing",
            description = "Interior basement finishing including framing and drywall",
            sequence = 2,
            imageUrl = null
        )
        val basementFinishingScope = basementFinishingResult.getOrThrow()
        
        // Create Framed Basement Walls assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementFinishingScope.id,
                "name" to "Framed Basement Walls",
                "description" to "Interior framed walls for basement finishing",
                "sequence" to 1,
                "unit" to "LF",
                "laborHours" to 24.0,
                "materialCost" to 3600.0,
                "laborCost" to 1200.0,
                "equipmentCost" to 200.0,
                "subcontractorCost" to 0.0,
                "otherCost" to 0.0,
                "totalCost" to 5000.0,
                "markupPercentage" to 0.15,
                "notes" to "Complete framed wall system with insulation and vapor barrier",
                "tags" to listOf("basement", "framing", "walls", "insulation", "finishing"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Layout wall locations",
                    "description" to "Mark wall layout on floor and ceiling",
                    "sequence" to 1,
                    "laborHours" to 2.0,
                    "materialCost" to 50.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Use chalk line and square for accuracy",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install bottom plate (pressure treated)",
                    "description" to "Install pressure treated bottom plates",
                    "sequence" to 2,
                    "laborHours" to 3.0,
                    "materialCost" to 300.0,
                    "laborCost" to 150.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Use concrete anchors or ramset",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install top plate",
                    "description" to "Install top plates to ceiling/joists",
                    "sequence" to 3,
                    "laborHours" to 2.0,
                    "materialCost" to 200.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Attach securely to floor joists above",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install studs 16\" O.C.",
                    "description" to "Install wall studs at 16 inches on center",
                    "sequence" to 4,
                    "laborHours" to 6.0,
                    "materialCost" to 800.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 50.0,
                    "notes" to "Check for plumb and straight",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Frame door openings",
                    "description" to "Frame rough openings for doors",
                    "sequence" to 5,
                    "laborHours" to 2.0,
                    "materialCost" to 150.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Include headers and cripple studs",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Frame utilities chases",
                    "description" to "Frame chases for utilities",
                    "sequence" to 6,
                    "laborHours" to 2.0,
                    "materialCost" to 100.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Size for plumbing and electrical",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install blocking for fixtures",
                    "description" to "Install blocking for wall-mounted fixtures",
                    "sequence" to 7,
                    "laborHours" to 2.0,
                    "materialCost" to 100.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Include TV mounts, cabinets, etc.",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install wall insulation",
                    "description" to "Install insulation in wall cavities",
                    "sequence" to 8,
                    "laborHours" to 4.0,
                    "materialCost" to 600.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 0.0,
                    "notes" to "R-13 fiberglass batts",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install vapor barrier",
                    "description" to "Install vapor barrier over insulation",
                    "sequence" to 9,
                    "laborHours" to 1.0,
                    "materialCost" to 200.0,
                    "laborCost" to 50.0,
                    "equipmentCost" to 0.0,
                    "notes" to "6 mil poly with sealed joints",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "2x4 Studs",
                    "description" to "Standard construction grade 2x4 studs",
                    "quantity" to 50.0,
                    "unit" to "each",
                    "unitCost" to 8.00,
                    "totalCost" to 400.0,
                    "waste" to 0.1,
                    "notes" to "8 foot lengths",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Pressure Treated Plates",
                    "description" to "2x4 pressure treated lumber for bottom plates",
                    "quantity" to 25.0,
                    "unit" to "each",
                    "unitCost" to 12.00,
                    "totalCost" to 300.0,
                    "waste" to 0.05,
                    "notes" to "Ground contact rated",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Standard Plates",
                    "description" to "2x4 lumber for top plates",
                    "quantity" to 25.0,
                    "unit" to "each",
                    "unitCost" to 8.00,
                    "totalCost" to 200.0,
                    "waste" to 0.05,
                    "notes" to "Standard construction grade",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Insulation",
                    "description" to "R-13 fiberglass batt insulation",
                    "quantity" to 150.0,
                    "unit" to "SF",
                    "unitCost" to 1.00,
                    "totalCost" to 150.0,
                    "waste" to 0.1,
                    "notes" to "3.5\" thick for 2x4 walls",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Fasteners",
                    "description" to "Nails, screws, and concrete anchors",
                    "quantity" to 10.0,
                    "unit" to "lbs",
                    "unitCost" to 5.00,
                    "totalCost" to 50.0,
                    "waste" to 0.0,
                    "notes" to "Include concrete anchors for bottom plates",
                    "isActive" to true
                )
            )
        )
        
        // Create Basement Ceiling assembly
        catalogueService.createCompleteAssembly(
            assemblyData = mapOf(
                "scopeId" to basementFinishingScope.id,
                "name" to "Basement Ceiling",
                "description" to "Finished basement ceiling with insulation and drywall",
                "sequence" to 2,
                "unit" to "SF",
                "laborHours" to 20.0,
                "materialCost" to 2400.0,
                "laborCost" to 1000.0,
                "equipmentCost" to 100.0,
                "subcontractorCost" to 500.0,
                "otherCost" to 0.0,
                "totalCost" to 4000.0,
                "markupPercentage" to 0.15,
                "notes" to "Complete ceiling system with sound control and access panels",
                "tags" to listOf("basement", "ceiling", "drywall", "insulation", "finishing"),
                "isActive" to true
            ),
            taskDataList = listOf(
                mapOf(
                    "name" to "Install sound insulation",
                    "description" to "Install sound insulation between joists",
                    "sequence" to 1,
                    "laborHours" to 4.0,
                    "materialCost" to 600.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 0.0,
                    "notes" to "R-19 fiberglass batts for sound control",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install resilient channels",
                    "description" to "Install resilient channels for sound isolation",
                    "sequence" to 2,
                    "laborHours" to 3.0,
                    "materialCost" to 300.0,
                    "laborCost" to 150.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Space per manufacturer specifications",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install drywall",
                    "description" to "Install drywall on ceiling",
                    "sequence" to 3,
                    "laborHours" to 6.0,
                    "materialCost" to 800.0,
                    "laborCost" to 300.0,
                    "equipmentCost" to 50.0,
                    "notes" to "5/8\" drywall for better sag resistance",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Tape and finish drywall",
                    "description" to "Tape, mud, and finish drywall joints",
                    "sequence" to 4,
                    "laborHours" to 4.0,
                    "materialCost" to 200.0,
                    "laborCost" to 200.0,
                    "equipmentCost" to 25.0,
                    "notes" to "Three coat system with sanding",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Install ceiling access panels",
                    "description" to "Install access panels for utilities",
                    "sequence" to 5,
                    "laborHours" to 2.0,
                    "materialCost" to 200.0,
                    "laborCost" to 100.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Provide access to critical utilities",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Prime and paint ceiling",
                    "description" to "Prime and paint finished ceiling",
                    "sequence" to 6,
                    "laborHours" to 1.0,
                    "materialCost" to 300.0,
                    "laborCost" to 50.0,
                    "equipmentCost" to 0.0,
                    "notes" to "Use high-quality ceiling paint",
                    "isActive" to true
                )
            ),
            materialDataList = listOf(
                mapOf(
                    "name" to "Sound Insulation",
                    "description" to "R-19 fiberglass insulation",
                    "quantity" to 1000.0,
                    "unit" to "SF",
                    "unitCost" to 0.60,
                    "totalCost" to 600.0,
                    "waste" to 0.1,
                    "notes" to "6.25\" thick for sound control",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Resilient Channels",
                    "description" to "Metal resilient channels",
                    "quantity" to 300.0,
                    "unit" to "LF",
                    "unitCost" to 1.00,
                    "totalCost" to 300.0,
                    "waste" to 0.05,
                    "notes" to "RC-1 resilient channels",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Drywall",
                    "description" to "5/8\" drywall sheets",
                    "quantity" to 25.0,
                    "unit" to "sheets",
                    "unitCost" to 15.00,
                    "totalCost" to 375.0,
                    "waste" to 0.1,
                    "notes" to "4x8 sheets, 5/8\" thick",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Drywall Compound",
                    "description" to "Joint compound and tape",
                    "quantity" to 10.0,
                    "unit" to "gal",
                    "unitCost" to 25.00,
                    "totalCost" to 250.0,
                    "waste" to 0.05,
                    "notes" to "Include paper tape and corner bead",
                    "isActive" to true
                ),
                mapOf(
                    "name" to "Ceiling Paint",
                    "description" to "High-quality ceiling paint",
                    "quantity" to 3.0,
                    "unit" to "gal",
                    "unitCost" to 45.00,
                    "totalCost" to 135.0,
                    "waste" to 0.05,
                    "notes" to "Flat white ceiling paint",
                    "isActive" to true
                )
            )
        )
        
        Log.d(TAG, "Created Foundation data")
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