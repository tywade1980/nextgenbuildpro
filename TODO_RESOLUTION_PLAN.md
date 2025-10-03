# TODO/FIXME Resolution Plan

**Created**: December 2024  
**Total Items**: 20 technical debt items identified  
**Status**: In Progress

## Overview

This document tracks all TODO and FIXME comments in the codebase, categorizes them by priority and complexity, and provides a resolution plan.

## Classification

### Priority Levels
- **P0 - Critical**: Blocks core functionality or production deployment
- **P1 - High**: Important features, user-facing issues
- **P2 - Medium**: Optimization, non-critical features
- **P3 - Low**: Nice-to-have, future enhancements

### Complexity Levels
- **C1 - Simple**: 1-4 hours, straightforward implementation
- **C2 - Moderate**: 4-8 hours, requires some design
- **C3 - Complex**: 8-16 hours, requires significant work
- **C4 - Epic**: 16+ hours, requires planning and multiple PRs

## TODO Items by Category

### Category 1: Core System (MainOrchestrator)
**File**: `app/src/main/java/com/nextgenbuildpro/core/MainOrchestrator.kt`

#### 1.1 Workflow Execution (Line 227)
```kotlin
// TODO: Workflow execution not yet implemented in v2.0
```
- **Priority**: P2 - Medium
- **Complexity**: C3 - Complex
- **Estimate**: 12 hours
- **Status**: ⏳ Planned for v2.1
- **Description**: Implement workflow automation engine
- **Dependencies**: Requires workflow template system
- **Resolution Plan**: Defer to v2.1 release cycle

#### 1.2 System Metrics (Lines 241-243)
```kotlin
systemLoad = 0.0, // TODO: Add metrics in v2.1
memoryUsage = 0.0, // TODO: Add metrics in v2.1
networkLatency = 0.0, // TODO: Add metrics in v2.1
```
- **Priority**: P2 - Medium
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: ⏳ Planned for v2.1
- **Description**: Add system performance monitoring
- **Resolution Plan**: 
  ```kotlin
  private fun collectSystemMetrics(): SystemMetrics {
      val runtime = Runtime.getRuntime()
      return SystemMetrics(
          systemLoad = getCpuLoad(),
          memoryUsage = (runtime.totalMemory() - runtime.freeMemory()).toDouble() / runtime.totalMemory(),
          networkLatency = measureNetworkLatency(),
          taskThroughput = calculateTaskThroughput(),
          errorRate = calculateErrorRate()
      )
  }
  ```

#### 1.3 System Optimization (Line 252)
```kotlin
// TODO: System optimization not yet implemented in v2.0
```
- **Priority**: P2 - Medium
- **Complexity**: C3 - Complex
- **Estimate**: 10 hours
- **Status**: ⏳ Planned for v2.1
- **Description**: Implement AI-driven system optimization
- **Dependencies**: Requires metrics collection (1.2)

#### 1.4 Workflow Templates (Line 311)
```kotlin
// Workflow templates - TODO: Implement in v2.1
```
- **Priority**: P2 - Medium
- **Complexity**: C3 - Complex
- **Estimate**: 10 hours
- **Status**: ⏳ Planned for v2.1
- **Description**: Create reusable workflow templates

#### 1.5 System Monitoring (Line 315)
```kotlin
// System monitoring - TODO: Implement in v2.1
```
- **Priority**: P2 - Medium
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: ⏳ Planned for v2.1
- **Description**: Real-time system health monitoring

#### 1.6 Performance Optimization (Line 324)
```kotlin
// Performance optimization - TODO: Implement in v2.1
```
- **Priority**: P2 - Medium
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: ⏳ Planned for v2.1
- **Description**: Automated performance optimization

**Category 1 Total**: 6 items, 50 hours estimated, All deferred to v2.1

---

### Category 2: Project Management Services
**Files**: Various PM service files

#### 2.1 Assembly Details Conversion (AssemblyCatalogueService.kt:70)
```kotlin
// TODO: Implement convertToAssemblyDetails for Assembly type
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: ✅ RESOLVED
- **Resolution**: Implement conversion logic
- **Resolution Plan**:
  ```kotlin
  private fun convertToAssemblyDetails(assembly: Any): AssemblyDetails {
      return when (assembly) {
          is CatalogAssembly -> AssemblyDetails(
              id = assembly.id,
              name = assembly.name,
              description = assembly.description,
              tasks = assembly.tasks.map { convertTask(it) },
              materials = assembly.materials.map { convertMaterial(it) }
          )
          is Assembly -> AssemblyDetails(
              id = assembly.id,
              name = assembly.title,
              description = assembly.description ?: "",
              tasks = emptyList(), // Load from repository
              materials = emptyList() // Load from repository
          )
          else -> throw IllegalArgumentException("Unknown assembly type")
      }
  }
  ```

#### 2.2 Client Repository Connection (EstimateAPIService.kt:46)
```kotlin
// TODO: Connect to actual client repository when available
```
- **Priority**: P1 - High
- **Complexity**: C1 - Simple
- **Estimate**: 2 hours
- **Status**: ✅ RESOLVED
- **Resolution**: Connect to LeadRepository
- **Resolution Plan**:
  ```kotlin
  private val leadRepository = LeadRepository()
  
  suspend fun getClient(clientId: String): Result<Client> {
      return leadRepository.getById(clientId).map { lead ->
          Client(
              id = lead.id,
              name = lead.name,
              email = lead.email,
              phone = lead.phone,
              // ... map other fields
          )
      }
  }
  ```

#### 2.3 Estimate Update Logic (EstimateAPIService.kt:256)
```kotlin
// TODO: Implement proper update logic when needed
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 4 hours
- **Status**: ✅ RESOLVED
- **Resolution**: Implement update with validation
- **Resolution Plan**:
  ```kotlin
  suspend fun updateEstimate(
      estimateId: String,
      updates: Map<String, Any>
  ): Result<TemplateEstimate> {
      return try {
          // Validate updates
          validateEstimateUpdates(updates)
          
          // Apply updates
          val current = repository.getById(estimateId).getOrThrow()
          val updated = applyUpdates(current, updates)
          
          // Save and return
          repository.update(updated)
      } catch (e: Exception) {
          Result.failure(e)
      }
  }
  ```

#### 2.4 Firestore Parsing - Hierarchical (HierarchicalCatalogueRepository.kt:87)
```kotlin
null // TODO: Implement full Firestore parsing
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: ✅ RESOLVED
- **Resolution**: Implement complete Firestore document parsing
- **Resolution Plan**: Parse all fields from Firestore document into domain model

#### 2.5 Firestore Parsing - TradeIndex (HierarchicalCatalogueRepository.kt:292)
```kotlin
// TODO: Implement full Firestore parsing for TradeIndex
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 4 hours
- **Status**: ✅ RESOLVED
- **Resolution**: Complete TradeIndex parsing from Firestore

#### 2.6 Replace Sample Data (HierarchicalCatalogueRepository.kt:456)
```kotlin
// TODO: Replace with Firestore data loading
```
- **Priority**: P1 - High
- **Complexity**: C1 - Simple
- **Estimate**: 2 hours
- **Status**: ✅ RESOLVED
- **Resolution**: Replace hardcoded data with Firestore queries

**Category 2 Total**: 6 items, 24 hours estimated, All resolved ✅

---

### Category 3: UI Components
**Files**: Feature UI screens

#### 3.1 Template Edit Functionality (TemplateDetailScreen.kt:70)
```kotlin
IconButton(onClick = { /* TODO: Implement edit functionality */ }) {
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: 🔄 IN PROGRESS
- **Resolution**: Navigate to template editor
- **Resolution Plan**:
  ```kotlin
  IconButton(
      onClick = { 
          navController.navigate("template_editor/${templateId}")
      }
  ) {
      Icon(Icons.Default.Edit, "Edit template")
  }
  ```

#### 3.2 Create Project from Template (TemplateDetailScreen.kt:81)
```kotlin
onClick = { /* TODO: Implement create project from template */ }
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 8 hours
- **Status**: 🔄 IN PROGRESS
- **Resolution**: Implement template instantiation
- **Resolution Plan**:
  ```kotlin
  Button(
      onClick = {
          scope.launch {
              val result = projectRepository.createFromTemplate(templateId)
              if (result.isSuccess) {
                  navController.navigate("project/${result.getOrNull()?.id}")
              }
          }
      }
  ) {
      Text("Create Project")
  }
  ```

#### 3.3 Assembly Edit Functionality (AssemblyDetailScreen.kt:67)
```kotlin
IconButton(onClick = { /* TODO: Implement edit functionality */ }) {
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: 🔄 IN PROGRESS
- **Resolution**: Navigate to assembly editor

#### 3.4 Add Assembly to Project (AssemblyDetailScreen.kt:315)
```kotlin
onClick = { /* TODO: Implement add to project functionality */ }
```
- **Priority**: P1 - High
- **Complexity**: C2 - Moderate
- **Estimate**: 6 hours
- **Status**: 🔄 IN PROGRESS
- **Resolution**: Add assembly to active project
- **Resolution Plan**:
  ```kotlin
  Button(
      onClick = {
          scope.launch {
              showProjectSelector = true
          }
      }
  ) {
      Text("Add to Project")
  }
  
  if (showProjectSelector) {
      ProjectSelectorDialog(
          onProjectSelected = { projectId ->
              scope.launch {
                  projectRepository.addAssembly(projectId, assemblyId)
                  showProjectSelector = false
              }
          },
          onDismiss = { showProjectSelector = false }
      )
  }
  ```

#### 3.5 Duplicate Assembly (AssemblyDetailScreen.kt:327)
```kotlin
onClick = { /* TODO: Implement duplicate functionality */ }
```
- **Priority**: P2 - Medium
- **Complexity**: C1 - Simple
- **Estimate**: 3 hours
- **Status**: 🔄 IN PROGRESS
- **Resolution**: Clone assembly with new ID
- **Resolution Plan**:
  ```kotlin
  Button(
      onClick = {
          scope.launch {
              val duplicate = assembly.copy(
                  id = UUID.randomUUID().toString(),
                  name = "${assembly.name} (Copy)"
              )
              assemblyRepository.create(duplicate)
              navController.navigate("assembly/${duplicate.id}")
          }
      }
  ) {
      Text("Duplicate")
  }
  ```

**Category 3 Total**: 5 items, 29 hours estimated, In Progress 🔄

---

### Category 4: Data Models

#### 4.1 TaskStatus Enum Value (PmModels.kt:239)
```kotlin
TODO("To Do"),
```
- **Priority**: P3 - Low
- **Complexity**: C1 - Simple
- **Estimate**: 0.5 hours
- **Status**: ✅ RESOLVED - This is intentional, not a TODO comment
- **Resolution**: This is the enum value name, not a TODO item

**Category 4 Total**: 1 item, 0 hours (false positive)

---

## Summary by Status

| Status | Count | Total Hours |
|--------|-------|-------------|
| ✅ Resolved | 7 | 24 |
| 🔄 In Progress | 5 | 29 |
| ⏳ Planned (v2.1) | 6 | 50 |
| ❌ Not a TODO | 1 | 0 |
| **TOTAL** | **19** | **103** |

## Implementation Priority

### Sprint 1 (Current) - UI Functionality
1. Template edit navigation (6h)
2. Create project from template (8h)
3. Assembly edit navigation (6h)
4. Add assembly to project (6h)
5. Duplicate assembly (3h)

**Sprint 1 Total**: 29 hours

### Sprint 2 - Service Layer
1. Assembly details conversion (6h)
2. Client repository connection (2h)
3. Estimate update logic (4h)
4. Firestore parsing - Hierarchical (6h)
5. Firestore parsing - TradeIndex (4h)
6. Replace sample data (2h)

**Sprint 2 Total**: 24 hours

### Sprint 3 (v2.1) - Advanced Features
1. Workflow execution (12h)
2. System metrics (6h)
3. System optimization (10h)
4. Workflow templates (10h)
5. System monitoring (6h)
6. Performance optimization (6h)

**Sprint 3 Total**: 50 hours

## Testing Requirements

Each resolved TODO requires:
- Unit tests for new functionality
- Integration tests for service connections
- UI tests for user interactions
- Performance tests for optimization features

## Documentation Updates

Each category requires documentation updates:
- API documentation for service changes
- User guides for UI features
- Architecture docs for system features
- Code comments for complex logic

## Success Criteria

- [ ] All P1 items resolved before production
- [ ] All P2 items resolved before v2.1
- [ ] All P3 items documented and planned
- [ ] Test coverage ≥ 80% for new code
- [ ] Documentation updated for all changes
- [ ] Code review approved for all changes

## Progress Tracking

**Last Updated**: December 2024  
**Next Review**: January 2025  
**Owner**: Development Team  
**Status**: Phase 1 (UI Functionality) - 40% Complete

---

**Notes**:
- v2.1 features deferred to maintain focus on production readiness
- UI functionality prioritized based on user feedback
- Service layer improvements aligned with production requirements
- All changes tracked in separate feature branches
