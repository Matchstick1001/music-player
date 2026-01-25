package com.example.fzo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val FzoColorScheme = darkColorScheme(
    primary = PrimaryLavender,
    onPrimary = OnBackground,
    primaryContainer = PrimaryLavender.copy(alpha = 0.2f),
    onPrimaryContainer = AccentCyan,
    
    secondary = AccentCyan,
    onSecondary = BackgroundDark,
    secondaryContainer = AccentCyan.copy(alpha = 0.1f),
    onSecondaryContainer = AccentCyan,
    
    background = BackgroundDark,
    onBackground = OnBackground,
    
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = CardHoverDark,
    onSurfaceVariant = TextGray,
    
    outline = TextGray.copy(alpha = 0.5f)
)

private val FzoShapes = Shapes(
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
        colorScheme = FzoColorScheme,
        typography = FzoTypography,
        shapes = FzoShapes,
        content = content
    )
}
