package io.getstream.chat.android.compose.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Wraps the content of a message in a bubble.
 *
 * @param color The color of the bubble.
 * @param shape The shape of the bubble.
 * @param modifier Modifier for styling.
 * @param border The optional border of the bubble.
 * @param content The content of the message.
 */
@Composable
public fun MessageBubble(
    color: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    border: BorderStroke? = BorderStroke(1.dp, ChatTheme.colors.borders),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        border = border,
    ) {
        content()
    }
}
