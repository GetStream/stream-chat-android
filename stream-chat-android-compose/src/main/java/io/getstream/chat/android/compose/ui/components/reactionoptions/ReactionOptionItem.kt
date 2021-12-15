package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Individual reaction item.
 *
 * @param option The reaction to show.
 * @param modifier Modifier for styling.
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
 * Preview of [ReactionOptionItem] in its non selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Not Selected)")
@Composable
private fun ReactionOptionItemNotSelectedPreview() {
    ChatTheme {
        val reactionTypeEntry = ChatTheme.reactionTypes.entries.firstOrNull()

        reactionTypeEntry?.let { (type, drawableRes) ->
            ReactionOptionItem(
                option = ReactionOptionItemState(
                    painter = painterResource(id = drawableRes),
                    isSelected = false,
                    type = type
                )
            )
        }
    }
}

/**
 * Preview of [ReactionOptionItem] in its selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Selected)")
@Composable
private fun ReactionOptionItemSelectedPreview() {
    ChatTheme {
        val reactionTypeEntry = ChatTheme.reactionTypes.entries.firstOrNull()

        reactionTypeEntry?.let { (type, drawableRes) ->
            ReactionOptionItem(
                option = ReactionOptionItemState(
                    painter = painterResource(id = drawableRes),
                    isSelected = true,
                    type = type
                )
            )
        }
    }
}
