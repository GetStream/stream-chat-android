package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the styling for the icon container component.
 *
 * @param size The size of the icon container.
 * @param padding The padding of the icon container.
 * @param icon The styling for the icon.
 */
public data class IconContainerStyle(
    val size: ComponentSize,
    val padding: ComponentPadding,
    val icon: IconStyle,
)

/**
 * Represents the styling for the icon component.
 *
 * @param painter The painter to use for the icon.
 * @param tint The tint color for the icon.
 * @param size The size of the icon.
 */
public data class IconStyle(
    val painter: Painter,
    val tint: Color,
    val size: ComponentSize,
)