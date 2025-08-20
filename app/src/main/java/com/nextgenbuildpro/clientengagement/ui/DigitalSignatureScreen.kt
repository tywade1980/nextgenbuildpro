package com.nextgenbuildpro.clientengagement.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.clientengagement.data.model.*
import com.nextgenbuildpro.clientengagement.data.repository.DigitalSignatureRepository
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Digital Signature Screen
 * 
 * This screen allows users to:
 * 1. Upload documents for signature
 * 2. Capture digital signatures
 * 3. Send signature requests to leads/clients
 * 4. View signature status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalSignatureScreen(
    navController: NavController,
    leadId: String? = null,
    documentId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Create repositories
    val signatureRepository = remember { DigitalSignatureRepository(context) }
    val leadRepository = remember { LeadRepository() }

    // State for the screen
    var currentTab by remember { mutableStateOf(0) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var selectedDocumentId by remember { mutableStateOf<String?>(documentId) }
    var selectedDocumentType by remember { mutableStateOf(DocumentType.CONTRACT) }
    var selectedLeadId by remember { mutableStateOf(leadId) }

    // State for documents and signatures
    var documents by remember { mutableStateOf<List<SignableDocument>>(emptyList()) }
    var signatureRequests by remember { mutableStateOf<List<SignatureRequest>>(emptyList()) }
    var signatures by remember { mutableStateOf<List<DigitalSignature>>(emptyList()) }

    // Load data
    LaunchedEffect(Unit) {
        documents = signatureRepository.getAll().map { 
            signatureRepository.getSignableDocumentById(it.documentId) 
        }.filterNotNull()

        signatureRequests = signatureRepository.getSignatureRequestsByStatus(SignatureRequestStatus.PENDING)
        signatures = signatureRepository.getAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Signatures") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.Upload, contentDescription = "Upload Document")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = currentTab) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = { Text("Documents") }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = { Text("Signature Requests") }
                )
                Tab(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    text = { Text("Completed") }
                )
            }

            // Tab content
            when (currentTab) {
                0 -> DocumentsTab(
                    documents = documents,
                    onDocumentSelected = { 
                        selectedDocumentId = it.id
                        showSignatureDialog = true
                    }
                )
                1 -> SignatureRequestsTab(
                    signatureRequests = signatureRequests,
                    onSendReminder = { requestId ->
                        coroutineScope.launch {
                            signatureRepository.sendReminder(requestId)
                            // Refresh the list
                            signatureRequests = signatureRepository.getSignatureRequestsByStatus(SignatureRequestStatus.PENDING)
                        }
                    }
                )
                2 -> CompletedSignaturesTab(
                    signatures = signatures,
                    onViewSignature = { /* View signature details */ }
                )
            }
        }

        // Upload Document Dialog
        if (showUploadDialog) {
            UploadDocumentDialog(
                onDismiss = { showUploadDialog = false },
                onDocumentUploaded = { document ->
                    coroutineScope.launch {
                        signatureRepository.saveSignableDocument(document)
                        // Refresh the list
                        documents = signatureRepository.getAll().map { 
                            signatureRepository.getSignableDocumentById(it.documentId) 
                        }.filterNotNull()
                        showUploadDialog = false
                    }
                },
                leadId = selectedLeadId,
                documentType = selectedDocumentType,
                onDocumentTypeChanged = { selectedDocumentType = it }
            )
        }

        // Signature Dialog
        if (showSignatureDialog && selectedDocumentId != null) {
            SignatureDialog(
                onDismiss = { showSignatureDialog = false },
                onSignatureCompleted = { signature ->
                    coroutineScope.launch {
                        signatureRepository.save(signature)
                        // Refresh the lists
                        signatures = signatureRepository.getAll()
                        showSignatureDialog = false
                    }
                },
                documentId = selectedDocumentId!!,
                documentType = selectedDocumentType
            )
        }
    }
}

/**
 * Tab for displaying documents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsTab(
    documents: List<SignableDocument>,
    onDocumentSelected: (SignableDocument) -> Unit
) {
    if (documents.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No documents available.\nUpload a document to get started.",
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documents) { document ->
                DocumentItem(
                    document = document,
                    onClick = { onDocumentSelected(document) }
                )
            }
        }
    }
}

/**
 * Item for displaying a document
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentItem(
    document: SignableDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (document.documentType) {
                    DocumentType.CONTRACT -> Icons.Default.Description
                    DocumentType.ESTIMATE -> Icons.Default.Calculate
                    DocumentType.INVOICE -> Icons.Default.Receipt
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Type: ${document.documentType.name.lowercase().capitalize()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Created: ${Date(document.createdAt).toLocaleString()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sign"
                )
            }
        }
    }
}

/**
 * Tab for displaying signature requests
 */
@Composable
fun SignatureRequestsTab(
    signatureRequests: List<SignatureRequest>,
    onSendReminder: (String) -> Unit
) {
    if (signatureRequests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pending signature requests.",
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(signatureRequests) { request ->
                SignatureRequestItem(
                    request = request,
                    onSendReminder = { onSendReminder(request.id) }
                )
            }
        }
    }
}

/**
 * Item for displaying a signature request
 */
@Composable
fun SignatureRequestItem(
    request: SignatureRequest,
    onSendReminder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Request to: ${request.requestedFrom}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Status: ${request.status.name.lowercase().capitalize()}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Requested: ${Date(request.requestedAt).toLocaleString()}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (request.expiresAt != null) {
                        Text(
                            text = "Expires: ${Date(request.expiresAt).toLocaleString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(onClick = onSendReminder) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Send Reminder"
                    )
                }
            }

            if (request.remindersSent > 0) {
                Text(
                    text = "Reminders sent: ${request.remindersSent}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Tab for displaying completed signatures
 */
@Composable
fun CompletedSignaturesTab(
    signatures: List<DigitalSignature>,
    onViewSignature: (DigitalSignature) -> Unit
) {
    if (signatures.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed signatures.",
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(signatures) { signature ->
                SignatureItem(
                    signature = signature,
                    onClick = { onViewSignature(signature) }
                )
            }
        }
    }
}

/**
 * Item for displaying a signature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureItem(
    signature: DigitalSignature,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Draw,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Signed by: ${signature.signedBy}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Document: ${signature.documentType.name.lowercase().capitalize()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Date: ${Date(signature.signedAt).toLocaleString()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (signature.isValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid",
                    tint = Color.Green
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid",
                    tint = Color.Red
                )
            }
        }
    }
}

/**
 * Dialog for uploading a document
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentDialog(
    onDismiss: () -> Unit,
    onDocumentUploaded: (SignableDocument) -> Unit,
    leadId: String? = null,
    documentType: DocumentType,
    onDocumentTypeChanged: (DocumentType) -> Unit
) {
    val context = LocalContext.current
    var documentTitle by remember { mutableStateOf("") }
    var documentDescription by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // File picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Document") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = documentTitle,
                    onValueChange = { documentTitle = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = documentDescription,
                    onValueChange = { documentDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Document type dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = documentType.name.lowercase().capitalize(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DocumentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().capitalize()) },
                                onClick = {
                                    onDocumentTypeChanged(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { launcher.launch("application/pdf") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select File")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (selectedFileUri != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "File Selected",
                            tint = Color.Green
                        )
                    }
                }

                if (selectedFileUri != null) {
                    Text(
                        text = "File selected: ${selectedFileUri?.lastPathSegment}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (documentTitle.isNotBlank() && selectedFileUri != null) {
                        // In a real app, we would upload the file to storage and get a URL
                        // For this example, we'll just use the URI string
                        val document = SignableDocument(
                            title = documentTitle,
                            description = documentDescription.takeIf { it.isNotBlank() },
                            documentUrl = selectedFileUri.toString(),
                            documentType = documentType,
                            createdBy = "current_user_id", // In a real app, get from auth
                            signatureFields = emptyList()
                        )
                        onDocumentUploaded(document)
                    }
                },
                enabled = documentTitle.isNotBlank() && selectedFileUri != null
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
 * Dialog for capturing a signature
 */
@Composable
fun SignatureDialog(
    onDismiss: () -> Unit,
    onSignatureCompleted: (DigitalSignature) -> Unit,
    documentId: String,
    documentType: DocumentType
) {
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf(Path()) }
    var signerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Document") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = signerName,
                    onValueChange = { signerName = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Draw your signature below:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Signature canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        currentPath = Path()
                                        currentPath.moveTo(it.x, it.y)
                                    },
                                    onDrag = { change, _ ->
                                        val position = change.position
                                        currentPath.lineTo(position.x, position.y)
                                    },
                                    onDragEnd = {
                                        paths = paths + currentPath
                                    }
                                )
                            }
                    ) {
                        // Draw all paths
                        paths.forEach { path ->
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 5f)
                            )
                        }

                        // Draw current path
                        drawPath(
                            path = currentPath,
                            color = Color.Black,
                            style = Stroke(width = 5f)
                        )
                    }

                    // Clear button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                paths = emptyList()
                                currentPath = Path()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (signerName.isNotBlank() && paths.isNotEmpty()) {
                        // In a real app, we would convert the signature to an image and upload it
                        // For this example, we'll just create a signature object
                        val signature = DigitalSignature(
                            signatureImageUrl = "signature_image_url", // In a real app, upload and get URL
                            signedBy = signerName,
                            documentId = documentId,
                            documentType = documentType,
                            ipAddress = "127.0.0.1", // In a real app, get actual IP
                            deviceInfo = "Android Device" // In a real app, get device info
                        )
                        onSignatureCompleted(signature)
                    }
                },
                enabled = signerName.isNotBlank() && paths.isNotEmpty()
            ) {
                Text("Sign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Extension function to capitalize the first letter of a string
fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}
