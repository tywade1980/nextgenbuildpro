package com.nextgenbuildpro.features.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Camera Screen - Handles photo capture and gallery management for projects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Camera", "Gallery", "Projects")
    var photos by remember { mutableStateOf(getSamplePhotos()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Handle photo capture
                    val newPhoto = Photo(
                        id = "photo_${System.currentTimeMillis()}",
                        title = "New Photo ${photos.size + 1}",
                        description = "Captured just now",
                        timestamp = "Just now",
                        projectName = "Current Project",
                        tags = listOf("Progress", "Site")
                    )
                    photos = listOf(newPhoto) + photos
                }
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> CameraTab()
                1 -> GalleryTab(photos = photos, onDeletePhoto = { photoId ->
                    photos = photos.filter { it.id != photoId }
                })
                2 -> ProjectsTab(photos = photos)
            }
        }
    }
}

@Composable
private fun CameraTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Camera Controls",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Capture photos for your construction projects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Camera Options
        items(getCameraOptions()) { option ->
            CameraOptionCard(option = option)
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(getQuickActions()) { action ->
            QuickActionCard(action = action)
        }
    }
}

@Composable
private fun GalleryTab(
    photos: List<Photo>,
    onDeletePhoto: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(photos) { photo ->
            PhotoItem(photo = photo, onDelete = { onDeletePhoto(photo.id) })
        }
    }
}

@Composable
private fun PhotoItem(photo: Photo, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Photo placeholder (in a real app, this would use Coil or similar to load the image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = photo.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = photo.timestamp,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = photo.projectName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Photo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectsTab(photos: List<Photo>) {
    // Extract unique projects
    val projects = photos.map { it.projectName }.distinct()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(projects) { projectName ->
            ProjectItem(
                projectName = projectName,
                photoCount = photos.count { it.projectName == projectName }
            )
        }
    }
}

@Composable
private fun ProjectItem(projectName: String, photoCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$photoCount photos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CameraOptionCard(option: CameraOption) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Handle camera option */ },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Configure",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Handle quick action */ },
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Data class for photo information
 */
data class Photo(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val projectName: String,
    val tags: List<String>
)

/**
 * Generate sample photos for testing
 */
private fun getSamplePhotos(): List<Photo> {
    return listOf(
        Photo(
            id = "photo_1",
            title = "Foundation Work",
            description = "Concrete foundation being poured",
            timestamp = "Today, 10:45 AM",
            projectName = "123 Main St Renovation",
            tags = listOf("Foundation", "Progress")
        ),
        Photo(
            id = "photo_2",
            title = "Frame Construction",
            description = "Wall framing in progress",
            timestamp = "Yesterday, 3:30 PM",
            projectName = "123 Main St Renovation",
            tags = listOf("Framing", "Progress")
        ),
        Photo(
            id = "photo_3",
            title = "Site Inspection",
            description = "Pre-construction site review",
            timestamp = "Sep 22, 2025",
            projectName = "456 Oak Ave Build",
            tags = listOf("Site", "Inspection")
        ),
        Photo(
            id = "photo_4",
            title = "Material Delivery",
            description = "Lumber and supplies arrived on site",
            timestamp = "Sep 21, 2025",
            projectName = "456 Oak Ave Build",
            tags = listOf("Materials", "Logistics")
        )
    )
}

private fun getCameraOptions(): List<CameraOption> {
    return listOf(
        CameraOption(
            icon = Icons.Default.CameraAlt,
            title = "Photo Mode",
            description = "Standard photo capture with project tagging"
        ),
        CameraOption(
            icon = Icons.Default.Panorama,
            title = "Panorama Mode",
            description = "Wide-angle panoramic shots for site overviews"
        ),
        CameraOption(
            icon = Icons.Default.GridOn,
            title = "Grid Mode",
            description = "Enable grid lines for better composition"
        ),
        CameraOption(
            icon = Icons.Default.Timer,
            title = "Timer Mode",
            description = "Self-timer for hands-free photography"
        )
    )
}

private fun getQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction(
            icon = Icons.Default.PhotoLibrary,
            title = "View Gallery"
        ),
        QuickAction(
            icon = Icons.Default.Upload,
            title = "Upload to Cloud"
        ),
        QuickAction(
            icon = Icons.Default.Share,
            title = "Share with Team"
        ),
        QuickAction(
            icon = Icons.Default.Edit,
            title = "Edit Last Photo"
        )
    }
}

private data class CameraOption(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private data class QuickAction(
    val icon: ImageVector,
    val title: String
)
