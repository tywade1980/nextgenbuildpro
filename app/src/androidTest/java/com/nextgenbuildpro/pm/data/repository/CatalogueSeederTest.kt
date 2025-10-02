package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nextgenbuildpro.pm.service.EnhancedAssemblyCatalogueService
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test class for CatalogueSeeder functionality
 */
@RunWith(AndroidJUnit4::class)
class CatalogueSeederTest {

    private lateinit var context: Context
    private lateinit var catalogueService: EnhancedCatalogueDataService
    private lateinit var assemblyService: EnhancedAssemblyCatalogueService
    private lateinit var seeder: CatalogueSeeder

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        catalogueService = EnhancedCatalogueDataService(context)
        assemblyService = EnhancedAssemblyCatalogueService(catalogueService)
        seeder = CatalogueSeeder(catalogueService)
    }

    @Test
    fun testSeedCatalogue_CreatesCategories() = runBlocking {
        // Act
        val result = seeder.seedCatalogue()

        // Assert
        assertTrue("Seeding should succeed", result.isSuccess)

        // Verify categories were created
        val categoriesResult = catalogueService.getCategoriesWithChildren()
        assertTrue("Should be able to get complete catalogue", categoriesResult.isSuccess)

        val categories = categoriesResult.getOrThrow()
        assertTrue("Should have categories", categories.isNotEmpty())

        // Verify specific categories exist
        val categoryNames = categories.map { it.category.name }
        assertTrue("Should contain Interior Finishes category",
            categoryNames.contains("Interior Finishes"))
        assertTrue("Should contain Plumbing category",
            categoryNames.contains("Plumbing"))
        assertTrue("Should contain Electrical category",
            categoryNames.contains("Electrical"))
    }

    @Test
    fun testOverloadedCreateCompleteAssembly() = runBlocking {
        // First create basic structure
        val categoryResult = catalogueService.createCategory(
            name = "Test Category",
            description = "Test category description",
            sequence = 1
        )
        val category = categoryResult.getOrThrow()

        val tradeResult = catalogueService.createTrade(
            categoryId = category.id,
            name = "Test Trade",
            description = "Test trade description",
            sequence = 1
        )
        val trade = tradeResult.getOrThrow()

        val scopeResult = catalogueService.createScope(
            tradeId = trade.id,
            name = "Test Scope",
            description = "Test scope description",
            sequence = 1
        )
        val scope = scopeResult.getOrThrow()

        // Test the overloaded createCompleteAssembly method
        val assemblyData = mapOf(
            "scopeId" to scope.id,
            "name" to "Test Assembly",
            "description" to "Test assembly description",
            "sequence" to 1,
            "unit" to "each",
            "laborHours" to 5.0,
            "materialCost" to 100.0,
            "laborCost" to 200.0,
            "totalCost" to 300.0,
            "tags" to listOf("test", "assembly")
        )

        val taskDataList = listOf(
            mapOf(
                "name" to "Test Task",
                "description" to "Test task description",
                "sequence" to 1,
                "laborHours" to 2.0,
                "materialCost" to 50.0,
                "laborCost" to 100.0,
                "isActive" to true
            )
        )

        val materialDataList = listOf(
            mapOf(
                "name" to "Test Material",
                "description" to "Test material description",
                "quantity" to 1.0,
                "unit" to "each",
                "unitCost" to 50.0,
                "totalCost" to 50.0,
                "isActive" to true
            )
        )

        // Act
        val result = catalogueService.createCompleteAssembly(
            assemblyData, taskDataList, materialDataList
        )

        // Assert
        assertTrue("Assembly creation should succeed", result.isSuccess)
        val assemblyWithChildren = result.getOrThrow()

        assertEquals("Assembly name should match", "Test Assembly", assemblyWithChildren.assembly.name)
        assertEquals("Should have 1 task", 1, assemblyWithChildren.tasks.size)
        assertEquals("Task name should match", "Test Task", assemblyWithChildren.tasks[0].task.name)
        assertTrue("Should have materials", assemblyWithChildren.materials.isNotEmpty())
    }
}