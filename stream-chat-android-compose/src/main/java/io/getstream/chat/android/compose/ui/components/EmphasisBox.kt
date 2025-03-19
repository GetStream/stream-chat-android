package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A box that can be used to emphasize its content by adding a transparent overlay on top of it.
 */
@Composable
internal fun EmphasisBox(
    isEmphasized: Boolean,
    content: @Composable () -> Unit,
) {
    val color = ChatTheme.colors.textHighEmphasis
    Box(
        modifier = Modifier.drawWithContent {
            drawContent()
            if (isEmphasized) {
                drawRect(color = color, alpha = 0.1f, size = size)
            }
        },
    ) {
        content()
    }
}
