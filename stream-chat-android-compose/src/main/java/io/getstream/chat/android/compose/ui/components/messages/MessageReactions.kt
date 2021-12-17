package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a reaction bubble with a list of reactions this message has.
 *
 * @param options The list of reactions to display.
 * @param modifier Modifier for styling.
 * @param itemContent Composable that represents each reaction item in the row of message reactions.
 */
@Composable
public fun MessageReactions(
    options: List<ReactionOptionItemState>,
    modifier: Modifier = Modifier,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        MessageReactionItem(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp)
                .align(Alignment.CenterVertically),
            option = option
        )
    },
) {
    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            itemContent(option)
        }
    }
}

/**
 * Preview of the [MessageReactions] with one reaction.
 */
@Preview
@Composable
private fun OneMessageReactionPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionData.oneReaction())
    }
}

/**
 * Preview of the [MessageReactions] with many reactions.
 */
@Preview
@Composable
private fun ManyMessageReactionsPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionData.manyReactions())
    }
}
