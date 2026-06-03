package com.constructionmanager.data.database

import com.constructionmanager.data.database.entity.MaterialEntity
import com.constructionmanager.domain.model.MaterialCategory
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

object SeedData {
    
    fun getMaterials2025(): List<MaterialEntity> = listOf(
        // LUMBER - Updated for 2025 prices
        MaterialEntity(
            id = "lumber_001",
            name = "Dimensional Lumber 2x4x8 SPF",
            category = MaterialCategory.LUMBER,
            subcategory = "Framing Lumber",
            unitOfMeasurement = "Each",
            currentPrice = "8.50",
            supplier = "Home Depot",
            supplierSku = "HD-2X4X8SPF",
            description = "Kiln-dried spruce-pine-fir dimensional lumber for framing",
            specifications = Json.encodeToString(mapOf(
                "Grade" to "Stud",
                "Moisture Content" to "19% max",
                "Species" to "SPF",
                "Treatment" to "Kiln Dried"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "9.25",
                "Southeast" to "7.85",
                "Midwest" to "8.15",
                "Southwest" to "8.95",
                "West" to "10.50"
            ))
        ),
        MaterialEntity(
            id = "lumber_002",
            name = "Dimensional Lumber 2x6x10 SPF",
            category = MaterialCategory.LUMBER,
            subcategory = "Framing Lumber",
            unitOfMeasurement = "Each",
            currentPrice = "18.75",
            supplier = "Lowes",
            supplierSku = "L-2X6X10SPF",
            description = "Premium grade spruce-pine-fir for structural applications",
            specifications = Json.encodeToString(mapOf(
                "Grade" to "Select Structural",
                "Moisture Content" to "19% max",
                "Species" to "SPF"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "20.50",
                "Southeast" to "17.25",
                "Midwest" to "18.00",
                "Southwest" to "19.95",
                "West" to "22.75"
            ))
        ),
        MaterialEntity(
            id = "lumber_003",
            name = "Plywood 3/4\" CDX 4x8",
            category = MaterialCategory.LUMBER,
            subcategory = "Sheathing",
            unitOfMeasurement = "Sheet",
            currentPrice = "68.95",
            supplier = "Menards",
            supplierSku = "M-PLY34CDX48",
            description = "Construction grade plywood for subflooring and sheathing",
            specifications = Json.encodeToString(mapOf(
                "Grade" to "CDX",
                "Thickness" to "3/4 inch",
                "Size" to "4x8 feet",
                "Exposure" to "Exterior"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "74.50",
                "Southeast" to "65.25",
                "Midwest" to "66.95",
                "Southwest" to "71.50",
                "West" to "78.95"
            ))
        ),
        
        // CONCRETE - Updated 2025 prices
        MaterialEntity(
            id = "concrete_001",
            name = "Ready Mix Concrete 3000 PSI",
            category = MaterialCategory.CONCRETE,
            subcategory = "Ready Mix",
            unitOfMeasurement = "Cubic Yard",
            currentPrice = "185.00",
            supplier = "LaFarge",
            supplierSku = "LF-3000PSI",
            description = "Standard 3000 PSI concrete mix for foundations and slabs",
            specifications = Json.encodeToString(mapOf(
                "Compressive Strength" to "3000 PSI",
                "Slump" to "4-6 inches",
                "Air Content" to "4-7%",
                "Mix Design" to "Standard"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "195.00",
                "Southeast" to "175.00",
                "Midwest" to "180.00",
                "Southwest" to "190.00",
                "West" to "205.00"
            ))
        ),
        MaterialEntity(
            id = "concrete_002",
            name = "Concrete Block 8x8x16 Standard",
            category = MaterialCategory.CONCRETE,
            subcategory = "Masonry",
            unitOfMeasurement = "Each",
            currentPrice = "3.85",
            supplier = "Block Supply Co",
            supplierSku = "BSC-8816STD",
            description = "Standard concrete masonry unit for foundations and walls",
            specifications = Json.encodeToString(mapOf(
                "Size" to "8x8x16 inches",
                "Weight" to "38 lbs",
                "Compressive Strength" to "1900 PSI",
                "Type" to "Standard"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        ),
        
        // STEEL - Updated 2025 prices
        MaterialEntity(
            id = "steel_001",
            name = "Structural Steel Beam W12x26",
            category = MaterialCategory.STEEL,
            subcategory = "Structural",
            unitOfMeasurement = "Linear Foot",
            currentPrice = "28.50",
            supplier = "Steel Warehouse",
            supplierSku = "SW-W12X26",
            description = "Hot-rolled structural steel wide flange beam",
            specifications = Json.encodeToString(mapOf(
                "Designation" to "W12x26",
                "Weight" to "26 lbs/ft",
                "Grade" to "A992",
                "Finish" to "Mill"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "31.00",
                "Southeast" to "26.75",
                "Midwest" to "27.50",
                "Southwest" to "29.25",
                "West" to "32.50"
            ))
        ),
        
        // ROOFING - Updated 2025 prices
        MaterialEntity(
            id = "roofing_001",
            name = "Asphalt Shingles 3-Tab 25 Year",
            category = MaterialCategory.ROOFING,
            subcategory = "Shingles",
            unitOfMeasurement = "Square",
            currentPrice = "125.00",
            supplier = "GAF",
            supplierSku = "GAF-3TAB25",
            description = "Standard 3-tab asphalt shingles with 25-year warranty",
            specifications = Json.encodeToString(mapOf(
                "Type" to "3-Tab",
                "Warranty" to "25 Years",
                "Wind Rating" to "60 mph",
                "Coverage" to "100 sq ft"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "135.00",
                "Southeast" to "118.00",
                "Midwest" to "122.00",
                "Southwest" to "128.00",
                "West" to "142.00"
            ))
        ),
        MaterialEntity(
            id = "roofing_002",
            name = "Architectural Shingles Lifetime",
            category = MaterialCategory.ROOFING,
            subcategory = "Shingles",
            unitOfMeasurement = "Square",
            currentPrice = "285.00",
            supplier = "Owens Corning",
            supplierSku = "OC-ARCHLIFE",
            description = "Premium architectural shingles with lifetime warranty",
            specifications = Json.encodeToString(mapOf(
                "Type" to "Architectural",
                "Warranty" to "Lifetime",
                "Wind Rating" to "130 mph",
                "Algae Resistance" to "Yes"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "310.00",
                "Southeast" to "265.00",
                "Midwest" to "275.00",
                "Southwest" to "295.00",
                "West" to "325.00"
            ))
        ),
        
        // INSULATION - Updated 2025 prices
        MaterialEntity(
            id = "insulation_001",
            name = "Fiberglass Batt R-13 3.5\"",
            category = MaterialCategory.INSULATION,
            subcategory = "Batt",
            unitOfMeasurement = "Square Foot",
            currentPrice = "1.35",
            supplier = "Johns Manville",
            supplierSku = "JM-R13BATT",
            description = "Kraft-faced fiberglass batt insulation for 2x4 walls",
            specifications = Json.encodeToString(mapOf(
                "R-Value" to "R-13",
                "Thickness" to "3.5 inches",
                "Width" to "15 inches",
                "Facing" to "Kraft Paper"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        ),
        
        // DRYWALL - Updated 2025 prices
        MaterialEntity(
            id = "drywall_001",
            name = "Gypsum Board 1/2\" 4x8",
            category = MaterialCategory.DRYWALL,
            subcategory = "Standard",
            unitOfMeasurement = "Sheet",
            currentPrice = "18.95",
            supplier = "USG",
            supplierSku = "USG-HALF48",
            description = "Standard 1/2 inch gypsum wallboard",
            specifications = Json.encodeToString(mapOf(
                "Thickness" to "1/2 inch",
                "Size" to "4x8 feet",
                "Edge" to "Tapered",
                "Type" to "Regular"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "20.50",
                "Southeast" to "17.75",
                "Midwest" to "18.25",
                "Southwest" to "19.50",
                "West" to "21.95"
            ))
        ),
        
        // FLOORING - Updated 2025 prices
        MaterialEntity(
            id = "flooring_001",
            name = "Luxury Vinyl Plank Premium",
            category = MaterialCategory.FLOORING,
            subcategory = "LVP",
            unitOfMeasurement = "Square Foot",
            currentPrice = "4.85",
            supplier = "Shaw",
            supplierSku = "SHAW-LVPPREM",
            description = "Premium luxury vinyl plank with attached pad",
            specifications = Json.encodeToString(mapOf(
                "Thickness" to "8mm",
                "Wear Layer" to "20 mil",
                "Installation" to "Click Lock",
                "Warranty" to "25 Years"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "5.25",
                "Southeast" to "4.55",
                "Midwest" to "4.65",
                "Southwest" to "4.95",
                "West" to "5.50"
            ))
        ),
        MaterialEntity(
            id = "flooring_002",
            name = "Hardwood Oak 3/4\" Solid",
            category = MaterialCategory.FLOORING,
            subcategory = "Hardwood",
            unitOfMeasurement = "Square Foot",
            currentPrice = "8.95",
            supplier = "Bruce",
            supplierSku = "BRUCE-OAK34",
            description = "Solid red oak hardwood flooring",
            specifications = Json.encodeToString(mapOf(
                "Species" to "Red Oak",
                "Thickness" to "3/4 inch",
                "Width" to "3.25 inches",
                "Grade" to "Select"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "9.85",
                "Southeast" to "8.25",
                "Midwest" to "8.55",
                "Southwest" to "9.25",
                "West" to "10.50"
            ))
        ),
        
        // PLUMBING - Updated 2025 prices
        MaterialEntity(
            id = "plumbing_001",
            name = "PEX Tubing 1/2\" x 100ft",
            category = MaterialCategory.PLUMBING,
            subcategory = "Water Supply",
            unitOfMeasurement = "Roll",
            currentPrice = "89.50",
            supplier = "Uponor",
            supplierSku = "UP-PEX12100",
            description = "Cross-linked polyethylene tubing for potable water",
            specifications = Json.encodeToString(mapOf(
                "Size" to "1/2 inch",
                "Length" to "100 feet",
                "Color" to "Red",
                "Pressure Rating" to "160 PSI at 73°F"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        ),
        
        // ELECTRICAL - Updated 2025 prices
        MaterialEntity(
            id = "electrical_001",
            name = "Romex 12-2 W/G 250ft",
            category = MaterialCategory.ELECTRICAL,
            subcategory = "Wire",
            unitOfMeasurement = "Roll",
            currentPrice = "165.00",
            supplier = "Southwire",
            supplierSku = "SW-122WG250",
            description = "Non-metallic sheathed cable for residential wiring",
            specifications = Json.encodeToString(mapOf(
                "Size" to "12 AWG",
                "Conductors" to "2 + Ground",
                "Length" to "250 feet",
                "Insulation" to "THHN"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "178.00",
                "Southeast" to "155.00",
                "Midwest" to "160.00",
                "Southwest" to "168.00",
                "West" to "185.00"
            ))
        ),
        
        // HVAC - Updated 2025 prices
        MaterialEntity(
            id = "hvac_001",
            name = "Ductwork Galvanized 6\" Round",
            category = MaterialCategory.HVAC,
            subcategory = "Ductwork",
            unitOfMeasurement = "Linear Foot",
            currentPrice = "12.50",
            supplier = "Imperial",
            supplierSku = "IMP-DUCT6RD",
            description = "Galvanized steel spiral duct for HVAC systems",
            specifications = Json.encodeToString(mapOf(
                "Diameter" to "6 inches",
                "Material" to "Galvanized Steel",
                "Gauge" to "26",
                "Type" to "Spiral"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        ),
        
        // WINDOWS - Updated 2025 prices
        MaterialEntity(
            id = "windows_001",
            name = "Double Hung Window 36\"x48\"",
            category = MaterialCategory.WINDOWS,
            subcategory = "Double Hung",
            unitOfMeasurement = "Each",
            currentPrice = "485.00",
            supplier = "Andersen",
            supplierSku = "AND-DH3648",
            description = "Energy efficient vinyl double hung window",
            specifications = Json.encodeToString(mapOf(
                "Size" to "36\" x 48\"",
                "Material" to "Vinyl",
                "Glass" to "Low-E",
                "U-Factor" to "0.30"
            )),
            lastPriceUpdate = Clock.System.now().toString(),
            regionalPricing = Json.encodeToString(mapOf(
                "Northeast" to "525.00",
                "Southeast" to "465.00",
                "Midwest" to "475.00",
                "Southwest" to "495.00",
                "West" to "545.00"
            ))
        ),
        
        // DOORS - Updated 2025 prices
        MaterialEntity(
            id = "doors_001",
            name = "Interior Door 6-Panel 32\"",
            category = MaterialCategory.DOORS,
            subcategory = "Interior",
            unitOfMeasurement = "Each",
            currentPrice = "125.00",
            supplier = "Masonite",
            supplierSku = "MAS-6PAN32",
            description = "Hollow core 6-panel interior door",
            specifications = Json.encodeToString(mapOf(
                "Width" to "32 inches",
                "Height" to "80 inches",
                "Thickness" to "1 3/8 inches",
                "Core" to "Hollow"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        ),
        
        // HARDWARE - Updated 2025 prices
        MaterialEntity(
            id = "hardware_001",
            name = "Framing Nails 16d Galvanized",
            category = MaterialCategory.HARDWARE,
            subcategory = "Fasteners",
            unitOfMeasurement = "50 lb Box",
            currentPrice = "165.00",
            supplier = "Grip-Rite",
            supplierSku = "GR-16DGALV50",
            description = "Hot-dipped galvanized framing nails",
            specifications = Json.encodeToString(mapOf(
                "Size" to "16d",
                "Length" to "3.5 inches",
                "Coating" to "Hot-Dipped Galvanized",
                "Head" to "Round"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        ),
        
        // FIXTURES - 2025 prices
        MaterialEntity(
            id = "fixtures_001",
            name = "Kitchen Sink Stainless 33\"x22\"",
            category = MaterialCategory.FIXTURES,
            subcategory = "Kitchen",
            unitOfMeasurement = "Each",
            currentPrice = "285.00",
            supplier = "Kohler",
            supplierSku = "KOH-SS3322",
            description = "Stainless steel double bowl kitchen sink",
            specifications = Json.encodeToString(mapOf(
                "Size" to "33\" x 22\"",
                "Material" to "18-Gauge Stainless Steel",
                "Bowls" to "Double",
                "Installation" to "Drop-In"
            )),
            lastPriceUpdate = Clock.System.now().toString()
        )
    )
}