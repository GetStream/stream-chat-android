package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a small message reaction item.
 *
 * @param option The reaction option state, holding all information required to render the icon.
 * @param modifier for styling.
 */
@Composable
public fun MessageReactionItem(
    option: ReactionOptionItemState,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        painter = option.painter,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
        contentDescription = null
    )
}

/**
 * Preview for [MessageReactionItem] that's selected.
 */
@Preview
@Composable
public fun MessageReactionItemSelectedPreview() {
    ChatTheme {
        MessageReactionItem(option = PreviewReactionData.oneReaction().first())
    }
}

/**
 * Preview for [MessageReactionItem] that's not selected.
 */
@Preview
@Composable
public fun MessageReactionItemNotSelectedPreview() {
    ChatTheme {
        MessageReactionItem(option = PreviewReactionData.oneReaction().first().copy(isSelected = false))
    }
}
