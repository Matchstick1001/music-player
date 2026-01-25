package com.example.fzo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import com.example.fzo.ui.theme.GradientColors

@OptIn(ExperimentalTextApi::class)
@Composable
fun AnimatedAppTitle(
    modifier: Modifier = Modifier,
    text: String = "FZO Player"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "title_animation")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    val brush = Brush.linearGradient(
        colors = GradientColors,
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 500f, 500f)
    )

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            brush = brush
        ),
        modifier = modifier
    )
}
