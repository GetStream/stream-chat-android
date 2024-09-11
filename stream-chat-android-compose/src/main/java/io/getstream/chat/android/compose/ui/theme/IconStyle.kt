package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the styling for the icon button component.
 */
public data class IconContainerStyle(
    val size: ComponentSize,
    val padding: ComponentPadding,
    val icon: IconStyle,
)

/**
 * Represents the styling for the icon component.
 */
public data class IconStyle(
    val painter: Painter,
    val tint: Color,
    val size: ComponentSize,
)