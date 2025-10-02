package com.nextgenbuildpro.features.analytics.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.features.analytics.AdvancedAnalyticsDashboard
import kotlinx.coroutines.launch

/**
 * Advanced Analytics Dashboard Screen
 *
 * Real-time KPI visualization with predictive insights:
 * - Safety metrics with trend analysis
 * - Performance indicators with efficiency tracking
 * - Cost analytics with overrun predictions
 * - Quality metrics with defect analysis
 * - AI-powered recommendations and alerts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAnalyticsDashboardScreen(
    navController: NavController,
    analyticsDashboard: AdvancedAnalyticsDashboard
) {
    val scope = rememberCoroutineScope()

    // State collections
    var dashboardData by remember { mutableStateOf<com.nextgenbuildpro.features.analytics.DashboardData?>(null) }
    var aiRecommendations by remember { mutableStateOf<List<com.nextgenbuildpro.features.analytics.AIRecommendation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load data on composition
    LaunchedEffect(Unit) {
        scope.launch {
            dashboardData = analyticsDashboard.getDashboardData().getOrNull()
            aiRecommendations = analyticsDashboard.generateAIRecommendations("General project oversight").getOrNull() ?: emptyList()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Analytics Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                dashboardData = analyticsDashboard.getDashboardData().getOrNull()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "Real-Time Construction Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Safety KPIs
                item {
                    KPICard(
                        title = "Safety Metrics",
                        icon = Icons.Default.Security,
                        iconTint = Color.Red
                    ) {
                        dashboardData?.safetyKPIs?.let { safety ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIRow("Current Incidents", "${safety.currentIncidents}")
                                KPIRow("Monthly Trend", "${String.format("%.1f%%", safety.incidentsTrend * 100)}")
                                KPIRow("Risk Level", safety.riskLevel, getRiskColor(safety.riskLevel))
                                KPIRow("Detection Rate", "${String.format("%.1f%%", safety.hazardDetectionRate * 100)}")
                                KPIRow("Compliance Score", "${String.format("%.1f%%", safety.complianceScore)}")
                            }
                        }
                    }
                }

                // Performance KPIs
                item {
                    KPICard(
                        title = "Performance Metrics",
                        icon = Icons.Default.Speed,
                        iconTint = Color.Blue
                    ) {
                        dashboardData?.performanceKPIs?.let { perf ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIRow("System Efficiency", "${perf.systemEfficiency}%")
                                KPIRow("Avg Response Time", "${perf.averageResponseTime}ms")
                                KPIRow("Active Agents", "${perf.activeAgents}")
                                KPIRow("Task Completion", "${String.format("%.1f%%", perf.taskCompletionRate)}")
                                KPIRow("Throughput", "${perf.throughput}/min")
                            }
                        }
                    }
                }

                // Cost KPIs
                item {
                    KPICard(
                        title = "Cost Analytics",
                        icon = Icons.Default.AttachMoney,
                        iconTint = Color.Green
                    ) {
                        dashboardData?.costKPIs?.let { cost ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIRow("Cost Variance", "${String.format("%.1f%%", cost.currentCostVariance)}")
                                KPIRow("Projected Overrun", "${String.format("%.1f%%", cost.projectedOverrun)}")
                                KPIRow("Budget Utilization", "${String.format("%.1f%%", cost.budgetUtilization)}")
                                KPIRow("Cost Savings", "$${String.format("%,.0f", cost.costSavings)}")
                            }
                        }
                    }
                }

                // Quality KPIs
                item {
                    KPICard(
                        title = "Quality Metrics",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF4CAF50)
                    ) {
                        dashboardData?.qualityKPIs?.let { quality ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIRow("Overall Score", "${quality.overallScore}/100")
                                KPIRow("Pass Rate", "${String.format("%.1f%%", quality.inspectionPassRate * 100)}")
                                KPIRow("Defect Reduction", "${String.format("%.1f%%", quality.defectReduction * 100)}")
                                KPIRow("Rework Rate", "${String.format("%.1f%%", quality.reworkRate * 100)}")
                            }
                        }
                    }
                }

                // Predictive Insights
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Insights,
                                    contentDescription = "Predictive Insights",
                                    tint = Color(0xFF9C27B0)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Predictive Insights (30-Day Horizon)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            dashboardData?.predictiveInsights?.let { insights ->
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PredictiveInsightRow("Safety Risk", insights.safetyRiskLevel, insights.safetyRiskProbability)
                                    PredictiveInsightRow("Cost Overrun", insights.costOverrunRisk, insights.costOverrunProbability)
                                    PredictiveInsightRow("Schedule Delay", insights.scheduleDelayRisk, insights.scheduleDelayProbability)
                                    PredictiveInsightRow("Resource Shortage", insights.resourceShortageRisk, insights.resourceShortageProbability)
                                    PredictiveInsightRow("Quality Failure", insights.qualityFailureRisk, insights.qualityFailureProbability)
                                }
                            }
                        }
                    }
                }

                // AI Recommendations
                item {
                    Text(
                        text = "AI-Powered Recommendations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(aiRecommendations) { recommendation ->
                    RecommendationCard(recommendation)
                }

                // Alerts
                dashboardData?.alerts?.let { alerts ->
                    if (alerts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active Alerts",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(alerts) { alert ->
                            AlertCard(alert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KPICard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = title, tint = iconTint)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
fun KPIRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun PredictiveInsightRow(label: String, riskLevel: String, probability: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                riskLevel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getRiskColor(riskLevel)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "${String.format("%.0f%%", probability * 100)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendationCard(recommendation: com.nextgenbuildpro.features.analytics.AIRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.priority) {
                "HIGH" -> MaterialTheme.colorScheme.errorContainer
                "MEDIUM" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    recommendation.priority,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (recommendation.priority) {
                        "HIGH" -> MaterialTheme.colorScheme.error
                        "MEDIUM" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Confidence: ${String.format("%.0f%%", recommendation.confidence * 100)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    recommendation.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AlertCard(alert: com.nextgenbuildpro.features.analytics.AlertData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                "HIGH" -> MaterialTheme.colorScheme.errorContainer
                "MEDIUM" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Alert",
                tint = when (alert.severity) {
                    "HIGH" -> MaterialTheme.colorScheme.error
                    "MEDIUM" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    alert.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getRiskColor(riskLevel: String): Color {
    return when (riskLevel.uppercase()) {
        "CRITICAL" -> Color.Red
        "HIGH" -> Color(0xFFFF9800) // Orange
        "MEDIUM" -> Color(0xFFFFC107) // Amber
        "LOW" -> Color.Green
        else -> MaterialTheme.colorScheme.onSurface
    }
}