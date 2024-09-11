package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

/**
 * Represents the styling for the icon button component.
 */
public data class IconContainerStyle(
    val width: Dp,
    val height: Dp,
    val padding: Dp,
    val icon: IconStyle,
)

/**
 * Represents the styling for the icon component.
 */
public data class IconStyle(
    val painter: Painter,
    val tint: Color,
    val width: Dp,
    val height: Dp,
)