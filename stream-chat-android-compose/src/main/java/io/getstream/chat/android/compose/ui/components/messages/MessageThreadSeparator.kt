package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a thread separator item that is displayed in thread mode to separate a parent message
 * from thread replies.
 *
 * @param threadSeparator The data used to show the separator text.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageThreadSeparator(
    threadSeparator: ThreadSeparatorState,
    modifier: Modifier = Modifier,
) {
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            ChatTheme.colors.threadSeparatorGradientStart,
            ChatTheme.colors.threadSeparatorGradientEnd
        )
    )
    val replyCount = threadSeparator.replyCount

    Box(
        modifier = modifier
            .background(brush = backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(vertical = ChatTheme.dimens.threadSeparatorTextVerticalPadding),
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_separator,
                replyCount,
                replyCount
            ),
            color = ChatTheme.colors.textLowEmphasis,
            style = ChatTheme.typography.body
        )
    }
}
