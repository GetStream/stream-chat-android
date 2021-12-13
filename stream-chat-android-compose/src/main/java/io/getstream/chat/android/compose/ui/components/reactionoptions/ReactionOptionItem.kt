package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme.reactionTypes

/**
 * Individual reaction item.
 *
 * @param option The reaction to show.
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Icon].
 */
@Composable
public fun ReactionOptionItem(
    option: ReactionOptionItemState,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        painter = option.painter,
        contentDescription = option.type,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
    )
}

/**
 * Preview of [ReactionOptionItem] in it's non selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Not Selected)")
@Composable
private fun ReactionOptionItemNotSelectedPreview() {
    ChatTheme {
        val reactionOptionItem = reactionTypes.entries.map { (type, drawable) ->
            ReactionOptionItemState(
                painter = painterResource(id = drawable),
                isSelected = false,
                type = type
            )
        }

        reactionOptionItem.firstOrNull()?.let {
            ReactionOptionItem(option = it)
        }
    }
}

/**
 * Preview of [ReactionOptionItem] in it's selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Selected)")
@Composable
private fun ReactionOptionItemSelectedPreview() {
    ChatTheme {
        val reactionOptionItem = reactionTypes.entries.map { (type, drawable) ->
            ReactionOptionItemState(
                painter = painterResource(id = drawable),
                isSelected = true,
                type = type
            )
        }

        reactionOptionItem.firstOrNull()?.let {
            ReactionOptionItem(option = it)
        }
    }
}