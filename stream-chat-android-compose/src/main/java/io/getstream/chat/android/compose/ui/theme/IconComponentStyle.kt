package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

/**
 * Represents the styling for the icon component.
 */
public data class IconComponentStyle(
    val painter: Painter,
    val tint: Color,
    val width: Dp,
    val height: Dp,
)