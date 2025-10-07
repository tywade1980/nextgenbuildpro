package com.nextgenbuildpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Construction Theme for NextGen BuildPro
 *
 * Optimized for construction workers with:
 * - Glove-friendly touch targets (minimum 48dp)
 * - High contrast colors for outdoor visibility
 * - Large, readable fonts
 * - Bright color schemes for sunlight readability
 * - Construction-specific color coding
 */

// Construction-specific colors
val ConstructionColors = ConstructionColorScheme(
    // Safety colors (OSHA standard)
    safetyRed = Color(0xFFD32F2F),
    safetyOrange = Color(0xFFF57C00),
    safetyYellow = Color(0xFFFBC02D),
    safetyGreen = Color(0xFF388E3C),

    // Material colors
    concreteGray = Color(0xFF757575),
    steelBlue = Color(0xFF1976D2),
    woodBrown = Color(0xFF8D6E63),

    // Status colors
    completeGreen = Color(0xFF4CAF50),
    inProgressBlue = Color(0xFF2196F3),
    delayedOrange = Color(0xFFFF9800),
    issueRed = Color(0xFFF44336),

    // Outdoor visibility colors (high contrast)
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF424242),
    backgroundLight = Color(0xFFFFFFFF),
    backgroundDark = Color(0xFF121212),

    // Glove-friendly accent colors
    accentPrimary = Color(0xFF1976D2),
    accentSecondary = Color(0xFF4CAF50),
    accentTertiary = Color(0xFFFF9800)
)

data class ConstructionColorScheme(
    val safetyRed: Color,
    val safetyOrange: Color,
    val safetyYellow: Color,
    val safetyGreen: Color,
    val concreteGray: Color,
    val steelBlue: Color,
    val woodBrown: Color,
    val completeGreen: Color,
    val inProgressBlue: Color,
    val delayedOrange: Color,
    val issueRed: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val backgroundLight: Color,
    val backgroundDark: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentTertiary: Color
)

val LocalConstructionColors = staticCompositionLocalOf { ConstructionColors }

// Construction-optimized typography
val ConstructionTypography = Typography(
    // Display styles - Large for outdoor readability
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles - Bold and clear
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - Clear hierarchy
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - High readability
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - Bold for buttons and labels
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Construction-optimized shapes
val ConstructionShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Light construction theme
private val LightConstructionColorScheme = lightColorScheme(
    primary = ConstructionColors.accentPrimary,
    onPrimary = Color.White,
    primaryContainer = ConstructionColors.accentPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = ConstructionColors.accentPrimary,

    secondary = ConstructionColors.accentSecondary,
    onSecondary = Color.White,
    secondaryContainer = ConstructionColors.accentSecondary.copy(alpha = 0.1f),
    onSecondaryContainer = ConstructionColors.accentSecondary,

    tertiary = ConstructionColors.accentTertiary,
    onTertiary = Color.White,
    tertiaryContainer = ConstructionColors.accentTertiary.copy(alpha = 0.1f),
    onTertiaryContainer = ConstructionColors.accentTertiary,

    error = ConstructionColors.safetyRed,
    onError = Color.White,
    errorContainer = ConstructionColors.safetyRed.copy(alpha = 0.1f),
    onErrorContainer = ConstructionColors.safetyRed,

    background = ConstructionColors.backgroundLight,
    onBackground = ConstructionColors.textPrimary,
    surface = ConstructionColors.backgroundLight,
    onSurface = ConstructionColors.textPrimary,
    surfaceVariant = ConstructionColors.concreteGray.copy(alpha = 0.1f),
    onSurfaceVariant = ConstructionColors.textSecondary,

    outline = ConstructionColors.concreteGray,
    outlineVariant = ConstructionColors.concreteGray.copy(alpha = 0.5f),

    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = ConstructionColors.accentPrimary
)

// Dark construction theme (for low light conditions)
private val DarkConstructionColorScheme = darkColorScheme(
    primary = ConstructionColors.accentPrimary,
    onPrimary = Color.Black,
    primaryContainer = ConstructionColors.accentPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = ConstructionColors.accentPrimary,

    secondary = ConstructionColors.accentSecondary,
    onSecondary = Color.Black,
    secondaryContainer = ConstructionColors.accentSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = ConstructionColors.accentSecondary,

    tertiary = ConstructionColors.accentTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = ConstructionColors.accentTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = ConstructionColors.accentTertiary,

    error = ConstructionColors.safetyRed,
    onError = Color.Black,
    errorContainer = ConstructionColors.safetyRed.copy(alpha = 0.2f),
    onErrorContainer = ConstructionColors.safetyRed,

    background = ConstructionColors.backgroundDark,
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = ConstructionColors.concreteGray.copy(alpha = 0.2f),
    onSurfaceVariant = ConstructionColors.concreteGray,

    outline = ConstructionColors.concreteGray,
    outlineVariant = ConstructionColors.concreteGray.copy(alpha = 0.5f),

    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = ConstructionColors.accentPrimary
)

/**
 * Construction Theme - Optimized for construction workers
 *
 * Features:
 * - Glove-friendly touch targets (48dp minimum)
 * - High contrast for outdoor visibility
 * - Large, readable fonts
 * - Construction-specific color coding
 * - Bright themes for sunlight readability
 */
@Composable
fun ConstructionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkConstructionColorScheme else LightConstructionColorScheme

    CompositionLocalProvider(LocalConstructionColors provides ConstructionColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ConstructionTypography,
            shapes = ConstructionShapes,
            content = content
        )
    }
}

// Extension properties for construction colors
val MaterialTheme.constructionColors: ConstructionColorScheme
    @Composable
    get() = LocalConstructionColors.current

// Construction-specific component defaults
object ConstructionComponents {
    // Glove-friendly button sizes
    val MinTouchTargetSize = 48.dp
    val LargeButtonHeight = 56.dp
    val ExtraLargeButtonHeight = 64.dp

    // High contrast text sizes for outdoor use
    val LargeTextSize = 18.sp
    val ExtraLargeTextSize = 22.sp
    val HugeTextSize = 28.sp

    // Construction status colors
    fun getStatusColor(status: String): Color {
        return when (status.uppercase()) {
            "COMPLETE", "COMPLETED", "DONE" -> ConstructionColors.completeGreen
            "IN_PROGRESS", "IN PROGRESS", "WORKING" -> ConstructionColors.inProgressBlue
            "DELAYED", "BEHIND", "LATE" -> ConstructionColors.delayedOrange
            "ISSUE", "PROBLEM", "ERROR", "FAILED" -> ConstructionColors.issueRed
            "PENDING", "WAITING" -> ConstructionColors.concreteGray
            else -> ConstructionColors.accentPrimary
        }
    }

    // Safety color mapping
    fun getSafetyColor(severity: String): Color {
        return when (severity.uppercase()) {
            "CRITICAL", "HIGH" -> ConstructionColors.safetyRed
            "MEDIUM", "WARNING" -> ConstructionColors.safetyOrange
            "LOW", "INFO" -> ConstructionColors.safetyYellow
            "SAFE", "GOOD" -> ConstructionColors.safetyGreen
            else -> ConstructionColors.concreteGray
        }
    }

    // Material type colors
    fun getMaterialColor(material: String): Color {
        return when (material.lowercase()) {
            "concrete", "cement" -> ConstructionColors.concreteGray
            "steel", "metal", "rebar" -> ConstructionColors.steelBlue
            "wood", "lumber", "timber" -> ConstructionColors.woodBrown
            else -> ConstructionColors.accentPrimary
        }
    }
}