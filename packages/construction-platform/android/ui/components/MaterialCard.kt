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
import com.constructionmanager.domain.model.Material
import com.constructionmanager.domain.model.MaterialCategory
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCard(
    material: Material,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = material.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = material.subcategory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                MaterialCategoryChip(category = material.category)
            }
            
            Text(
                text = material.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Price per ${material.unitOfMeasurement}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrice(material.currentPrice.toDouble()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Supplier",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = material.supplier,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (material.specifications.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                
                Text(
                    text = "Key Specifications",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                
                material.specifications.entries.take(2).forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                if (material.specifications.size > 2) {
                    Text(
                        text = "+${material.specifications.size - 2} more specs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialCategoryChip(category: MaterialCategory) {
    val (icon, color) = when (category) {
        MaterialCategory.LUMBER -> Icons.Default.Forest to MaterialTheme.colorScheme.secondary
        MaterialCategory.CONCRETE -> Icons.Default.Foundation to MaterialTheme.colorScheme.tertiary
        MaterialCategory.STEEL -> Icons.Default.Hardware to MaterialTheme.colorScheme.primary
        MaterialCategory.ROOFING -> Icons.Default.Roofing to MaterialTheme.colorScheme.secondary
        MaterialCategory.ELECTRICAL -> Icons.Default.ElectricBolt to MaterialTheme.colorScheme.primary
        MaterialCategory.PLUMBING -> Icons.Default.Plumbing to MaterialTheme.colorScheme.secondary
        MaterialCategory.HVAC -> Icons.Default.Air to MaterialTheme.colorScheme.tertiary
        MaterialCategory.WINDOWS -> Icons.Default.Window to MaterialTheme.colorScheme.primary
        MaterialCategory.DOORS -> Icons.Default.MeetingRoom to MaterialTheme.colorScheme.secondary
        MaterialCategory.FLOORING -> Icons.Default.Layers to MaterialTheme.colorScheme.tertiary
        MaterialCategory.FIXTURES -> Icons.Default.Lightbulb to MaterialTheme.colorScheme.primary
        MaterialCategory.HARDWARE -> Icons.Default.Construction to MaterialTheme.colorScheme.secondary
        MaterialCategory.TOOLS -> Icons.Default.Build to MaterialTheme.colorScheme.tertiary
        else -> Icons.Default.Category to MaterialTheme.colorScheme.outline
    }
    
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = category.name.replace("_", " "),
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(price)
}