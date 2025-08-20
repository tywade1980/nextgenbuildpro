package com.nextgenbuildpro.crm.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.core.ui.*
import com.nextgenbuildpro.crm.data.model.MessageRecord
import com.nextgenbuildpro.crm.viewmodel.MessagesViewModel

/**
 * Screen for displaying and managing messages
 */
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel,
    leadId: String? = null,
    leadName: String? = null,
    phoneNumber: String? = null
) {
    val messages by viewModel.leadMessages.collectAsState()
    val messageDraft by viewModel.messageDraft.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // If leadId is provided, select that lead
    LaunchedEffect(leadId) {
        if (leadId != null) {
            viewModel.selectLead(leadId)
            viewModel.getMessagesForLead(leadId)
        }
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = leadName ?: "Messages",
                onBackClick = { 
                    viewModel.clearSelectedLead()
                    navController.popBackStack() 
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else if (error != null) {
                ErrorMessage(error = error!!) {
                    if (leadId != null) {
                        viewModel.getMessagesForLead(leadId)
                    } else {
                        viewModel.refresh()
                    }
                }
            } else if (leadId == null) {
                // No lead selected, show lead selection screen
                LeadSelectionScreen(navController)
            } else if (messages.isEmpty()) {
                EmptyState(
                    message = "No messages yet",
                    icon = Icons.Default.Message,
                    actionLabel = "Send Message",
                    onActionClick = { /* Focus on message input */ }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Messages list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true // Show newest messages at the bottom
                    ) {
                        items(messages.sortedBy { it.timestamp }) { message ->
                            MessageBubble(message = message)
                        }
                    }
                    
                    // Message input
                    MessageInput(
                        value = messageDraft,
                        onValueChange = { viewModel.updateMessageDraft(it) },
                        onSendClick = {
                            if (leadId != null && leadName != null && phoneNumber != null) {
                                viewModel.sendMessage(leadId, leadName, phoneNumber)
                            }
                        },
                        onTemplateClick = {
                            if (leadName != null) {
                                viewModel.generateMessageTemplate(leadName, "follow_up")
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Screen for selecting a lead to message
 */
@Composable
fun LeadSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select a lead to message",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // In a real app, this would be a list of leads
        // For now, we'll just show a button to go to the leads screen
        Button(
            onClick = { navController.navigate("leads") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Leads")
        }
    }
}

/**
 * Message bubble for displaying a message
 */
@Composable
fun MessageBubble(message: MessageRecord) {
    val isOutgoing = message.type == "Outgoing"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOutgoing) 16.dp else 4.dp,
                        bottomEnd = if (isOutgoing) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isOutgoing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = message.timestamp.substringAfter(" "), // Show only time
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(4.dp))
}

/**
 * Message input for composing and sending messages
 */
@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onTemplateClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTemplateClick) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = "Template"
                )
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Type a message...") },
                maxLines = 4
            )
            
            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}