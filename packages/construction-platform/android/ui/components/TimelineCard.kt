package com.constructionmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus

@Composable
fun TimelineCard(
    project: Project?,
    modifier: Modifier = Modifier
) {
    project?.let {
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Project Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimelineItem(
                        label = "Start Date",
                        date = project.startDate.toString(),
                        icon = Icons.Default.PlayArrow
                    )
                    TimelineItem(
                        label = "Est. Completion",
                        date = project.estimatedEndDate.toString(),
                        icon = Icons.Default.Flag
                    )
                }
                
                if (project.status == ProjectStatus.COMPLETED && project.actualEndDate != null) {
                    Divider()
                    TimelineItem(
                        label = "Actual Completion",
                        date = project.actualEndDate.toString(),
                        icon = Icons.Default.CheckCircle,
                        isCompleted = true
                    )
                }
                
                // Calculate and show project duration
                val durationText = calculateProjectDuration(project)
                if (durationText.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    label: String,
    date: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCompleted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calculateProjectDuration(project: Project): String {
    return try {
        val startDate = project.startDate
        val endDate = project.actualEndDate ?: project.estimatedEndDate
        
        val daysBetween: Long = (endDate.toEpochDays() - startDate.toEpochDays()).toLong()
        
        when {
            daysBetween < 0L -> ""
            daysBetween == 0L -> "Same day project"
            daysBetween == 1L -> "1 day duration"
            daysBetween < 7L -> "$daysBetween days duration"
            daysBetween < 30L -> "${daysBetween / 7} weeks duration"
            daysBetween < 365L -> "${daysBetween / 30} months duration"
            else -> "${daysBetween / 365} years duration"
        }
    } catch (e: Exception) {
        ""
    }
}