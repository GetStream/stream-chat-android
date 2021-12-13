package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a meta information about the message that is shown above the message bubble.
 *
 * @param painter The icon to be shown.
 * @param text The text to be shown.
 * @param modifier Modifier for styling.
 * @param contentPadding The inner padding inside the component.
 */
@Composable
public fun MessageHeaderLabel(
    painter: Painter,
    modifier: Modifier = Modifier,
    text: String? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 2.dp, horizontal = 4.dp),
) {
    Row(
        modifier = modifier.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 2.dp)
                .size(14.dp),
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis
        )

        if (text != null) {
            Text(
                text = text,
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis
            )
        }
    }
}
