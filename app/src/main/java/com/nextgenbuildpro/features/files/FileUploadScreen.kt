package com.nextgenbuildpro.features.files

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * File Upload Screen - Manages document upload and file management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUploadScreen(navController: NavController) {
    var uploadedFiles by remember { mutableStateOf(getSampleFiles()) }
    var showUploadDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Upload File")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadDialog = true }
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Project Documents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload and manage project-related documents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Upload Options
            item {
                Text(
                    text = "Upload Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(getUploadOptions()) { option ->
                UploadOptionCard(
                    option = option,
                    onClick = {
                        when (option.type) {
                            UploadType.CAMERA -> {
                                // Handle camera capture
                            }
                            UploadType.GALLERY -> {
                                // Handle gallery selection
                            }
                            UploadType.DOCUMENT -> {
                                // Handle document picker
                            }
                            UploadType.CLOUD -> {
                                // Handle cloud import
                            }
                        }
                    }
                )
            }

            // Recent Files
            item {
                Text(
                    text = "Recent Files",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(uploadedFiles) { file ->
                FileItem(
                    file = file,
                    onDelete = {
                        uploadedFiles = uploadedFiles.filter { it.id != file.id }
                    },
                    onShare = {
                        // Handle file sharing
                    },
                    onView = {
                        // Handle file viewing
                    }
                )
            }
        }
    }

    // Upload Dialog
    if (showUploadDialog) {
        UploadDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { fileName, fileType ->
                val newFile = ProjectFile(
                    id = "file_${System.currentTimeMillis()}",
                    name = fileName,
                    type = fileType,
                    size = "${(100..5000).random()} KB",
                    uploadDate = "Just now",
                    category = FileCategory.DOCUMENT
                )
                uploadedFiles = listOf(newFile) + uploadedFiles
                showUploadDialog = false
            }
        )
    }
}

@Composable
private fun UploadOptionCard(
    option: UploadOption,
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
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FileItem(
    file: ProjectFile,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (file.category) {
                        FileCategory.IMAGE -> Icons.Default.Image
                        FileCategory.DOCUMENT -> Icons.Default.Description
                        FileCategory.PDF -> Icons.Default.PictureAsPdf
                        FileCategory.VIDEO -> Icons.Default.VideoFile
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${file.size} • ${file.uploadDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, contentDescription = "View")
                }
                
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadDialog(
    onDismiss: () -> Unit,
    onUpload: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("Document") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload File") },
        text = {
            Column {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = fileType,
                    onValueChange = { fileType = it },
                    label = { Text("File Type") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (fileName.isNotBlank()) {
                        onUpload(fileName, fileType)
                    }
                }
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Data classes for file management
 */
data class ProjectFile(
    val id: String,
    val name: String,
    val type: String,
    val size: String,
    val uploadDate: String,
    val category: FileCategory
)

data class UploadOption(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val type: UploadType
)

enum class FileCategory {
    IMAGE,
    DOCUMENT,
    PDF,
    VIDEO
}

enum class UploadType {
    CAMERA,
    GALLERY,
    DOCUMENT,
    CLOUD
}

/**
 * Sample data functions
 */
private fun getSampleFiles(): List<ProjectFile> {
    return listOf(
        ProjectFile(
            id = "1",
            name = "Building Plans.pdf",
            type = "PDF",
            size = "2.3 MB",
            uploadDate = "2 hours ago",
            category = FileCategory.PDF
        ),
        ProjectFile(
            id = "2",
            name = "Site Photo 1.jpg",
            type = "Image",
            size = "1.8 MB",
            uploadDate = "1 day ago",
            category = FileCategory.IMAGE
        ),
        ProjectFile(
            id = "3",
            name = "Material Invoice.doc",
            type = "Document",
            size = "524 KB",
            uploadDate = "3 days ago",
            category = FileCategory.DOCUMENT
        ),
        ProjectFile(
            id = "4",
            name = "Progress Video.mp4",
            type = "Video",
            size = "15.2 MB",
            uploadDate = "1 week ago",
            category = FileCategory.VIDEO
        )
    )
}

private fun getUploadOptions(): List<UploadOption> {
    return listOf(
        UploadOption(
            icon = Icons.Default.CameraAlt,
            title = "Take Photo",
            description = "Capture a new photo for the project",
            type = UploadType.CAMERA
        ),
        UploadOption(
            icon = Icons.Default.PhotoLibrary,
            title = "Choose from Gallery",
            description = "Select existing photos from your device",
            type = UploadType.GALLERY
        ),
        UploadOption(
            icon = Icons.Default.AttachFile,
            title = "Upload Document",
            description = "Select documents, PDFs, or other files",
            type = UploadType.DOCUMENT
        ),
        UploadOption(
            icon = Icons.Default.Cloud,
            title = "Import from Cloud",
            description = "Import files from cloud storage services",
            type = UploadType.CLOUD
        )
    )
}