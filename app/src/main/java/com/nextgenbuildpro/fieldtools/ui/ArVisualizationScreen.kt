package com.nextgenbuildpro.fieldtools.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nextgenbuildpro.features.fieldtools.ArBlueprintService
import com.nextgenbuildpro.features.fieldtools.BlueprintData
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import androidx.compose.ui.viewinterop.AndroidView

/**
 * AR Visualization Screen - Functional implementation for AR tools
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArVisualizationScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isArActive by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var arBlueprintService by remember { mutableStateOf<ArBlueprintService?>(null) }

    // Initialize AR service
    LaunchedEffect(Unit) {
        arBlueprintService = ArBlueprintService(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Visualization") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Toggle AR help */ }
                    ) {
                        Icon(Icons.Default.Help, contentDescription = "Help")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isArActive) {
                FloatingActionButton(
                    onClick = { isArActive = true }
                ) {
                    Icon(Icons.Default.ViewInAr, contentDescription = "Start AR")
                }
            }
        }
    ) { paddingValues ->
        if (isArActive) {
            ArViewScreen(
                selectedModel = selectedModel,
                arBlueprintService = arBlueprintService,
                onModelPlaced = { model ->
                    // Handle model placement
                },
                onExitAr = {
                    isArActive = false
                    arBlueprintService?.clearAll()
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "AR Visualization Tools",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text(
                        text = "Visualize construction projects and materials in augmented reality",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(getArQuickActions()) { action ->
                    ArActionCard(
                        action = action,
                        onClick = {
                            selectedModel = action.modelId
                            if (action.requiresAr) {
                                isArActive = true
                            }
                        }
                    )
                }

                // Recent Projects
                item {
                    Text(
                        text = "Recent AR Sessions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(getRecentArSessions()) { session ->
                    ArSessionCard(session = session)
                }
            }
        }
    }
}

@Composable
fun ArViewScreen(
    selectedModel: String?,
    arBlueprintService: ArBlueprintService?,
    onModelPlaced: (String) -> Unit,
    onExitAr: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var arSceneView by remember { mutableStateOf<ArSceneView?>(null) }
    var isPlacingModel by remember { mutableStateOf(false) }

    // Initialize AR scene
    LaunchedEffect(Unit) {
        arBlueprintService?.let { service ->
            // Load sample blueprint
            val sampleBlueprint = BlueprintData(
                id = "sample_blueprint",
                name = "Sample Kitchen Layout",
                dimensions = floatArrayOf(5f, 4f)
            )
            service.loadBlueprint(sampleBlueprint)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // AR Scene View
        AndroidView(
            factory = { ctx ->
                ArSceneView(ctx).apply {
                    arSceneView = this
                    arBlueprintService?.initializeArScene(this)

                    // Set up plane detection
                    planeRenderer.isVisible = true

                    // Handle tap to place models
                    setOnTouchAr {
                        if (isPlacingModel) {
                            val hitResult = it.node?.hitTest(it.motionEvent) ?: it.hitResult
                            hitResult?.let { result ->
                                placeModelAtHit(result, selectedModel, arBlueprintService, onModelPlaced)
                                isPlacingModel = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // AR UI Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Tap to place: ${selectedModel ?: "Select a model"}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // AR Controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isPlacingModel = true },
                    enabled = selectedModel != null
                ) {
                    Text("Place Model")
                }

                OutlinedButton(
                    onClick = onExitAr
                ) {
                    Text("Exit AR")
                }
            }

            // Model manipulation controls
            arBlueprintService?.placedModels?.collectAsState()?.value?.let { models ->
                if (models.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${models.size} models placed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun placeModelAtHit(
    hitResult: HitResult,
    selectedModel: String?,
    arBlueprintService: ArBlueprintService?,
    onModelPlaced: (String) -> Unit
) {
    arBlueprintService?.let { service ->
        when (selectedModel) {
            "kitchen_layout", "bathroom_fixtures", "furniture_set" -> {
                // Place blueprint overlay
                val blueprintId = service.placeBlueprintOverlay(hitResult.hitPose, hitResult.trackable as? Plane ?: return)
                onModelPlaced(blueprintId)
            }
            "lighting_fixtures" -> {
                // Place 3D model (simplified - would need actual 3D model files)
                val modelId = service.place3DModel(
                    modelPath = "models/light_fixture.glb", // Would need actual model file
                    hitPose = hitResult.hitPose,
                    modelName = "Lighting Fixture"
                )
                onModelPlaced(modelId)
            }
            else -> {
                // Default blueprint placement
                val blueprintId = service.placeBlueprintOverlay(hitResult.hitPose, hitResult.trackable as? Plane ?: return)
                onModelPlaced(blueprintId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArActionCard(
    action: ArAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ArSessionCard(session: ArSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = session.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = "Models",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${session.modelsUsed} models placed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class ArAction(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val modelId: String?,
    val requiresAr: Boolean = true
)

data class ArSession(
    val projectName: String,
    val description: String,
    val date: String,
    val modelsUsed: Int
)

private fun getArQuickActions(): List<ArAction> {
    return listOf(
        ArAction(
            title = "Kitchen Layout",
            description = "Visualize kitchen cabinets and appliances",
            icon = Icons.Default.Kitchen,
            modelId = "kitchen_layout"
        ),
        ArAction(
            title = "Bathroom Fixtures",
            description = "Place bathroom fixtures and tiles",
            icon = Icons.Default.Bathtub,
            modelId = "bathroom_fixtures"
        ),
        ArAction(
            title = "Furniture Placement",
            description = "Preview furniture in rooms",
            icon = Icons.Default.Chair,
            modelId = "furniture_set"
        ),
        ArAction(
            title = "Lighting Design",
            description = "Position lights and see illumination",
            icon = Icons.Default.Lightbulb,
            modelId = "lighting_fixtures"
        ),
        ArAction(
            title = "Material Samples",
            description = "Compare flooring and wall materials",
            icon = Icons.Default.Palette,
            modelId = "material_samples"
        )
    )
}

private fun getRecentArSessions(): List<ArSession> {
    return listOf(
        ArSession(
            projectName = "Kitchen Renovation - Johnson Residence",
            description = "Placed kitchen cabinets and island layout",
            date = "Dec 10, 2024",
            modelsUsed = 12
        ),
        ArSession(
            projectName = "Master Bathroom - Smith House",
            description = "Visualized new vanity and shower placement",
            date = "Dec 8, 2024",
            modelsUsed = 8
        ),
        ArSession(
            projectName = "Living Room Layout - Davis Home",
            description = "Arranged furniture and lighting",
            date = "Dec 5, 2024",
            modelsUsed = 15
        )
    )
}