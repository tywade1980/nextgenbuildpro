package com.nextgenbuildpro.crm.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Message Detail Screen - Shows conversation history with a specific client
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(navController: NavController, conversationId: String) {
    val conversation = remember { getConversationById(conversationId) }
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(conversation.messages) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(conversation.clientName)
                        Text(
                            text = conversation.lastSeen,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Call client */ }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { /* Video call */ }) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Video Call")
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = {
            MessageInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        val newMessage = ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            text = messageText,
                            isFromMe = true,
                            timestamp = "Just now",
                            status = "Sent"
                        )
                        messages = messages + newMessage
                        messageText = ""
                    }
                },
                onAttachFile = { /* Handle file attachment */ },
                onTakePhoto = { /* Handle photo capture */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message = message)
            }
            
            // Conversation info at the top
            item {
                ConversationInfoCard(conversation = conversation)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        if (!message.isFromMe) {
            // Client avatar
            Card(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.senderInitials ?: "C",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isFromMe) {
                Alignment.End
            } else {
                Alignment.Start
            }
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                    bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isFromMe) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isFromMe) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    if (message.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        message.attachments.forEach { attachment ->
                            AttachmentChip(attachment = attachment)
                        }
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (message.isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = when (message.status) {
                            "Sent" -> Icons.Default.Done
                            "Delivered" -> Icons.Default.DoneAll
                            "Read" -> Icons.Default.DoneAll
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = message.status,
                        modifier = Modifier.size(12.dp),
                        tint = if (message.status == "Read") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
        
        if (message.isFromMe) {
            Spacer(modifier = Modifier.width(8.dp))
            // My avatar
            Card(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Me",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                leadingIcon = {
                    Row {
                        IconButton(onClick = onAttachFile) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        }
                        IconButton(onClick = onTakePhoto) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ConversationInfoCard(conversation: ConversationDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Client",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = conversation.clientName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = conversation.projectName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("View Project") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text("Schedule") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AttachmentChip(attachment: MessageAttachment) {
    AssistChip(
        onClick = { /* Open attachment */ },
        label = { Text(attachment.name) },
        leadingIcon = {
            Icon(
                imageVector = when (attachment.type) {
                    "image" -> Icons.Default.Image
                    "document" -> Icons.Default.Description
                    "pdf" -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.AttachFile
                },
                contentDescription = attachment.type,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

// Data classes
data class ConversationDetail(
    val id: String,
    val clientName: String,
    val projectName: String,
    val lastSeen: String,
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String,
    val status: String = "",
    val senderInitials: String? = null,
    val attachments: List<MessageAttachment> = emptyList()
)

data class MessageAttachment(
    val name: String,
    val type: String,
    val size: String
)

private fun getConversationById(conversationId: String): ConversationDetail {
    return ConversationDetail(
        id = conversationId,
        clientName = "Sarah Johnson",
        projectName = "Kitchen Renovation",
        lastSeen = "Online now",
        messages = listOf(
            ChatMessage(
                id = "1",
                text = "Hi! I wanted to check on the progress of our kitchen renovation. How are things going?",
                isFromMe = false,
                timestamp = "10:30 AM",
                senderInitials = "SJ"
            ),
            ChatMessage(
                id = "2",
                text = "Hello Sarah! Everything is progressing well. We've completed the electrical work and are moving on to plumbing today.",
                isFromMe = true,
                timestamp = "10:45 AM",
                status = "Read"
            ),
            ChatMessage(
                id = "3",
                text = "That's great to hear! I've attached some photos of fixtures I'd like to discuss for the island.",
                isFromMe = false,
                timestamp = "11:00 AM",
                senderInitials = "SJ",
                attachments = listOf(
                    MessageAttachment("Kitchen Island Ideas.jpg", "image", "2.3 MB"),
                    MessageAttachment("Fixture Options.pdf", "pdf", "1.8 MB")
                )
            ),
            ChatMessage(
                id = "4",
                text = "Perfect! I'll review these with the team and get back to you with recommendations and pricing by tomorrow.",
                isFromMe = true,
                timestamp = "11:15 AM",
                status = "Read"
            ),
            ChatMessage(
                id = "5",
                text = "Also, when would be a good time for the countertop template? We're ready to schedule that.",
                isFromMe = true,
                timestamp = "11:16 AM",
                status = "Delivered"
            )
        )
    )
}