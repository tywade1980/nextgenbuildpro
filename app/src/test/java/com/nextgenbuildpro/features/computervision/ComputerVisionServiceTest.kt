package com.nextgenbuildpro.features.computervision

import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Comprehensive test suite for Computer Vision Service
 *
 * Tests ML Kit integration, hazard detection, progress monitoring,
 * equipment recognition, and quality inspection features.
 */
class ComputerVisionServiceTest {

    @Mock
    private lateinit var mockContext: android.content.Context

    private lateinit var computerVisionService: ComputerVisionService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        computerVisionService = ComputerVisionService()
    }

    @Test
    fun `test service initialization`() = runTest {
        // Given
        val result = computerVisionService.start()

        // Then
        assertTrue("Service should start successfully", result.isSuccess)
        assertEquals(SystemStatus.READY, computerVisionService.status.value)
    }

    @Test
    fun `test hazard detection with mock data`() = runTest {
        // Given
        computerVisionService.start()
        val testImageData = ByteArray(1024) // Mock image data

        // When
        val result = computerVisionService.detectSafetyHazards(testImageData, "site_001")

        // Then
        assertTrue("Hazard detection should succeed", result.isSuccess)
        val detectionResult = result.getOrNull()
        assertNotNull("Detection result should not be null", detectionResult)
        assertEquals("site_001", detectionResult?.siteId)
    }

    @Test
    fun `test progress analysis with mock data`() = runTest {
        // Given
        computerVisionService.start()
        val testImageData = ByteArray(2048)
        val expectedWork = "Foundation work, framing"

        // When
        val result = computerVisionService.analyzeProgressFromPhoto(testImageData, "project_001", expectedWork)

        // Then
        assertTrue("Progress analysis should succeed", result.isSuccess)
        val progressResult = result.getOrNull()
        assertNotNull("Progress result should not be null", progressResult)
        assertEquals("project_001", progressResult?.projectId)
        assertTrue("Completion percentage should be valid", progressResult?.completionPercentage in 0..100)
    }

    @Test
    fun `test equipment recognition with mock data`() = runTest {
        // Given
        computerVisionService.start()
        val testImageData = ByteArray(1536)

        // When
        val result = computerVisionService.recognizeEquipmentAndMaterials(testImageData, "site_002")

        // Then
        assertTrue("Equipment recognition should succeed", result.isSuccess)
        val recognitionResult = result.getOrNull()
        assertNotNull("Recognition result should not be null", recognitionResult)
        assertEquals("site_002", recognitionResult?.siteId)
        assertTrue("Should detect some items", recognitionResult?.totalItemsDetected ?: 0 > 0)
    }

    @Test
    fun `test quality inspection with mock data`() = runTest {
        // Given
        computerVisionService.start()
        val testImageData = ByteArray(1024)
        val inspectionType = "Concrete Pour Quality"

        // When
        val result = computerVisionService.performQualityInspection(testImageData, inspectionType)

        // Then
        assertTrue("Quality inspection should succeed", result.isSuccess)
        val inspectionResult = result.getOrNull()
        assertNotNull("Inspection result should not be null", inspectionResult)
        assertEquals(inspectionType, inspectionResult?.inspectionType)
        assertNotNull("Quality score should be set", inspectionResult?.overallQualityScore)
    }

    @Test
    fun `test performance statistics tracking`() = runTest {
        // Given
        computerVisionService.start()

        // When
        val statsResult = computerVisionService.getPerformanceStats()

        // Then
        assertTrue("Performance stats should be available", statsResult.isSuccess)
        val stats = statsResult.getOrNull()
        assertNotNull("Stats should not be null", stats)
        assertTrue("Accuracy should be valid", stats?.hazardDetectionAccuracy in 0.0..1.0)
        assertTrue("Processing time should be non-negative", (stats?.averageProcessingTimeMs ?: 0) >= 0)
    }

    @Test
    fun `test learning data integration`() = runTest {
        // Given
        computerVisionService.start()
        val learningData = LearningData(
            input = "hazard_detection_test",
            output = "correct_hazard_identified",
            feedback = 0.9,
            metadata = mapOf("dataType" to "hazard_detection")
        )

        // When
        val learnResult = computerVisionService.learn(learningData)

        // Then
        assertTrue("Learning should succeed", learnResult.isSuccess)

        // Verify knowledge base was updated
        val knowledgeBase = computerVisionService.getKnowledgeBase()
        assertTrue("Knowledge base should contain learning data", knowledgeBase.containsKey("learning_data"))
    }

    @Test
    fun `test model parameter updates`() = runTest {
        // Given
        computerVisionService.start()
        val newParams = mapOf(
            "hazardDetectionAccuracy" to "0.95",
            "progressTrackingAccuracy" to "0.92"
        )

        // When
        val updateResult = computerVisionService.updateModel(newParams)

        // Then
        assertTrue("Model update should succeed", updateResult.isSuccess)
    }

    @Test
    fun `test service health monitoring`() = runTest {
        // Given
        computerVisionService.start()

        // When
        val healthResult = computerVisionService.getHealthStatus()

        // Then
        assertTrue("Health check should succeed", healthResult.isSuccess)
        val health = healthResult.getOrNull()
        assertNotNull("Health status should not be null", health)
        assertEquals("Computer Vision Service", health?.serviceName)
    }

    @Test
    fun `test concurrent processing safety`() = runTest {
        // Given
        computerVisionService.start()
        val testImageData = ByteArray(512)

        // When - Run multiple concurrent operations
        val results = (1..5).map {
            kotlinx.coroutines.async {
                computerVisionService.detectSafetyHazards(testImageData, "concurrent_site_$it")
            }
        }.map { it.await() }

        // Then - All should succeed without race conditions
        results.forEach { result ->
            assertTrue("Concurrent operation should succeed", result.isSuccess)
        }
    }

    @Test
    fun `test service lifecycle management`() = runTest {
        // Test start
        var result = computerVisionService.start()
        assertTrue("Start should succeed", result.isSuccess)

        // Test restart
        result = computerVisionService.restart()
        assertTrue("Restart should succeed", result.isSuccess)

        // Test stop
        result = computerVisionService.stop()
        assertTrue("Stop should succeed", result.isSuccess)
    }

    @Test
    fun `test error handling for invalid input`() = runTest {
        // Given
        computerVisionService.start()
        val invalidImageData = ByteArray(0) // Empty image

        // When
        val result = computerVisionService.detectSafetyHazards(invalidImageData, "invalid_site")

        // Then - Should handle gracefully (may succeed with simulation or fail gracefully)
        // This tests that the service doesn't crash on invalid input
        assertNotNull("Result should be returned", result)
    }
}