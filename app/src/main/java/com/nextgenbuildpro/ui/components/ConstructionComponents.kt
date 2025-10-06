package com.nextgenbuildpro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextgenbuildpro.ui.theme.ConstructionComponents
import com.nextgenbuildpro.ui.theme.constructionColors

/**
 * Construction-Optimized UI Components
 *
 * Glove-friendly design with large touch targets and high contrast colors
 * for construction workers in outdoor environments.
 */

// Large, glove-friendly button
@Composable
fun ConstructionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(ConstructionComponents.LargeButtonHeight)
            .defaultMinSize(minWidth = 120.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = ConstructionComponents.LargeTextSize,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// Extra large button for critical actions
@Composable
fun ConstructionActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    isCritical: Boolean = false
) {
    val buttonColor = if (isCritical) {
        MaterialTheme.constructionColors.safetyRed
    } else {
        color
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(ConstructionComponents.ExtraLargeButtonHeight)
            .fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = buttonColor.copy(alpha = 0.1f),
            contentColor = buttonColor
        ),
        border = BorderStroke(3.dp, buttonColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = ConstructionComponents.ExtraLargeTextSize,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// Status indicator with construction colors
@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    val color = ConstructionComponents.getStatusColor(status)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(50),
            modifier = Modifier.size(16.dp)
        ) {}
        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = status.replace("_", " ").uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = ConstructionComponents.LargeTextSize,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

// Safety alert card with high visibility
@Composable
fun SafetyAlertCard(
    title: String,
    message: String,
    severity: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (severity.uppercase()) {
        "CRITICAL", "HIGH" -> MaterialTheme.constructionColors.safetyRed.copy(alpha = 0.1f)
        "MEDIUM" -> MaterialTheme.constructionColors.safetyOrange.copy(alpha = 0.1f)
        "LOW" -> MaterialTheme.constructionColors.safetyYellow.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = ConstructionComponents.getSafetyColor(severity)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = borderColor,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(24.dp)
                ) {
                    // Safety icon would go here
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = ConstructionComponents.ExtraLargeTextSize,
                        fontWeight = FontWeight.Bold
                    ),
                    color = borderColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = ConstructionComponents.LargeTextSize
                )
            )
        }
    }
}

// Large KPI display card
@Composable
fun KPIDisplayCard(
    title: String,
    value: String,
    unit: String = "",
    trend: String = "",
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = ConstructionComponents.LargeTextSize,
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = ConstructionComponents.HugeTextSize,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )

            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = ConstructionComponents.LargeTextSize
                    ),
                    color = color.copy(alpha = 0.7f)
                )
            }

            if (trend.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = ConstructionComponents.LargeTextSize
                    ),
                    color = when {
                        trend.contains("↑") || trend.contains("+") -> MaterialTheme.constructionColors.safetyGreen
                        trend.contains("↓") || trend.contains("-") -> MaterialTheme.constructionColors.safetyRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

// Material type indicator
@Composable
fun MaterialIndicator(
    material: String,
    quantity: String = "",
    modifier: Modifier = Modifier
) {
    val color = ConstructionComponents.getMaterialColor(material)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(width = 60.dp, height = 32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = material.take(3).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = ConstructionComponents.LargeTextSize,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = material,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = ConstructionComponents.LargeTextSize,
                    fontWeight = FontWeight.Bold
                )
            )
            if (quantity.isNotEmpty()) {
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = ConstructionComponents.LargeTextSize
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Quick action grid for field workers
@Composable
fun QuickActionGrid(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
    columns: Int = 2
) {
    val rows = (actions.size + columns - 1) / columns

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < actions.size) {
                        QuickActionButton(
                            action = actions[index],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    ConstructionButton(
        onClick = action.onClick,
        modifier = modifier.height(ConstructionComponents.LargeButtonHeight),
        text = action.title,
        icon = action.icon,
        color = action.color ?: MaterialTheme.colorScheme.primary
    )
}

// Progress indicator with construction context
@Composable
fun ConstructionProgressIndicator(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = ConstructionComponents.LargeTextSize,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = ConstructionComponents.LargeTextSize,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// Emergency button with high visibility
@Composable
fun EmergencyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "EMERGENCY"
) {
    ConstructionActionButton(
        onClick = onClick,
        modifier = modifier,
        text = text,
        icon = androidx.compose.material.icons.Icons.Default.Warning,
        color = MaterialTheme.constructionColors.safetyRed,
        isCritical = true
    )
}

// Data class for quick actions
data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val color: Color? = null
)