package com.example.fzo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Custom "Moody & Lazy" dark color scheme
// NO dynamic colors from system wallpaper - fixed colors only
private val MoodyLazyColorScheme = darkColorScheme(
    primary = MoodyPrimary,
    onPrimary = MoodyOnPrimary,
    primaryContainer = MoodyPrimaryContainer,
    onPrimaryContainer = MoodyOnPrimaryContainer,
    
    secondary = MoodySecondary,
    onSecondary = MoodyOnPrimary,
    secondaryContainer = MoodySecondaryContainer,
    onSecondaryContainer = MoodyOnSecondaryContainer,
    
    tertiary = MoodyTertiary,
    onTertiary = MoodyOnPrimary,
    
    background = MoodyBackground,
    onBackground = MoodyOnBackground,
    
    surface = MoodySurface,
    onSurface = MoodyOnSurface,
    surfaceVariant = MoodySurfaceVariant,
    onSurfaceVariant = MoodyOnSurfaceVariant,
    
    error = MoodyError,
    onError = MoodyOnError
)

// Rounded shapes for soft, relaxed aesthetic
private val MoodyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MoodyLazyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MoodyLazyColorScheme,
        typography = MoodyTypography,
        shapes = MoodyShapes,
        content = content
    )
}
