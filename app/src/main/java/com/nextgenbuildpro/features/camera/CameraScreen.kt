package com.nextgenbuildpro.features.camera

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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(photos) { photo ->
            PhotoCard(
                photo = photo,
                onDelete = { onDeletePhoto(photo.id) }
            )
        }
    }
}

@Composable
private fun ProjectsTab(photos: List<Photo>) {
    val photosByProject = photos.groupBy { it.projectName }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Photos by Project",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        photosByProject.forEach { (projectName, projectPhotos) ->
            item {
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(projectPhotos) { photo ->
                ProjectPhotoItem(photo = photo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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

@Composable
private fun PhotoCard(
    photo: Photo,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Placeholder for image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = photo.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = photo.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
            
            Text(
                text = photo.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectPhotoItem(photo: Photo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = photo.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = photo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = photo.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Data classes
 */
data class Photo(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val projectName: String,
    val tags: List<String>
)

data class CameraOption(
    val icon: ImageVector,
    val title: String,
    val description: String
)

data class QuickAction(
    val icon: ImageVector,
    val title: String
)

/**
 * Sample data functions
 */
private fun getSamplePhotos(): List<Photo> {
    return listOf(
        Photo(
            id = "1",
            title = "Foundation Progress",
            description = "Foundation concrete pour completed",
            timestamp = "2 hours ago",
            projectName = "Johnson Residence",
            tags = listOf("Foundation", "Progress")
        ),
        Photo(
            id = "2",
            title = "Framing Stage",
            description = "Wall framing in progress",
            timestamp = "1 day ago",
            projectName = "Johnson Residence",
            tags = listOf("Framing", "Progress")
        ),
        Photo(
            id = "3",
            title = "Site Overview",
            description = "Aerial view of construction site",
            timestamp = "3 days ago",
            projectName = "Office Building",
            tags = listOf("Site", "Overview")
        ),
        Photo(
            id = "4",
            title = "Material Delivery",
            description = "Steel beams delivered to site",
            timestamp = "1 week ago",
            projectName = "Office Building",
            tags = listOf("Materials", "Delivery")
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
    )
}