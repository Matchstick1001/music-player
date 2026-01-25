package com.example.fzo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fzo.ui.theme.PrimaryLavender

@Composable
fun FzoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 48.dp
    val trackHeight = 24.dp
    val thumbSize = 24.dp
    val borderSize = 4.dp

    val trackColor by animateColorAsState(
        targetValue = if (checked) PrimaryLavender else Color(0xFF4B5563), // Gray-600
        label = "trackColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize else 0.dp,
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(borderSize, if (checked) PrimaryLavender else Color(0xFFD1D5DB), CircleShape) // Gray-300
        )
    }
}
