package io.getstream.chat.android.compose.state.imagepreview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the information for image preview options the user can take.
 *
 * @param title The title of the option in the list.
 * @param titleColor The color of the title option.
 * @param iconPainter The icon of the option.
 * @param iconColor The color of the icon.
 * @param action The action this option represents.
 */
internal data class ImagePreviewOption(
    internal val title: String,
    internal val titleColor: Color,
    internal val iconPainter: Painter,
    internal val iconColor: Color,
    internal val action: ImagePreviewAction,
)
