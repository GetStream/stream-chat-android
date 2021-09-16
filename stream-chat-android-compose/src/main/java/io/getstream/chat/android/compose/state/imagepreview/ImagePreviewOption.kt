package io.getstream.chat.android.compose.state.imagepreview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the information for image preview options the user can take.
 *
 * @param title The title of the option in the list.
 * @param titleColor The color of the title option.
 * @param icon The icon of the option.
 * @param iconColor The color of the icon.
 * @param action The action this option represents.
 */
public data class ImagePreviewOption(
    public val title: String,
    public val titleColor: Color,
    public val icon: ImageVector,
    public val iconColor: Color,
    public val action: ImagePreviewAction,
)
