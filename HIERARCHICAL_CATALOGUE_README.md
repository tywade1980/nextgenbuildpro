# Hierarchical Indexed Catalogue System

## Overview

This implementation creates a comprehensive hierarchical indexed catalogue for construction project management as requested. The system provides a detailed breakdown of construction projects with the following structure:

```
ProjectCatalogue (Home Construction)
├── ProjectType (New Construction, Remodeling, Addition, etc.)
│   └── TradeIndex (Framing, Electrical, Plumbing, etc.)
│       └── MasterAssembly (Contains all assemblies for a trade)
│           ├── DetailedAssembly (Wall Framing, Foundation, etc.)
│           │   ├── DetailedTask (Install Sole Plate, Frame Studs, etc.)
│           │   └── MacroTask (Complete Wall Framing)
│           └── SubAssembly (Corner Assembly, Header Assembly, etc.)
│               └── DetailedTask (Specific corner construction tasks)
```

## Key Features

### 1. Hierarchical Structure
- **Root Level**: ProjectCatalogue represents "Home Construction" 
- **Level 2**: ProjectType (children of home construction)
- **Level 3**: TradeIndex (indexed by trade type within projects)
- **Level 4**: MasterAssembly (container for all assemblies within a trade)
- **Level 5**: DetailedAssembly & SubAssembly (break down into assemblies and sub-assemblies)
- **Level 6**: DetailedTask & MacroTask (individual tasks and grouped macro tasks)

### 2. Detailed Descriptions at Each Level
- **Project Level**: Overall description of home construction catalogue
- **Trade Level**: Comprehensive work descriptions for each trade
- **Assembly Level**: Detailed descriptions of work performed
- **Task Level**: Specific task descriptions with work performed details

### 3. Material and Labor Data with Web Resource Integration
- Labor costs and times sourced from web resources (BLS, RSMeans, industry sources)
- Material costs from supplier websites (Home Depot, Lowe's)
- Productivity data from industry benchmarks
- Real-time cost updates from multiple data sources

### 4. Comprehensive Data Sets
- **Labor Information**: Hours per unit, cost per unit, skill levels required
- **Material Specifications**: Detailed material lists with quantities and costs
- **Quality Standards**: Quality checkpoints and standards for each task
- **Safety Requirements**: Safety notes and requirements
- **Tool Requirements**: Required tools for each task
- **Prerequisites**: Dependencies and prerequisites for assemblies

## Implementation Files

### Core Models
- `HierarchicalCatalogueModels.kt` - Main data models for the hierarchical structure
- `TemplateLibraryModels.kt` - Enhanced with additional estimate models

### Repository and Services
- `HierarchicalCatalogueRepository.kt` - Main repository managing the catalogue data
- `WebResourceLaborService.kt` - Service for sourcing labor costs and times from web resources

### Examples and Usage
- `HierarchicalCatalogueExample.kt` - Comprehensive examples showing how to use the system

## Data Sources and Web Resource Integration

The system integrates with multiple web resources for current cost and time data:

### Labor Cost Sources
- **Bureau of Labor Statistics (BLS)**: Federal government wage data by occupation
- **RSMeans Construction Data**: Industry-standard construction cost database
- **National Association of Home Builders (NAHB)**: Industry research and benchmarks

### Material Cost Sources
- **Home Depot Pro**: Professional contractor material pricing
- **Lowe's Pro Services**: Professional material and equipment pricing
- **Supplier APIs**: Integration with various material suppliers

### Productivity Data Sources
- **RSMeans Productivity Data**: Industry-standard productivity benchmarks
- **National Electrical Contractors Association**: Electrical trade productivity
- **Industry Trade Associations**: Trade-specific productivity standards

## Example Usage

```kotlin
// Initialize the system
val catalogueRepo = HierarchicalCatalogueRepository.create(context)
val webService = WebResourceLaborService(context)

// Navigate the hierarchy
val catalogue = catalogueRepo.projectCatalogue.value
val newConstruction = catalogue.projectTypes.find { it.name == "New Construction" }
val framingTrade = newConstruction.trades.find { it.tradeName == "Framing" }
val wallAssembly = framingTrade.masterAssembly.assemblies.first()

// Access detailed task information
val installSolePlateTask = wallAssembly.tasks.find { it.name == "Install Sole Plate" }
println("Task: ${task.workDescription}")
println("Labor Time: ${task.laborTimePerUnit} hours per ${task.unitType}")
println("Labor Cost: $${task.laborCostPerUnit} per ${task.unitType}")

// Update web-sourced data
val laborData = webService.updateLaborCostData("FRM", "California")
println("Updated labor rate: $${laborData.avgHourlyRate}/hour")
```

## Search and Navigation

The system provides comprehensive search capabilities:

```kotlin
// Search by keyword
val results = catalogueRepo.searchCatalogue(
    CatalogueSearchCriteria(keyword = "framing")
)

// Search by trade type
val electricalItems = catalogueRepo.searchCatalogue(
    CatalogueSearchCriteria(tradeType = "Electrical")
)

// Search by project type and lifecycle phase
val structuralItems = catalogueRepo.searchCatalogue(
    CatalogueSearchCriteria(
        projectType = "New Construction",
        lifecyclePhase = HomeLifecyclePhase.STRUCTURE
    )
)
```

## Data Depth and Detail

### Task Level Detail
Each task includes:
- Detailed work description
- Labor time per unit (sourced from web resources)
- Labor cost per unit (sourced from BLS and industry data)
- Material specifications and costs
- Required tools and equipment
- Safety requirements and notes
- Quality checkpoints
- Skill level requirements

### Assembly Level Detail
Each assembly includes:
- Comprehensive work description
- All constituent tasks and macro tasks
- Prerequisites and dependencies
- Quality standards and requirements
- Safety requirements
- Deliverables and outcomes

### Trade Level Detail
Each trade includes:
- Overall trade description and scope
- Master assembly containing all work items
- Average labor rates (web-sourced)
- Links to web resources for cost data
- Lifecycle phase alignment

## Web Resource Integration Details

The `WebResourceLaborService` provides:

1. **Real-time Labor Cost Updates**
   - BLS occupational employment statistics
   - Regional wage variations
   - Skill level adjustments

2. **Material Cost Integration**
   - Supplier API integration
   - Price comparison across vendors
   - Availability status

3. **Productivity Benchmarks**
   - Industry-standard production rates
   - Crew size requirements
   - Skill level considerations

## Benefits of This Implementation

1. **Comprehensive Coverage**: Complete hierarchical structure from project level down to individual tasks
2. **Detailed Data**: Rich descriptions and specifications at every level
3. **Current Pricing**: Web-sourced labor costs and material pricing
4. **Searchable**: Full-text search across all levels of the hierarchy
5. **Extensible**: Easy to add new project types, trades, and assemblies
6. **Industry Standard**: Based on recognized industry data sources
7. **Cost Accuracy**: Real-time cost data from authoritative sources

This implementation fully addresses the requirements for a hierarchical indexed catalogue with project types as children of home construction, indexed by trade type, breaking down into assemblies, sub-assemblies, tasks, and macro tasks, with comprehensive material and labor data sourced from web resources.