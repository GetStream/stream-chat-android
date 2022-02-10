package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.previewdata.PreviewReactionOptionData
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
    Image(
        modifier = modifier,
        painter = option.painter,
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
        MessageReactionItem(option = PreviewReactionOptionData.reactionOption2())
    }
}

/**
 * Preview for [MessageReactionItem] that's not selected.
 */
@Preview
@Composable
public fun MessageReactionItemNotSelectedPreview() {
    ChatTheme {
        MessageReactionItem(option = PreviewReactionOptionData.reactionOption1())
    }
}
