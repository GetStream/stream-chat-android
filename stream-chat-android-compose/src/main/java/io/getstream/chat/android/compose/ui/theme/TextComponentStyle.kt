package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Represents the styling for the text component.
 */
public data class TextComponentStyle(
    val color: Color,
    val style: TextStyle,
    val maxLines: Int = Int.MAX_VALUE,
    val overflow: TextOverflow = TextOverflow.Clip,
)
